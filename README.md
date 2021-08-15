# ConnOR

[![ci](https://github.com/helpermethod/connor/actions/workflows/ci.yml/badge.svg)](https://github.com/helpermethod/connor/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/helpermethod/connor/branch/main/graph/badge.svg?token=niYlJRkALi)](https://codecov.io/gh/helpermethod/connor)
[![license](https://badgen.net/badge/license/Apache%20Licence%202.0/blue)](https://github.com/helpermethod/connor/blob/main/LICENSE)

ConnOR, short for **Conn**ect**O**ffset**R**eset, is a command line tool for resetting Kafka Connect source connector offsets.

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

# Configuration

ConnOR accepts 3 mandatory command line arguments

## `--bootstrap-servers`

A comma-separated list of Kafka broker URLs, e.g. `localhost:9092`.

## `--offset-topic`

The name of the internal topic where Kafka Connect stores its source connector offsets.

## `--connector-name`

The name of the source connector to reset.

# Usage

Run `connor` with all 3 mandatory arguments set, e.g.

```sh
connor --bootstrap-servers localhost:9092 --offset-topic docker-connect-offsets --connector-name connect-file-pulse-quickstart-log4j
```

The output should look similar to this.

![image](https://user-images.githubusercontent.com/1562019/129492520-8858de84-243c-418a-939c-03a9666b09f3.png)
