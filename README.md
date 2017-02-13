Graphiak
========
[![Build Status](https://travis-ci.org/smoketurner/graphiak.svg?branch=master)](https://travis-ci.org/smoketurner/graphiak)
[![Coverage Status](https://coveralls.io/repos/github/smoketurner/graphiak/badge.svg?branch=master)](https://coveralls.io/github/smoketurner/graphiak?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.smoketurner.graphiak/graphiak-application.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.smoketurner.graphiak/graphiak-application/)
[![GitHub license](https://img.shields.io/github/license/smoketurner/graphiak.svg?style=flat-square)](https://github.com/smoketurner/graphiak/tree/master)

Graphiak will listen on a TCP port and batch and store metrics in [Riak TS](http://basho.com/products/riak-ts/).

Installation
------------
To build this code locally, clone the repository then use [Maven](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html) to build the jar:
```
git clone https://github.com/smoketurner/graphiak.git
cd uploader
mvn package
java -jar target/graphiak-application-1.0.0-SNAPSHOT.jar server config.yml
```

The Graphiak service should be listening on port `2003` for metric data and `8080` for API requests, and Dropwizard's administrative interface is available at `/admin` (both of these ports can be changed in the `config.yml` configuration file).


Support
-------

Please file bug reports and feature requests in [GitHub issues](https://github.com/smoketurner/graphiak/issues).


License
-------

Copyright (c) 2017 Smoke Turner, LLC

This library is licensed under the Apache License, Version 2.0.

See http://www.apache.org/licenses/LICENSE-2.0.html or the [LICENSE](LICENSE) file in this repository for the full license text.
