#!/usr/bin/python

# Copyright 2014-2016 Red Hat, Inc.

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#    http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


"""
The oVirt downstream branded documentation is typically installed
into the engine and is then used to supply context-sensitive help
throughout the application.

The engine relies on "helptags" (currently in HelpTag.java) to map
dialog boxes to help URLs. This mapping is very simple and lives in
a Windows-style ini file. Example:
    [helptags]
    new_vm=Admin_Guide/How_to_create_a_new_vm.html
    destroy_vm=Admin_Guide/How_to_destroy_a_new_vm.html

########################################################################
########################################################################

This is a utility script that can do two things:

1. It can generate a new empty helptag mapping file
   (not very useful on its own) -- "template mode"

2. It can take an existing mapping file and compare
   it to the existing helptags in engine code. It then
   generates a new updated file. This file is then
   typically filled in with new help URLs for and new
   helptags -- "update mode"

Example usage for update mode:

helptag.py --type userportal --command update --load 10-userportal-en-US.ini

Note that the loaded file is not updated in-place. The updated file is
printed to stdout.

"""


import argparse
import os
import re
import sys


COMMAND_UPDATE = 'update'
COMMAND_TEMPLATE = 'template'
TYPE_WEBADMIN = 'webadmin'
TYPE_USERPORTAL = 'userportal'
TYPE_COMMON = 'common'
TYPE_UNKNOWN = 'unknown'
HELPTAG_SECTION = '[helptags]'
DEFAULT_FILE = (
    'frontend/webadmin/modules/uicommonweb/src/main/java/org/ovirt/'
    'engine/ui/uicommonweb/help/HelpTag.java'
)

__RE_HELPTAG = re.compile(
    flags=re.VERBOSE,
    pattern=r"""
        \s*
        [^\."\(@]+
        \(
            \s*
            "(?P<name>[^"]+)"
            \s*,\s*
            HelpTagType\.(?P<type>%s|%s|%s|%s)
            \s*
            (
                ,\s*
                    "(?P<comment>.*)"
                \s*
            )?
        \)
        \s*
    """
    %
    (
        TYPE_WEBADMIN.upper(), TYPE_USERPORTAL.upper(), TYPE_COMMON.upper(),
        TYPE_UNKNOWN.upper(),
    )
)


def loadTagsFromMappingFile(file):

    ret = {}  # { tagname => (comment, url) }
    state = "HEADER"
    comment = ''

    with open(file, 'r') as f:
        for line in f.readlines():

            if state == "HEADER":
                if line.startswith(HELPTAG_SECTION):
                    state = "COMMENT"
                else:
                    raise RuntimeError(
                        "Invalid ini file. Expected section begin. Found '%s'"
                        % line
                    )

            elif state == "COMMENT":
                if not line.strip():
                    pass
                elif line.startswith(";"):
                    comment = line[2:]
                    state = "TAG"

            elif state == "TAG":
                (tag, url) = line.split("=")
                ret[tag] = (comment, url.strip())
                state = "COMMENT"

            else:
                raise RuntimeError("invalid state: " + state)

    return ret


def loadTagsFromCodebase(filename, type):
    """
    look for help tags in the source code.
    """
    # { tagname => (comment, url) }
    tags = {}

    if filename.endswith('.java') and os.path.isfile(filename):
        with open(filename, 'r') as f:
            for line in f:
                comment = ""
                m = __RE_HELPTAG.match(line)
                if m:
                    if (
                        m.group("type") == type.upper() or
                        m.group("type") == TYPE_COMMON.upper()
                    ):
                        name = m.group("name")
                        if m.group("comment"):
                            comment = m.group("comment")
                        tags[name] = (comment, '')  # placeholder for URL
    return tags


def produceTemplate(codebaseTags, mappingFileTags):
    newTags = {}

    for tag in codebaseTags:

        # if we found that tag in the mapping file, save the URL
        # this is basically the most important part :)

        (newComment, junk) = codebaseTags[tag]

        if tag in mappingFileTags:
            (oldComment, oldUrl) = mappingFileTags[tag]
            newTags[tag] = (newComment, oldUrl)
        else:
            # this is a new tag! just set the URL to ''
            newTags[tag] = (newComment, '')

    print(HELPTAG_SECTION + "\n")

    for tag in sorted(set(newTags)):
        (comment, url) = newTags[tag]
        spacer = ('' if comment == '' else ' ')
        print(";%s%s\n%s=%s\n" % (spacer, comment, tag, url))


def main():
    ret = 1

    parser = argparse.ArgumentParser(
        description=(
            'Compare code help tags to help mapping files, '
            'or produce template of mapping files.'
        ),
    )
    parser.add_argument(
        '--type',
        metavar='COMMAND',
        dest='type',
        choices=[TYPE_WEBADMIN, TYPE_USERPORTAL],
        help='Type (%(choices)s)',
        required=True
    )
    parser.add_argument(
        '--sourcefile',
        metavar='FILE',
        dest='sourcefile',
        default=DEFAULT_FILE,
        help='the source code file to scan',
    )
    parser.add_argument(
        '--command',
        metavar='COMMAND',
        dest='command',
        default=COMMAND_UPDATE,
        choices=[COMMAND_UPDATE, COMMAND_TEMPLATE],
        help='Command (%(choices)s)',
    )
    parser.add_argument(
        '--load',
        metavar='FILE',
        dest='load',
        default='',
        help='the existing ini file to load',
    )
    args = parser.parse_args()

    codebaseTags = loadTagsFromCodebase(args.sourcefile, args.type)

    if args.command == COMMAND_TEMPLATE:
        mappingFileTags = {}
        produceTemplate(codebaseTags, mappingFileTags)
    elif args.command == COMMAND_UPDATE:
        mappingFileTags = loadTagsFromMappingFile(args.load)
        produceTemplate(codebaseTags, mappingFileTags)

    else:
        raise RuntimeError("Invalid command '%s'" % args.command)

    sys.exit(ret)


if __name__ == "__main__":
    main()


# vim: expandtab tabstop=4 shiftwidth=4
