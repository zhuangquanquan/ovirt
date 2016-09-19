#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2015 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""sso plugin."""


import gettext
import random
import string

from otopi import constants as otopicons
from otopi import filetransaction, plugin, util

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):
    """sso plugin."""

    client_id = 'ovirt-engine-core'

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        after=(
            oengcommcons.Stages.DB_CONNECTION_AVAILABLE,
        ),
        condition=lambda self: self.environment[oenginecons.CoreEnv.ENABLE],
    )
    def _misc(self):

        def generatePassword():
            rand = random.SystemRandom()
            return ''.join([
                rand.choice(string.ascii_letters + string.digits)
                for i in range(32)
            ])

        result = self.environment[
            oenginecons.EngineDBEnv.STATEMENT
        ].execute(
            statement="""
                select sso_oauth_client_exists(
                    %(client_id)s
                ) as r
            """,
            args=dict(
                client_id=self.client_id,
            ),
        )
        if result[0]['r'] == 0:
            engine_port = self.environment[
                oengcommcons.ConfigEnv.HTTPS_PORT
            ] if self.environment[
                oengcommcons.ConfigEnv.JBOSS_AJP_PORT
            ] else self.environment[
                oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTPS_PORT
            ]
            engine_http_port = self.environment[
                oengcommcons.ConfigEnv.HTTP_PORT
            ] if self.environment[
                oengcommcons.ConfigEnv.JBOSS_AJP_PORT
            ] else self.environment[
                oengcommcons.ConfigEnv.JBOSS_DIRECT_HTTP_PORT
            ]

            client_secret = generatePassword()
            self.environment[
                otopicons.CoreEnv.LOG_FILTER
            ].append(client_secret)

            rc, stdout, stderr = self.execute(
                (
                    oenginecons.FileLocations.OVIRT_ENGINE_CRYPTO_TOOL,
                    'pbe-encode',
                    '--password=env:pass',
                ),
                envAppend={
                    'OVIRT_ENGINE_JAVA_HOME_FORCE': '1',
                    'OVIRT_ENGINE_JAVA_HOME': self.environment[
                        oengcommcons.ConfigEnv.JAVA_HOME
                    ],
                    'OVIRT_JBOSS_HOME': self.environment[
                        oengcommcons.ConfigEnv.JBOSS_HOME
                    ],
                    'pass': client_secret,
                },
            )

            self.environment[oenginecons.EngineDBEnv.STATEMENT].execute(
                statement="""
                    select sso_oauth_register_client(
                        %(client_id)s,
                        %(client_secret)s,
                        %(scope)s,
                        %(certificate)s,
                        %(callback_prefix)s,
                        %(description)s,
                        %(email)s,
                        %(trusted)s,
                        %(notification_callback)s,
                        %(notification_callback_host_protocol)s,
                        %(notification_callback_host_verification)s,
                        %(notification_callback_chain_validation)s
                    )
                """,
                args=dict(
                    client_id=self.client_id,
                    client_secret=stdout[0],
                    scope=' '.join(
                        (
                            'ovirt-app-portal',
                            'ovirt-app-admin',
                            'ovirt-app-api',
                            'ovirt-ext=auth:identity',
                            'ovirt-ext=token:password-access',
                            'ovirt-ext=auth:sequence-priority',
                            'ovirt-ext=token:login-on-behalf',
                            'ovirt-ext=token-info:authz-search',
                            'ovirt-ext=token-info:public-authz-search',
                            'ovirt-ext=token-info:validate',
                            'ovirt-ext=revoke:revoke-all',
                        )
                    ),
                    certificate=(
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_PKI_ENGINE_CERT
                    ),
                    callback_prefix='https://%s:%s/ovirt-engine/' % (
                        self.environment[osetupcons.ConfigEnv.FQDN],
                        engine_port,
                    ),
                    description='oVirt Engine',
                    email='',
                    trusted=True,
                    notification_callback=(
                        'https://localhost:%s/ovirt-engine/'
                        'services/sso-callback'
                    ) % engine_port,
                    notification_callback_host_protocol='TLSv1',
                    notification_callback_host_verification=False,
                    notification_callback_chain_validation=True,
                ),
            )

            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=(
                        oenginecons.FileLocations.
                        OVIRT_ENGINE_SERVICE_CONFIG_SSO
                    ),
                    mode=0o600,
                    owner=self.environment[osetupcons.SystemEnv.USER_ENGINE],
                    enforcePermissions=True,
                    content=(
                        'ENGINE_SSO_CLIENT_ID="{client_id}"\n'
                        'ENGINE_SSO_CLIENT_SECRET="{client_secret}"\n'
                        'ENGINE_SSO_AUTH_URL='
                        '"https://{fqdn}:{port}/ovirt-engine/sso"\n'
                        'ENGINE_SSO_SERVICE_URL='
                        '"https://localhost:{port}/ovirt-engine/sso"\n'
                        'ENGINE_SSO_SERVICE_SSL_VERIFY_HOST=false\n'
                        'ENGINE_SSO_SERVICE_SSL_VERIFY_CHAIN=true\n'
                        'SSO_ENGINE_URL='
                        '"{engine_url_scheme}://{fqdn}:'
                        '{engine_url_port}/ovirt-engine/"\n'
                        '{devenv}'
                    ).format(
                        client_id=self.client_id,
                        client_secret=client_secret,
                        fqdn=(
                            "${ENGINE_FQDN}"
                        ),
                        port=engine_port,
                        devenv=(
                            (
                                'SSO_CALLBACK_PREFIX_CHECK=false\n'
                                if self.environment[
                                    osetupcons.CoreEnv.DEVELOPER_MODE
                                ] else ''
                            )
                        ),
                        engine_url_scheme=(
                            (
                                'http'
                                if self.environment[
                                    osetupcons.CoreEnv.DEVELOPER_MODE
                                ] else 'https'
                            )
                        ),
                        engine_url_port=(
                            (
                                engine_http_port
                                if self.environment[
                                    osetupcons.CoreEnv.DEVELOPER_MODE
                                ] else engine_port
                            )
                        )
                    ),
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
