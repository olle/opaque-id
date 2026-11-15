package com.studiomediatech.opaque;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.studiomediatech.utils.ZBase32;

/**
 * Encapsulates an opaque identifier, with the capability for convenient encoding/decoding for many serialization
 * purposes.
 */
public final class Identifier {

    private static final String VALUE_KEY = "value";

    private final String domain;
    private final String path;
    private final Map<String, Object> properties;

    Identifier(IdentifierBuilder builder) {
        this(builder.getDomain(), builder.getPath(), builder.getParamsMap());
    }

    private Identifier(String domain, String path, Map<String, Object> properties) {
        this.domain = Objects.requireNonNull(domain, "domain");
        this.path = Objects.requireNonNull(path, "path");
        this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(properties));
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, path, properties);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Identifier other && Objects.equals(domain, other.domain)
                && Objects.equals(path, other.path) && Objects.equals(properties, other.properties);
    }

    /**
     * {@return A new builder starting in the given realm.}
     *
     * @param realm
     *            to start the builder with.
     */
    public static IdentifierBuilder inRealm(String realm) {
        if (realm == null || realm.isBlank()) {
            throw new IllegalArgumentException("Realm must not be blank");
        }
        return new IdentifierBuilder(new Realm(realm));
    }

    /**
     * {@return A new builder starting in the given sector.}
     *
     * @param sector
     *            to start the builder with.
     */
    public static IdentifierBuilder inSector(String sector) {
        return new IdentifierBuilder(new Sector(Objects.requireNonNull(sector, "sector")));
    }

    /**
     * Encapsulates an identifier realm.
     */
    static record Realm(String value) {
        Realm {
            Objects.requireNonNull(value, "value");
        }

        static Realm empty() {
            return new Realm("");
        }
    }

    /**
     * Encapsulates an identifier path.
     */
    static record Sector(String value) {
        Sector {
            Objects.requireNonNull(value, "value");
        }

        static Sector empty() {
            return new Sector("");
        }
    }

    /**
     * Encapsulates an identifier property.
     */
    static record Property(String name, Object value) {
        Property {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Property name must not be blank");
            }
            Objects.requireNonNull(value, "value");
        }
    }

    /**
     * {@return Returns the identifier property object value, for the given name.}
     *
     * @param key
     *            a key identifying the value.
     */
    public Object get(String key) {
        return properties.get(key);
    }

    /**
     * {@return Retrieves the identifier property string value, for the given name.}
     *
     * @param key
     *            a key identifying the value.
     *
     * @throws IllegalStateException
     *             if the property is missing or not a string.
     */
    public String getString(String key) {
        if (properties.get(key) instanceof String value) {
            return value;
        }
        throw new IllegalStateException("Property '%s' is not a string".formatted(key));
    }

    /**
     * {@return Retrieves the identifier property numeric value, for the given name.}
     *
     * @param name
     *            a key identifying the value.
     *
     * @throws IllegalStateException
     *             if the property is missing or not a number.
     */
    public Number getNumber(String name) {
        if (properties.get(name) instanceof Number value) {
            return value;
        }
        throw new IllegalStateException("Property '%s' is not a number".formatted(name));
    }

    private URI toURI() {
        try {
            var base = new URI(null, domain, "/" + path, null, null);
            var query = buildURIQuery();
            return URI.create(query.isEmpty() ? base.toASCIIString() : base.toASCIIString() + "?" + query);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid identifier components", e);
        }
    }

    private String buildURIQuery() {
        return properties.entrySet().stream()
                .map(entry -> "%s=%s".formatted(encode(entry.getKey()), encode(String.valueOf(entry.getValue()))))
                .collect(Collectors.joining("&"));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    /**
     * {@return The text representation of this identifier.}
     */
    public String toText() {
        return toURI().toString();
    }

    /**
     * {@return The string value representation (z-Base32) of this identifier.}
     */
    public String toValue() {
        return toBase32();
    }

    /**
     * {@return The JSON representation of this identifier.}
     */
    public String toJSON() {
        return toJSON(false);
    }

    /**
     * Returns the JSON representation of this identifier.
     *
     * @param pretty
     *            {@code true} if the JSON representation should be pretty printed, {@code false} if not.
     *
     * @return The JSON representation of this identifier.
     */
    public String toJSON(boolean pretty) {

        var entries = new LinkedHashMap<String, Object>(properties.size() + 1);
        entries.put(VALUE_KEY, toBase32());
        entries.putAll(properties);

        var delimiter = pretty ? ",\n  " : ",";
        var prefix = pretty ? "{\n  " : "{";
        var suffix = pretty ? "\n}\n" : "}";

        return entries.entrySet().stream().map(entry -> jsonEntry(entry, pretty))
                .collect(Collectors.joining(delimiter, prefix, suffix));
    }

    private static String jsonEntry(Map.Entry<String, Object> entry, boolean pretty) {
        var separator = pretty ? "\": \"" : "\":\"";
        return "\"%s%s%s\"".formatted(escapeJson(entry.getKey()), separator,
                escapeJson(String.valueOf(entry.getValue())));
    }

    private static String escapeJson(String value) {
        var escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 0x20) {
                        escaped.append("\\u%04x".formatted((int) c));
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        return escaped.toString();
    }

    /**
     * {@return The z-Base32 encoded string representation of this identifier.}
     */
    public String toBase32() {
        return ZBase32.encode(toURI().toASCIIString());
    }

    /**
     * {@return The Base64 encoded string representation of this identifier.}
     */
    public String toBase64() {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(toURI().toASCIIString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates an identifier from the string standard representation as described in the {@link #toValue()} or
     * {@link #toBase32()} methods.
     *
     * @param value
     *            string that specifies an identifier.
     *
     * @return an identifier with the specified value.
     */
    public static Identifier fromValue(String value) {
        return Identifier.fromURI(URI.create(ZBase32.decode(value)));
    }

    private static Identifier fromURI(URI uri) {

        var path = Objects.requireNonNullElse(uri.getPath(), "");

        if (!path.isEmpty() && path.charAt(0) == '/') {
            path = path.substring(1);
        }

        return new Identifier(uri.getAuthority(), path, buildPropertyMap(uri.getRawQuery()));
    }

    private static Map<String, Object> buildPropertyMap(String query) {

        var map = new LinkedHashMap<String, Object>();

        if (query == null || query.isBlank()) {
            return map;
        }

        for (var pair : query.split("&")) {

            if (pair.isEmpty()) {
                continue;
            }

            var parts = pair.split("=", 2);
            var key = decode(parts[0]);
            var value = parts.length == 2 ? decode(parts[1]) : "";

            map.put(key, parseValue(value));
        }

        return map;
    }

    private static Object parseValue(String value) {

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ignored) {
            // Not an integer
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            // Not a long
        }

        return value;
    }

    /**
     * {@return The realm string representation of this identifier.}
     */
    public String realm() {
        return this.domain;
    }

    /**
     * {@return The canonical sectors as a collection of strings for this identifier.}
     */
    public Collection<String> sectors() {
        return List.of(this.path.split("/"));
    }

    @Override
    public String toString() {
        return toText();
    }

}
