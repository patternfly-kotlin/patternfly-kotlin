#!/bin/bash

# Script to bump the version in every file it is referenced.
#
# Parameters
#   1. New semantic version number
#
# What it does
#   1. Replace all occurrences with the new version number



VERSION=$1



# Prerequisites
if [[ "$#" -ne 1 ]]; then
    echo "Illegal number of parameters. Please use $0 <version>"
    exit 1
fi

# See https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
# and https://gist.github.com/rverst/1f0b97da3cbeb7d93f4986df6e8e5695
if ! [[ "$VERSION" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-((0|[1-9][0-9]*|[0-9]*[a-zA-Z-][0-9a-zA-Z-]*)(\.(0|[1-9][0-9]*|[0-9]*[a-zA-Z-][0-9a-zA-Z-]*))*))?(\+([0-9a-zA-Z-]+(\.[0-9a-zA-Z-]+)*))?$ ]]; then
    echo "Illegal semantic version number. Version must apply to https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string"
    exit 1
fi



# Replace versions
sed -i.versionsBackup "s/implementation(\"org\.patternfly:patternfly-fritz2:.*\")/implementation(\"org\.patternfly:patternfly-fritz2:$1\")/" README.md
sed -i.versionsBackup "s/const val version = \".*\"/const val version = \"$1\"/" buildSrc/src/main/kotlin/Build.kt
find . -name "*.versionsBackup" -exec rm {} \;
