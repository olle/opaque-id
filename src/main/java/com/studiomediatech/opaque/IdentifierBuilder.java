package com.studiomediatech.opaque;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.studiomediatech.opaque.Identifier.Property;
import com.studiomediatech.opaque.Identifier.Realm;
import com.studiomediatech.opaque.Identifier.Sector;

/**
 * Builder for {@link Identifier identifiers}, providing an easy-to-use chaining API.
 */
public final class IdentifierBuilder {

    private final Realm realm;
    private final Sector sector;
    private final Set<Property> properties = new LinkedHashSet<>();

    IdentifierBuilder(Realm realm) {
        this.realm = realm;
        this.sector = Sector.empty();
    }

    IdentifierBuilder(Sector sector) {
        this.realm = Realm.empty();
        this.sector = sector;
    }

    private IdentifierBuilder(IdentifierBuilder other, Sector sector) {
        this.realm = other.realm;
        this.sector = sector;
    }

    private IdentifierBuilder(IdentifierBuilder other, Property property) {
        this.realm = other.realm;
        this.sector = other.sector;
        this.properties.addAll(other.properties);
        this.properties.add(property);
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
        return new IdentifierBuilder(this, new Sector(String.join("/", sector)));
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
        return new IdentifierBuilder(this, new Property(key, value));
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
        return new IdentifierBuilder(this, new Property(key, value));
    }

    /**
     * {@return The identifier, built by this builder. }
     *
     * @throws UnbuildableIdentifierException
     *             if no properties have been added.
     */
    public Identifier build() {

        if (properties.isEmpty()) {
            throw new UnbuildableIdentifierException();
        }

        return new Identifier(this);
    }

    Map<String, Object> getParamsMap() {

        var map = new LinkedHashMap<String, Object>(properties.size());

        for (var property : properties) {
            map.put(property.name(), property.value());
        }

        return map;
    }

    String getDomain() {
        return realm.value();
    }

    String getPath() {
        return sector.value();
    }

}
