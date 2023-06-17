package com.studiomediatech.opaque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IdentifierTest {

    private Identifier id;

    @BeforeEach
    void fixture() {

        // @formatter:off
		id = Identifier.inRealm("tropian.io")
				   .inSector("scale", "compute")
				   .having("dc", "west1")
				   .having("room", 442)
				   .having("isle", "E")
				   .having("rack", "E5")
				   .build();
		// @formatter:on
    }

    @Test
    void builder_is_created_from_realm() throws Exception {
        assertThat(Identifier.inRealm("example.net")).isNotNull().isInstanceOf(IdentifierBuilder.class);
    }

    @Test
    void builder_is_created_from_sector() throws Exception {
        assertThat(Identifier.inSector("some/path")).isNotNull().isInstanceOf(IdentifierBuilder.class);
    }

    @Test
    void cannot_build_for_only_realm() throws Exception {
        assertThrows(UnbuildableIdentifierException.class, () -> Identifier.inRealm("domain").build());
    }

    @Test
    void cannot_build_for_only_sector() throws Exception {
        assertThrows(UnbuildableIdentifierException.class, () -> Identifier.inSector("path").build());
    }

    @Test
    void builds_identifier_with_realm() throws Exception {
        assertThat(id.realm()).isEqualTo("tropian.io");
    }

    @Test
    void builds_identifier_with_sectors() throws Exception {
        assertThat(id.sectors()).containsExactly("scale", "compute");
    }

    @Test
    void builds_identifier_with_properties() throws Exception {

        assertThat(id.get("dc")).isEqualTo("west1");
        assertThat(id.getNumber("room")).isEqualTo(442);
        assertThat(id.getString("isle")).isEqualTo("E");
        assertThat(id.get("rack")).isEqualTo("E5");

        assertThrows(IllegalStateException.class, () -> id.getNumber("rack"));
        assertThrows(IllegalStateException.class, () -> id.getString("room"));

        // String base32 = id.toBase32();
        // System.out.println(base32);
        //
        // String json = id.toJSON();
        // System.out.println(json);
        //
        // JSONAssert.assertEquals(
        // """
        // {
        // value: 'fhzzehuxqbwsn5tqpfz16h5dcfsgkm5dp7szy7mwcw9sea37q71zg7btr33g655p8w4dectgpf3sa3j7ewu8ramdpc6wkpe',
        // dc: 'west1',
        // room: '442',
        // isle: 'E',
        // rack: 'E5'
        // }
        // """, json, true);
    }

    @Test
    void to_text_is_human_readable() throws Exception {

        assertThat(id.toText()).contains("tropian.io").contains("scale/compute").contains("dc", "west1")
                .contains("room", "442").contains("isle", "E").contains("rack", "E5");

    }

    @Test
    void is_represented_as_base64() throws Exception {

        String encoded = id.toBase64();
        String decoded = new String(Base64.getUrlDecoder().decode(encoded));

        assertThat(encoded).isNotEqualTo(id.toText());
        assertThat(decoded).isEqualTo(id.toText());
    }

    @Test
    void scalar_value_is_parsed() throws Exception {

        Identifier id = Identifier.fromValue(
                "fhzzehuxqbwsn5tqpfz16h5dcfsgkm5dp7szy7mwcw9sea37q71zg7btr33g655p8w4dectgpf3sa3j7ewu8ramdpc6wkpe");

        assertThat(id.realm()).isEqualTo("tropian.io");
        assertThat(id.sectors()).containsExactly("scale", "compute");

        assertThat(id.get("dc")).isEqualTo("west1");
        assertThat(id.getNumber("room")).isEqualTo(442);
        assertThat(id.getString("isle")).isEqualTo("E");
        assertThat(id.get("rack")).isEqualTo("E5");
    }
}
