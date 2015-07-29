Brikar Log Analyzer
===================

## Overview

This is a CLI program for analyzing logs implemented using Apache Camel.

Tenets:

* Logs are custom to every application. The logic behind log analysis deserve to be well tested and it is better to do it using the logic written in standard Java and tested by standard JUnit tests.
* Log analyzer should be built on solid foundation.

## How to build

```
mvn clean install
```

## Example endpoints

Local elasticsearch cluster:

```
elasticsearch://local?operation=INDEX&&indexName=logs&indexType=logItem
```

Standard output:

```
stream:file?fileName=/dev/stdout
```

## How to start locally

Unpack elasticsearch distribution on your localhost.

Configure ``./config/elasticsearch.yml", add the following:

```
cluster.name: localhost
```

Start elasticsearch:

```
./bin/elasticsearch
```

Monitor logs, then do query:

```
curl -X GET 'http://127.0.0.1:9200/_search?q=severity:INFO&size=5&pretty=true'
```

```
curl -X GET 'http://127.0.0.1:9200/_search?q=op:Greeting&size=5&pretty=true'
```
