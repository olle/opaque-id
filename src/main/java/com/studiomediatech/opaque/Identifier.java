package com.studiomediatech.opaque;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

import com.studiomediatech.utils.ZBase32;

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

    public static IdentifierBuilder withDomain(String domain) {
        return new IdentifierBuilder(new Domain(domain));
    }

    public static IdentifierBuilder withPath(String path) {
        return new IdentifierBuilder(new Path(path));
    }

    static record Domain(String value) {
        public static Domain empty() {
            return new Domain("");
        }
    }

    static record Path(String value) {
        public static Path empty() {
            return new Path("");
        }
    }

    static record Param(String name, Object value) {
    }

    public Object get(String name) {
        return params.get(name);
    }

    public String getString(String name) {
        return params.get(name).toString();
    }

    public Number getNumber(String name) {
        try {
            return (Number) params.get(name);
        } catch (ClassCastException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public URI toURI() {
        try {
            return new URI(null, domain, "/%s".formatted(path), buildQuery(params), null);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private String buildQuery(Map<String, Object> params) {
        return params.entrySet().stream().map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    public String toText() {
        return toURI().toString();
    }

    public String toJSON() {
        return "{%s}".formatted(buildJSON(params));
    }

    private String buildJSON(Map<String, Object> params) {
        return params.entrySet().stream().map(entry -> "\"%s\":\"%s\"".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(","));
    }

    public String toBase32() {
        return ZBase32.encode(toURI().toString());
    }

    public String toBase64() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(toURI().toASCIIString().getBytes());
    }

}
