# Connor

[![ci](https://github.com/helpermethod/connor/actions/workflows/ci.yml/badge.svg)](https://github.com/helpermethod/connor/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/helpermethod/connor/branch/main/graph/badge.svg?token=niYlJRkALi)](https://codecov.io/gh/helpermethod/connor)
[![license](https://badgen.net/badge/license/Apache%20Licence%202.0/blue)](https://github.com/helpermethod/connor/blob/main/LICENSE)

Connor (short for: **Conn**ect**O**ffset**R**eset) is commandline tool for resetting Kafka Connect source connector offsets.

# Features

* :rocket: Fast startup time and low memory footprint  
* :package: No external dependencies  
* :rainbow: Colorized output

# Installation

## Via Homebrew

```sh
brew tap helpermethod/homebrew-tap
brew install connor
```

## Via Scoop

```sh
scoop bucket add helpermethod https://github.com/helpermethod/scoop-helpermethod.git
scopp install connor
```

# Usage

:exclamation: Before resetting the offsets for a source connector, make sure to stop the source connector first.
