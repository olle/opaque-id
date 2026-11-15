Opaque-Id
=========

[![Build](https://github.com/olle/opaque-id/actions/workflows/build.yml/badge.svg)](https://github.com/olle/opaque-id/actions/workflows/build.yml)

A small helper library for building opaque semantic identifiers.

--------------------------------------------------------------------
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

## Requirements

- Java 25 or newer
- Maven 3.9 or newer, or the bundled Maven Wrapper

## Usage

Build an identifier from a realm, optional sectors, and one or more properties:

```java
Identifier id = Identifier.inRealm("tropian.io")
        .inSector("scale", "compute")
        .having("dc", "west1")
        .having("room", 442)
        .build();
```

Read the identifier components:

```java
id.realm();            // "tropian.io"
id.sectors();          // ["scale", "compute"]
id.getString("dc");    // "west1"
id.getNumber("room");  // 442
```

Serialize it in a number of ways:

```java
id.toText();    // //tropian.io/scale/compute?dc=west1&room=442
id.toValue();   // z-Base32 scalar value
id.toBase32();  // z-Base32 encoded text
id.toBase64();  // URL-safe Base64 encoded text
id.toJSON();    // {"value":"...","dc":"west1","room":"442"}
```

And unpack a scalar value again:

```java
Identifier parsed = Identifier.fromValue(id.toValue());
```

Property keys and values are URL-encoded in the textual representation, so
reserved characters and Unicode are preserved through a round trip.

## Build

```sh
make          # format and verify
make test     # run the tests
make tidy     # apply formatting
make clean    # clean build output
```

The Maven Wrapper can be used directly as well:

```sh
./mvnw verify
```

## License

MIT. See [LICENSE](LICENSE).
