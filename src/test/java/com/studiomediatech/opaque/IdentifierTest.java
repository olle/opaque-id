package com.studiomediatech.opaque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.studiomediatech.utils.ZBase32;

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
    void equals_for_same_realm_sector_and_properties() throws Exception {

        Identifier other = Identifier.inRealm("tropian.io").inSector("scale", "compute").having("dc", "west1")
                .having("room", 442).having("isle", "E").having("rack", "E5").build();

        assertThat(other).isNotSameAs(id);
        assertThat(other).isEqualTo(id);
    }

    @Test
    void unequal_for_different_realm() throws Exception {

        Identifier other = Identifier.inRealm("xtropian.io").inSector("scale", "compute").having("dc", "west1")
                .having("room", 442).having("isle", "E").having("rack", "E5").build();

        assertThat(other).isNotEqualTo(id);
    }

    static Stream<Arguments> sectors() {
        return Stream.of(Arguments.of("xscale", "compute"), Arguments.of("scale", "xcompute"));
    }

    @ParameterizedTest
    @MethodSource("sectors")
    void unequal_for_different_sector(String key, String value) throws Exception {

        Identifier other = Identifier.inRealm("tropian.io").inSector(key, value).having("dc", "west1")
                .having("room", 442).having("isle", "E").having("rack", "E5").build();

        assertThat(other).isNotEqualTo(id);
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

        // String json = id.toJSON(true);
        // System.out.println(json);
        //
        // JSONAssert.assertEquals(
        // """
        // {
        // value:
        // 'fhzzehuxqbwsn5tqpfz16h5dcfsgkm5dp7szy7mwcw9sea37q71zg7btr33g655p8w4dectgpf3sa3j7ewu8ramdpc6wkpe',
        // dc: 'west1',
        // room: '442',
        // isle: 'E',
        // rack: 'E5'
        // }
        // """, json, true);
    }

    @Test
    void to_text_is_human_readable() throws Exception {

        String text = id.toText();

        assertThat(text).contains("tropian.io").contains("scale/compute").contains("dc", "west1")
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
    void is_represented_as_zbase32() throws Exception {

        String encoded = id.toBase32();
        String decoded = ZBase32.decode(encoded);

        assertThat(encoded).isNotEqualTo(id.toText());
        assertThat(decoded).isEqualTo(id.toText());
    }

    @Test
    void is_representated_as_scalar_value() throws Exception {

        String value = id.toValue();

        assertThat(value).isEqualTo(
                "fhzzehuxqbwsn5tqpfz16h5dcfsgkm5dp7szy7mwcw9sea37q71zg7btr33g655p8w4dectgpf3sa3j7ewu8ramdpc6wkpe");
    }

    @Test
    void scalar_value_is_parsed() throws Exception {

        Identifier parsedId = Identifier.fromValue(
                "fhzzehuxqbwsn5tqpfz16h5dcfsgkm5dp7szy7mwcw9sea37q71zg7btr33g655p8w4dectgpf3sa3j7ewu8ramdpc6wkpe");

        assertThat(parsedId).isEqualTo(this.id);

        assertThat(parsedId.realm()).isEqualTo("tropian.io");
        assertThat(parsedId.sectors()).containsExactly("scale", "compute");

        assertThat(parsedId.get("dc")).isEqualTo("west1");
        assertThat(parsedId.getNumber("room")).isEqualTo(442);
        assertThat(parsedId.getString("isle")).isEqualTo("E");
        assertThat(parsedId.get("rack")).isEqualTo("E5");
    }
}
