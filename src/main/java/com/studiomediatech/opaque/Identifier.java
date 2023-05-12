package com.studiomediatech.opaque;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Encapsulates an opaque identifier, with the capability for convenient encoding/decoding for many serialization
 * purposes.
 */
public final class Identifier {

    private final String domain;
    private final String path;
    private final Map<String, Object> params;

    Identifier(IdentifierBuilder builder) {
        this.domain = builder.getDomain();
        this.path = builder.getPath();
        this.params = builder.getParamsMap();
    }

    public static IdentifierBuilder withDomain(CharSequence domain) {
        return new IdentifierBuilder(new Domain(domain));
    }

    public static IdentifierBuilder withPath(CharSequence path) {
        return new IdentifierBuilder(new Path(path));
    }

    static record Domain(CharSequence value) {
        public static Domain empty() {
            return new Domain("");
        }
    }

    static record Path(CharSequence value) {
        public static Path empty() {
            return new Path("");
        }
    }

    static record Param(String name, Object value) {
    }

    public Object get(String name) {
        return params.get(name);
    }

    public URI toURI() {
        try {
            return new URI("opid", domain, "/%s".formatted(path), "");
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

}
