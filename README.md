# Connect Offset Reset

[![ci](https://github.com/helpermethod/connect-offset-reset/actions/workflows/ci.yml/badge.svg)](https://github.com/helpermethod/connect-offset-reset/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/helpermethod/connect-offset-reset/branch/main/graph/badge.svg?token=niYlJRkALi)](https://codecov.io/gh/helpermethod/connect-offset-reset)
[![license](https://badgen.net/badge/license/Apache%20Licence%202.0/blue)](https://github.com/helpermethod/connect-offset-reset/blob/main/LICENSE)

A commandline tool for resetting Kafka Connect source connector offsets.

# Features

* :rocket: Fast startup time and low memory footprint  
* :package: No external dependencies  
* :rainbow: Colorized output

# Installation

## Via Homebrew

```sh
brew tap helpermethod/homebrew-tap
brew install connect-offset-reset
```

## Via Scoop

```sh
scoop bucket add helpermethod https://github.com/helpermethod/scoop-helpermethod.git
scopp install connect-offset-reset
```

# Usage

:warning: Before running `connect-offset-reset`, make sure to stop the source connector. The easiest way to do this is via its [REST API](https://docs.confluent.io/platform/current/connect/references/restapi.html#put--connectors-(string-name)-pause)
