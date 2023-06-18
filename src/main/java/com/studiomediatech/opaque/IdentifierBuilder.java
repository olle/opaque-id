package com.studiomediatech.opaque;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.studiomediatech.opaque.Identifier.Realm;
import com.studiomediatech.opaque.Identifier.Property;
import com.studiomediatech.opaque.Identifier.Sector;

/**
 * Builder for {@link Identifier identifiers}, providing an easy-to-use chaining API.
 */
public final class IdentifierBuilder {

    private final Realm realm;
    private final Sector sector;
    private final Set<Property> properties = new LinkedHashSet<>();

    IdentifierBuilder(Realm domain) {
        this.realm = domain;
        this.sector = Sector.empty();
    }

    IdentifierBuilder(Sector path) {
        this.realm = Realm.empty();
        this.sector = path;
    }

    private IdentifierBuilder(IdentifierBuilder other, Sector path) {
        this.realm = other.realm;
        this.sector = path;
    }

    private IdentifierBuilder(IdentifierBuilder other, Property param) {
        this.realm = other.realm;
        this.sector = other.sector;
        this.properties.addAll(other.properties);
        this.properties.add(param);
    }

    /**
     * Appends sector to this builder.
     *
     * @param sector
     *            to append.
     *
     * @return this builder for chaining.
     */
    public IdentifierBuilder inSector(String... sector) {
        return new IdentifierBuilder(this, new Identifier.Sector(Stream.of(sector).collect(Collectors.joining("/"))));
    }

    /**
     * Appends a key-value numeric property to this builder.
     *
     * @param key
     *            of the property
     * @param value
     *            of the property
     *
     * @return this builder for chaining.
     */
    public IdentifierBuilder having(String key, Number value) {
        return new IdentifierBuilder(this, new Identifier.Property(key, value));
    }

    /**
     * Appends a key-value string property to this builder.
     *
     * @param key
     *            of the property
     * @param value
     *            of the property
     *
     * @return this builder for chaining.
     */
    public IdentifierBuilder having(String key, String value) {
        return new IdentifierBuilder(this, new Identifier.Property(key, value));
    }

    /**
     * {@return The identifier, built by this builder. }
     */
    public Identifier build() {

        if (properties.isEmpty()) {
            throw new UnbuildableIdentifierException();
        }

        return new Identifier(this);
    }

    Map<String, Object> getParamsMap() {

        Map<String, Object> map = new LinkedHashMap<>(properties.size());

        for (var param : properties) {
            map.put(param.name(), param.value());
        }

        return map;
    }

    String getDomain() {
        return realm.value().toString();
    }

    String getPath() {
        return sector.value().toString();
    }

}
