package com.studiomediatech.opaque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class IdentifierTest {

    @Test
    void ensureScaffoldsBuilderWithSystem() throws Exception {
        assertThat(Identifier.withDomain("example.net")).isNotNull();
    }

    @Test
    void ensureInitiatesBuilderWithPathOnly() throws Exception {
        assertThat(Identifier.withPath("some/path")).isNotNull();
    }

    @Test
    void ensureThrowsUnbuildableOnOnlyDomain() throws Exception {
        assertThrows(UnbuildableIdentifierException.class, () -> Identifier.withDomain("domain").build());
    }

    @Test
    void ensureThrowsUnbuildableOnOnlyPath() throws Exception {
        assertThrows(UnbuildableIdentifierException.class, () -> Identifier.withPath("path").build());
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

        assertThat(id.toText()).isEqualTo("//tropian.io/scale/compute?dc=west1&room=442&isle=E&rack=E5");

        assertThat(id.get("dc")).isEqualTo("west1");
        assertThat(id.get("room")).isEqualTo("442");
        assertThat(id.get("isle")).isEqualTo("E");
        assertThat(id.get("rack")).isEqualTo("E5");

        assertThrows(IllegalStateException.class, () -> id.getNumber("rack"));

        URI uri = id.toURI();
        System.out.println(uri.toASCIIString());
        System.out.println(uri.toString());
        assertThat(uri.getHost()).isEqualTo("tropian.io");
        assertThat(uri.getPath()).isEqualTo("/scale/compute");
        assertThat(uri.getQuery()).isEqualTo("dc=west1&room=442&isle=E&rack=E5");

        String base64 = id.toBase64();
        System.out.println(base64);

        String base32 = id.toBase32();
        System.out.println(base32);

        String json = id.toJSON();
        System.out.println(json);
        JSONAssert.assertEquals("""
                {
                  dc: 'west1',
                  room: '442',
                  isle: 'E',
                  rack: 'E5'
                }
                """, json, true);
    }
}
