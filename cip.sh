#!/bin/bash

# (C)an (I) (p)ush?
# Script which checks whether it is safe to push changes.
#
# Runs the following Gradle tasks:
#   - test
#   - ktlintCheck
#   - detekt
#

./gradlew test ktlintCheck detekt
