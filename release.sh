#!/bin/bash

# Script to build, deploy and release PatternFly Fritz2.
#
# Prerequisites
#   - Clean git status (no uncommitted changes in branch 'master')
#   - No tag for the specified version
#
# Parameters
#   1. New semantic version number
#
# What it does
#   1. Build branch 'master'
#   2. Bump version to '<version>'
#   3. Commit version change
#   4. Create and push tag 'v<version>'
#   5. GitHub workflow defined in 'release.yml' kicks in



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

if git rev-parse -q --verify "refs/tags/$VERSION" >/dev/null; then
    echo "A tag for '$VERSION' already exists."
    exit 1
fi

git checkout master
if ! git diff --no-ext-diff --quiet --exit-code; then
    echo "Unable to release. You have uncommitted changes in the branch 'master'."
    exit 1
fi



# Build, tag and push
box "Build 'master'"
git pull origin master
./gradlew build || { echo "Build failed" ; exit 1; }

box "Bump to '$VERSION'"
./versionBump.sh "$VERSION"
git commit -am "Bump to $VERSION"

box "Tag and push '$VERSION'"
git tag "$VERSION"
git push --tags origin



# Done
box "  <<--==  PatternFly Fritz2 successfully released  ==-->>  "
