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

    /**
     * {@return A new builder starting with the given domain.}
     *
     * @param domain
     *            to start the builder with.
     */
    public static IdentifierBuilder withDomain(String domain) {
        return new IdentifierBuilder(new Domain(domain));
    }

    /**
     * {@return A new builder starting with the given path.}
     *
     * @param path
     *            to start the builder with.
     */
    public static IdentifierBuilder withPath(String path) {
        return new IdentifierBuilder(new Path(path));
    }

    /**
     * Encapsulates an identifier domain.
     */
    static record Domain(String value) {
        public static Domain empty() {
            return new Domain("");
        }
    }

    /**
     * Encapsulates an identifier path.
     */
    static record Path(String value) {
        public static Path empty() {
            return new Path("");
        }
    }

    /**
     * Encapsulates an identifier parameter, a key-value property.
     */
    static record Param(String name, Object value) {
    }

    /**
     * {@return Returns the identifier parameter object value, for the given name.}
     *
     * @param name
     *            a key identifying the value.
     */
    public Object get(String name) {
        return params.get(name);
    }

    /**
     * {@return Retrieves the identifier parameter string value, for the given name.}
     *
     * @param name
     *            a key identifying the value.
     */
    public String getString(String name) {
        return params.get(name).toString();
    }

    /**
     * {@return Retrieves the identifier parameter numeric value, for the given name.}
     *
     * @param name
     *            a key identifying the value.
     */
    public Number getNumber(String name) {
        try {
            return (Number) params.get(name);
        } catch (ClassCastException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@return The URI representation of this identifier.}
     */
    public URI toURI() {
        try {
            return new URI(null, domain, "/%s".formatted(path), buildURIQuery(params), null);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private String buildURIQuery(Map<String, Object> params) {
        return params.entrySet().stream().map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    /**
     * {@return The text representation of this identifier.}
     */
    public String toText() {
        return toURI().toString();
    }

    /**
     * {@return The JSON representation of this identifier.}
     */
    public String toJSON() {
        return "{%s}".formatted(buildJSON(params));
    }

    private String buildJSON(Map<String, Object> params) {
        return params.entrySet().stream().map(entry -> "\"%s\":\"%s\"".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(","));
    }

    /**
     * {@return The z-Base32 encoded string representation of this identifier.}
     */
    public String toBase32() {
        return ZBase32.encode(toURI().toString());
    }

    /**
     * {@return The Base64 encoded string representation of this identifier.}
     */
    public String toBase64() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(toURI().toASCIIString().getBytes());
    }

}
