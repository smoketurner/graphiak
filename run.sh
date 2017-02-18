#!/bin/sh

VERSION=`xmllint --xpath "//*[local-name()='project']/*[local-name()='version']/text()" pom.xml`

docker run \
--name graphiak \
--rm \
-e PORT=2003 \
-p 2003:2003 \
-p 8080:8080 \
smoketurner/graphiak:${VERSION}
