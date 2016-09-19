#!/usr/bin/python
#
# Copyright 2015 Red Hat
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import contextlib
import gettext
import json
import logging
import os
import os.path
import socket
import ssl
import sys
import urllib2
import urlparse

import ovirt_vmconsole_conf as config

from ovirt_engine import configfile, service, ticket

if sys.version_info[0] < 3:
    from httplib import HTTPSConnection
    from urllib2 import HTTPSHandler
    from urllib2 import build_opener
else:
    from http.client import HTTPSConnection
    from urllib.request import HTTPSHandler
    from urllib.request import build_opener


_HTTP_STATUS_CODE_SUCCESS = 200
_LOGGER_NAME = 'ovirt.engine.vmconsole.helper'


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-vmconsole-helper')


def urlopen(url, ca_certs=None, verify_host=True):

    if getattr(ssl, 'create_default_context', None):
        context = ssl.create_default_context()

        if verify_host:
            context.check_hostname = ssl.match_hostname
        else:
            context.check_hostname = None

        if ca_certs:
            context.load_verify_locations(cafile=ca_certs)
            context.verify_mode = ssl.CERT_REQUIRED
        else:
            context.verify_mode = ssl.CERT_NONE

        return contextlib.closing(
            build_opener(HTTPSHandler(context=context)).open(url)
        )

    else:
        class MyHTTPSConnection(HTTPSConnection):
            def __init__(self, host, **kwargs):
                self._ca_certs = kwargs.pop('ca_certs', None)
                HTTPSConnection.__init__(self, host, **kwargs)

            def connect(self):
                self.sock = ssl.wrap_socket(
                    socket.create_connection((self.host, self.port)),
                    cert_reqs=(
                        ssl.CERT_REQUIRED if self._ca_certs
                        else ssl.CERT_NONE
                    ),
                    ca_certs=self._ca_certs,
                )
                if verify_host:
                    cert = self.sock.getpeercert()
                    for field in cert.get('subject', []):
                        if field[0][0] == 'commonName':
                            expected = field[0][1]
                            break
                    else:
                        raise RuntimeError(
                            _('No CN in peer certificate')
                        )

                    if expected != self.host:
                        raise RuntimeError(
                            _(
                                "Invalid host '{host}' "
                                "expected '{expected}'"
                            ).format(
                                expected=expected,
                                host=self.host,
                            )
                        )

        class MyHTTPSHandler(HTTPSHandler):

            def __init__(self, ca_certs=None):
                HTTPSHandler.__init__(self)
                self._ca_certs = ca_certs

            def https_open(self, req):
                return self.do_open(self._get_connection, req)

            def _get_connection(self, host, timeout):
                return MyHTTPSConnection(
                    host=host,
                    timeout=timeout,
                    ca_certs=self._ca_certs,
                )

        return contextlib.closing(
            build_opener(MyHTTPSHandler(ca_certs=ca_certs)).open(url)
        )


def make_ticket_encoder(cfg_file):
    return ticket.TicketEncoder(
        cfg_file.get('TOKEN_CERTIFICATE'),
        cfg_file.get('TOKEN_KEY'),
    )


def parse_args():
    parser = argparse.ArgumentParser(
        description='ovirt-vmconsole-proxy helper tool')
    parser.add_argument(
        '--debug', default=False, action='store_true',
        help='enable debug log',
    )
    parser.add_argument(
        '--version', metavar='V', type=int, nargs='?', default=1,
        help='version of the protocol to use',
    )
    subparsers = parser.add_subparsers(
        dest='entity',
        help='subcommand help',
    )

    parser_consoles = subparsers.add_parser(
        'consoles',
        help='list available consoles',
    )
    parser_consoles.add_argument(
        '--entityid', nargs='?', type=str, default='',
        help='entity ID where needed',
    )

    parser_keys = subparsers.add_parser(
        'keys',
        help='list available keys',
    )
    parser_keys.add_argument(
        '--keyfp', nargs='?', type=str, default='',
        help='list only the keys matching the given fingerprint',
    )
    parser_keys.add_argument(
        '--keytype', nargs='?', type=str, default='',
        help='list only the keys matching the given key type (e.g. ssh-rsa)',
    )
    parser_keys.add_argument(
        '--keycontent', nargs='?', type=str, default='',
        help='list only the keys matching the given content',
    )

    return parser.parse_args()


def make_request(args):
    if args.entity == 'keys':
        return {
            'command': 'public_keys',
            'version': args.version,
            'key_fp': args.keyfp,
            'key_type': args.keytype,
            'key_content': args.keycontent,
        }
    elif args.entity == 'consoles':
        if args.entityid is None:
            raise ValueError('entityid required and not found')
        return {
            'command': 'available_consoles',
            'version': args.version,
            'user_id': args.entityid,
        }
    else:
        raise ValueError('unknown entity: %s', args.entity)


def handle_response(res_string):
    if not res_string:
        return res_string

    res_obj = json.loads(res_string)
    # fixup types as ovirt-vmconsole-proxy-keys expects them
    res_obj['version'] = int(res_obj['version'])
    for con in res_obj.get('consoles', []):
        # fixup: servlet uses 'vmname' to reduce ambiguity;
        # ovirt-vmconsole-* however, expects 'vm'.
        con['vm'] = con['vmname']
        # fixup: to avoid name clashes between VMs
        # .sock suffix is for clarity
        con['console'] = '%s.sock' % con['vmid']

    return json.dumps(res_obj)


def main():
    service.setupLogger()

    logger = logging.getLogger(_LOGGER_NAME)

    try:
        args = parse_args()

        cfg_file = configfile.ConfigFile([
            config.VMCONSOLE_PROXY_HELPER_DEFAULTS,
            config.VMCONSOLE_PROXY_HELPER_VARS,
        ])

        if cfg_file.getboolean('DEBUG') or args.debug:
            logger.setLevel(logging.DEBUG)

        base_url = (
            # debug, emergency override
            os.getenv('OVIRT_VMCONSOLE_ENGINE_BASE_URL') or
            cfg_file.get('ENGINE_BASE_URL')
        )

        logger.debug('using engine base url: %s', base_url)

        enc = make_ticket_encoder(cfg_file)
        data = enc.encode(json.dumps(make_request(args)))
        req = urllib2.Request(
            urlparse.urljoin(base_url, 'services/vmconsole-proxy'),
            data=data,
            headers={
                'Content-Type': 'text/plain',
                'Content-Length': len(data),
            },
        )

        ca_certs = cfg_file.get('ENGINE_CA')
        if not ca_certs:
            logger.warn('Engine CA not configured, '
                        'connecting in insecure mode')
            ca_certs = None

        with urlopen(
            url=req,
            ca_certs=ca_certs,
            verify_host=cfg_file.getboolean('ENGINE_VERIFY_HOST')
        ) as res:
            if res.getcode() != _HTTP_STATUS_CODE_SUCCESS:
                raise RuntimeError(
                    'Engine call failed: code=%d' % res.getcode()
                )
            print(handle_response(res.read()))

    except Exception as ex:
        logger.error('Error: %s', ex)
        logger.debug('Exception', exc_info=True)
        return 1
    else:
        return 0


if __name__ == "__main__":
    sys.exit(main())
