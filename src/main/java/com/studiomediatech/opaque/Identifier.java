package com.studiomediatech.opaque;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.studiomediatech.utils.ZBase32;

/**
 * Encapsulates an opaque identifier, with the capability for convenient encoding/decoding for many serialization
 * purposes.
 */
public final class Identifier {

    private final String domain;
    private final String path;
    private final Map<String, Object> properties;

    Identifier(IdentifierBuilder builder) {
        this.domain = builder.getDomain();
        this.path = builder.getPath();
        this.properties = builder.getParamsMap();
    }

    private Identifier(String realm, String path, Map<String, Object> properties) {
        this.domain = realm;
        this.path = path;
        this.properties = properties;
    }

    /**
     * {@return A new builder starting in the given realm.}
     *
     * @param realm
     *            to start the builder with.
     */
    public static IdentifierBuilder inRealm(String realm) {
        return new IdentifierBuilder(new Realm(realm));
    }

    /**
     * {@return A new builder starting in the given sector.}
     *
     * @param sector
     *            to start the builder with.
     */
    public static IdentifierBuilder inSector(String sector) {
        return new IdentifierBuilder(new Sector(sector));
    }

    /**
     * Encapsulates an identifier realm.
     */
    static record Realm(String value) {
        public static Realm empty() {
            return new Realm("");
        }
    }

    /**
     * Encapsulates an identifier path.
     */
    static record Sector(String value) {
        public static Sector empty() {
            return new Sector("");
        }
    }

    /**
     * Encapsulates an identifier property.
     */
    static record Property(String name, Object value) {
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
     */
    public String getString(String key) {

        var value = properties.get(key);

        if (!(value instanceof String)) {
            throw new IllegalStateException();
        }

        return (String) value;
    }

    /**
     * {@return Retrieves the identifier property numeric value, for the given name.}
     *
     * @param name
     *            a key identifying the value.
     */
    public Number getNumber(String name) {
        try {
            return (Number) properties.get(name);
        } catch (ClassCastException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private URI toURI() {
        try {
            return new URI(null, domain, "/%s".formatted(path), buildURIQuery(properties), null);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private String buildURIQuery(Map<String, Object> params) {
        return params.entrySet().stream().map(this::mapEntryToParam).collect(Collectors.joining("&"));
    }

    private String mapEntryToParam(Map.Entry<String, Object> entry) {
        return "%s=%s".formatted(entry.getKey(), entry.getValue());
    }

    /**
     * {@return The text representation of this identifier.}
     */
    public String toText() {
        return toURI().toString();
    }

    public String toValue() {
        return "";
    }

    /**
     * {@return The JSON representation of this identifier.}
     */
    public String toJSON() {
        return "{\"value\":\"%s\",%s}".formatted(toBase32(), buildJSON(properties));
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

        String path = uri.getPath();

        if (path.charAt(0) == '/') {
            path = path.substring(1);
        }

        return new Identifier(uri.getAuthority(), path, buildPropertyMap(uri.getQuery()));
    }

    private static Map<String, Object> buildPropertyMap(String query) {

        Map<String, Object> map = new LinkedHashMap<>();

        String[] pairs = query.split(Pattern.quote("&"));

        for (var pair : pairs) {

            String[] parts = pair.split(Pattern.quote("="));

            String key = parts[0];

            try {
                long longValue = Long.parseLong(parts[1]);
                try {
                    int intValue = Math.toIntExact(longValue);
                    map.put(key, Integer.valueOf(intValue));
                } catch (ArithmeticException ex) {
                    map.put(key, Long.valueOf(longValue));
                }
            } catch (NumberFormatException ex) {
                map.put(key, parts[1]);
            }
        }

        return map;
    }

    public String realm() {
        return this.domain;
    }

    public Collection<String> sectors() {
        return List.of(this.path.split(Pattern.quote("/")));
    }

}
