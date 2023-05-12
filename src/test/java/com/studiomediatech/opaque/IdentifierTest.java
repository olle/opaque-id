package com.studiomediatech.opaque;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;

public class IdentifierTest {

    @Test
    void ensureScaffoldsBuilderWithSystem() throws Exception {
        IdentifierBuilder builder = Identifier.withDomain("net.example");
        assertThat(builder).isNotNull();
    }

    @Test
    void ensureInitiatesBuilderWithPathOnly() throws Exception {
        IdentifierBuilder builder = Identifier.withPath("some/path");
        assertThat(builder).isNotNull();
    }

    @Test
    void ensureBuildsRichOpaqueId() throws Exception {

        var id = Identifier.withDomain("tropian.io") // NOSONAR
                .withPath("scale", "compute") // NOSONAR
                .withParam("dc", "west1") // NOSONAR
                .withParam("room", "442") // NOSONAR
                .withParam("isle", "E") // NOSONAR
                .withParam("rack", "E5") // NOSONAR
                .build();

        assertThat(id.get("dc")).isEqualTo("west1");
        assertThat(id.get("room")).isEqualTo("442");
        assertThat(id.get("isle")).isEqualTo("E");
        assertThat(id.get("rack")).isEqualTo("E5");

        URI uri = id.toURI();
        assertThat(uri.getHost()).isEqualTo("tropian.io");
        assertThat(uri.getPath()).isEqualTo("/scale/compute");
    }
}
