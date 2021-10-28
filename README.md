# ConnOR

[![ci](https://github.com/helpermethod/connor/actions/workflows/ci.yml/badge.svg)](https://github.com/helpermethod/connor/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/helpermethod/connor/branch/main/graph/badge.svg?token=niYlJRkALi)](https://codecov.io/gh/helpermethod/connor)
[![license](https://badgen.net/badge/license/Apache%20Licence%202.0/blue)](https://github.com/helpermethod/connor/blob/main/LICENSE)

ConnOR, short for **Conn**ect**O**ffset**R**eset, is a command line tool for resetting Kafka Connect source connector offsets.

## Features

* :rocket: Fast startup and low memory footprint
* :package: No external dependencies
* :rainbow: Colorized output

## Installation

### [SDKMAN!](https://sdkman.io/)

```sh
sdk install connor
```

### [Homebrew](https://brew.sh/)

```sh
brew tap helpermethod/homebrew-tap
brew install connor
```

### [Scoop](https://scoop.sh/)

```sh
scoop bucket add helpermethod https://github.com/helpermethod/scoop-helpermethod.git
scopp install connor
```

## Options

### `--bootstrap-servers`

A comma-separated list of Kafka broker URLs.

### `--offset-topic`

The name of the internal topic where Kafka Connect stores its source connector offsets.

### `--connector-name`

The name of the source connector whose offsets you want to reset.

### `--execute`

Executes the reset. Without the option, a dry run is performed. 
