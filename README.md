# ConnOR

[![ci](https://github.com/helpermethod/connor/actions/workflows/ci.yml/badge.svg)](https://github.com/helpermethod/connor/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/helpermethod/connor/branch/main/graph/badge.svg?token=niYlJRkALi)](https://codecov.io/gh/helpermethod/connor)
[![license](https://badgen.net/badge/license/Apache%20Licence%202.0/blue)](https://github.com/helpermethod/connor/blob/main/LICENSE)

ConnOR, short for **Conn**ect**O**ffset**R**eset, is a command line tool for resetting Kafka Connect source connector offsets.

# Motivation

When I've started using Kafka Connect in a project, there were a lot of resources on
running Kafka Connect, but almost no resources on resetting Kafka Connect source connector offsets,
which is a thing you are doing **alot** in the beginning.

Thankfully I've stumbled over @rmoff's great blog post [Reset Kafka Connect Source Connector Offsets](https://rmoff.net/2019/08/15/reset-kafka-connect-source-connector-offsets/)
which describes the process of resetting source connector offsets in great detail. After applying it manually a few times,
I've decided to encode these manual steps into a commandline tool.

# Features

* :rocket: Fast startup time and low memory footprint
* :package: No external dependencies
* :rainbow: Colorized output

# Installation

## Via [SDKMAN!](https://sdkman.io/)

```sh
sdk install connor
```

## Via [Homebrew](https://brew.sh/)

```sh
brew tap helpermethod/homebrew-tap
brew install connor
```

## Via [Scoop](https://scoop.sh/)

```sh
scoop bucket add helpermethod https://github.com/helpermethod/scoop-helpermethod.git
scopp install connor
```

# Options

## `--bootstrap-servers`

A comma-separated list of Kafka broker URLs.

## `--offset-topic`

The name of the internal topic where Kafka Connect stores its source connector offsets.

## `--connector-name`

The name of the source connector whose offsets you want to reset.

## `--execute`

Executes the reset. Without this flag, a dry run is performed. 
