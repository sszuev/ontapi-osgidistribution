# ONT-API + OWL-API Tools & Formats - OSGI and Distribution.

This is a full **ONT-API** OSGi and standard distribution that includes OWL-API Tools (_owlapi-tools_), 
OWL-API Formats (_owlapi-parsers_, _owlapi-rio_, _owlapi-oboformat_) and the 
original in-memory OWL-API implementation (_owlapi-impl_), which was added for convenience' sake 
and because some its parts related to the reasoner are used by Protege.
The artifact _owlapi-apibinding_ is not included in the distribution.
Please do not confuse it with _net.sourceforge.owlapi:owlapi-osgidistribution_, 
which is an extraneous implementation, that is not based on **ONT-API**. 
See also _ru.avicomp:ontapi-osgi_ which is **ONT-API OSGi and Distribution** and underlies this project.
The project is completed with validation and integration tests.

## License
* Apache License Version 2.0

### Version history and dependencies:

* __1.0.0__: ONT-API-1.4.2-SNAPSHOT (jena 3.12.0, owlapi 5.1.11)
