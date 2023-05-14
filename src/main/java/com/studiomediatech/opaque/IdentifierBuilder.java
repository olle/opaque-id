package com.studiomediatech.opaque;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.studiomediatech.opaque.Identifier.Domain;
import com.studiomediatech.opaque.Identifier.Param;
import com.studiomediatech.opaque.Identifier.Path;

/**
 * Builder for {@link Identifier identifiers}, providing an easy-to-use chaining API.
 */
public final class IdentifierBuilder {

    private final Domain domain;
    private final Path path;
    private final Set<Param> params = new LinkedHashSet<>();

    IdentifierBuilder(Domain domain) {
        this.domain = domain;
        this.path = Path.empty();
    }

    IdentifierBuilder(Path path) {
        this.domain = Domain.empty();
        this.path = path;
    }

    private IdentifierBuilder(IdentifierBuilder other, Path path) {
        this.domain = other.domain;
        this.path = path;
    }

    private IdentifierBuilder(IdentifierBuilder other, Param param) {
        this.domain = other.domain;
        this.path = other.path;
        this.params.addAll(other.params);
        this.params.add(param);
    }

    /**
     * Appends paths to this builder.
     *
     * @param paths
     *            to append.
     *
     * @return this builder for chaining.
     */
    public IdentifierBuilder withPath(String... paths) {
        return new IdentifierBuilder(this, new Identifier.Path(Stream.of(paths).collect(Collectors.joining("/"))));
    }

    /**
     * Appends a key-value numeric parameter to this builder.
     *
     * @param name
     *            of the parameter
     * @param value
     *            of the parameter
     *
     * @return this builder for chaining.
     */
    public IdentifierBuilder withParam(String name, Number value) {
        return new IdentifierBuilder(this, new Identifier.Param(name, value));
    }

    /**
     * Appends a key-value string parameter to this builder.
     *
     * @param name
     *            of the parameter
     * @param value
     *            of the parameter
     *
     * @return this builder for chaining.
     */
    public IdentifierBuilder withParam(String name, String value) {
        return new IdentifierBuilder(this, new Identifier.Param(name, value));
    }

    /**
     * {@return The identifier, built by this builder. }
     */
    public Identifier build() {

        if (params.isEmpty()) {
            throw new UnbuildableIdentifierException();
        }

        return new Identifier(this);
    }

    Map<String, Object> getParamsMap() {

        Map<String, Object> map = new LinkedHashMap<>(params.size());

        for (var param : params) {
            map.put(param.name(), param.value());
        }

        return map;
    }

    String getDomain() {
        return domain.value().toString();
    }

    String getPath() {
        return path.value().toString();
    }

}
