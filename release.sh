#!/bin/bash

# Script to release PatternFly Kotlin.
#
# Prerequisites
#   - Clean git status (no uncommitted changes in branch 'main')
#   - No tag for the specified version
#
# Parameters
#   1. New semantic version number
#
# What it does
#   1. Build branch 'main'
#   2. Bump version to '<version>'
#   3. Commit version change
#   4. Create and push tag 'v<version>'
#      By pushing the tag, the GitHub actions
#      'release' and 'apidoc' kick in.



VERSION=$1
VERSION_TAG=v$1



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

if git rev-parse -q --verify "refs/tags/$VERSION_TAG" >/dev/null; then
    echo "A tag for '$VERSION_TAG' already exists."
    exit 1
fi

printf "\n# Check clean status\n\n"
git checkout main
if ! git diff --no-ext-diff --quiet --exit-code; then
    echo "Unable to release. You have uncommitted changes in the branch 'main'."
    exit 1
fi

printf "\n\n\n# Build main\n\n"
git pull origin main
./gradlew build || { echo "Build failed" ; exit 1; }

printf "\n\n\n# Bump to %s\n\n" "$VERSION"
sed -E -i.versionsBackup "s/\"org.patternfly:patternfly-kotlin:.*\"/\"patternfly-kotlin:$VERSION\"/" README.md
sed -i.versionsBackup "s/^version = \".*\"$/version = \"$VERSION\"/" build.gradle.kts
find . -name "*.versionsBackup" -exec rm {} \;
git commit -am "Bump to $VERSION"
git push origin main

printf "\n\n\n# Tag and push %s\n\n" "$VERSION_TAG"
git tag "$VERSION_TAG"
git push --tags origin

printf "\n\n\n<<--==  PatternFly Kotlin %s successfully released  ==-->>\n\n" "$VERSION_TAG"
