# ONT-API + OWL-API Tools & Formats - OSGI and Distribution.

This is a full **ONT-API** OSGi and standard distribution which includes:
- [ONT-API](https://github.com/owlcs/ont-api) - an [OWL-API](https://github.com/owlcs/owlapi) alternative implementation, that is based on [Apache Jena](https://github.com/apache/jena)
- OWL-API Formats (_owlapi-parsers_, _owlapi-rio_, _owlapi-oboformat_)
- OWL-API Tools (_owlapi-tools_)
- In the profile `withDefaultImpl` there is also the original in-memory OWL-API implementation (_owlapi-impl_), for testing and debugging 

The artifact _net.sourceforge.owlapi:owlapi-apibinding_ is not included in the distribution. Instead, the access point to manager instances is `com.github.owlcs.ontapi.OntManagers` from ONT-API.
Please do not confuse this distribution with _net.sourceforge.owlapi:owlapi-osgidistribution_, 
which is an extraneous implementation, that is not based on **ONT-API**. 

The project is completed with validation and integration tests.

## Requirements
* Java8

## License
* Apache License Version 2.0

## Installation

```
$ git clone https://github.com/owlcs/ont-api.git
$ cd ont-api
$ mvn clean install
$ cd ..
$ git clone https://github.com/sszuev/ontapi-osgidistribution.git
$ cd ontapi-osgidistribution
$ mvn clean install
```

## Version history and dependencies:

* __1.0.0-SNAPSHOT__: ONT-API-2.0.0-SNAPSHOT (jena 3.12.0, owlapi 5.1.11)
