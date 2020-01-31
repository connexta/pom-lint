# POM Linter
[![Build Status](https://travis-ci.com/connexta/pom-lint.svg?branch=master)](https://travis-ci.com/connexta/pom-lint)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg)](.github/CODE_OF_CONDUCT.md)

Scans pom files to check for missing dependencies.

## Maven Plugin

To build the maven plugin, do:

    mvn clean install

To run the maven plugin, do:

    mvn com.connexta:pom-lint:1.0-SNAPSHOT:lint
