Opaque-Id
=========

A small helper library for building opaque semantic identifiers.

-------------------------------------------------------------------------
## "CYPHER: Ignorance is bliss."

Sometimes, when information needs to cross over boundaries of ownership,
it may be very convenient to provide the option of non-transparency. We've
had great success with this solution, working with friendly scalar values, 
which can be passed around or stored easily, encapsulating unique and
canonical identifiers across distributed systems and service oriented
solutions.

Event-driven systems have the benefit of allowing multiple independent
actors consume and react to information that is published, carrying opaque
identifiers. Those identifiers can be forwarded in any new events, as simple
scalar values - allowing the initial modules or components to unpack and
make the identifying information transparent again.

