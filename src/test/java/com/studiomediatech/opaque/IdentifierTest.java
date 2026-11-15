package com.studiomediatech.opaque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

import com.studiomediatech.utils.ZBase32;

class IdentifierTest {

    private static final String VALUE = "fhzzehuxqbwsn5tqpfz16h5dcfsgkm5dp7szy7mwcw9sea37q71zg7btr33g655p8w4dectgpf3sa3j7ewu8ramdpc6wkpe";

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
    void equals_for_same_realm_sector_and_properties() {

        Identifier other = Identifier.inRealm("tropian.io").inSector("scale", "compute").having("dc", "west1")
                .having("room", 442).having("isle", "E").having("rack", "E5").build();

        assertThat(other).isNotSameAs(id);
        assertThat(other).isEqualTo(id);
        assertThat(other).hasSameHashCodeAs(id);
    }

    @Test
    void unequal_for_different_realm() {

        Identifier other = Identifier.inRealm("xtropian.io").inSector("scale", "compute").having("dc", "west1")
                .having("room", 442).having("isle", "E").having("rack", "E5").build();

        assertThat(other).isNotEqualTo(id);
    }

    static Stream<Arguments> sectors() {
        return Stream.of(Arguments.of("xscale", "compute"), Arguments.of("scale", "xcompute"));
    }

    @ParameterizedTest
    @MethodSource("sectors")
    void unequal_for_different_sector(String key, String value) {

        Identifier other = Identifier.inRealm("tropian.io").inSector(key, value).having("dc", "west1")
                .having("room", 442).having("isle", "E").having("rack", "E5").build();

        assertThat(other).isNotEqualTo(id);
    }

    @Test
    void builder_is_created_from_realm() {
        assertThat(Identifier.inRealm("example.net")).isNotNull().isInstanceOf(IdentifierBuilder.class);
    }

    @Test
    void builder_is_created_from_sector() {
        assertThat(Identifier.inSector("some/path")).isNotNull().isInstanceOf(IdentifierBuilder.class);
    }

    @Test
    void cannot_build_without_properties() {
        assertThatExceptionOfType(UnbuildableIdentifierException.class)
                .isThrownBy(() -> Identifier.inRealm("domain").build());
    }

    @Test
    void cannot_build_from_only_sector() {
        assertThatExceptionOfType(UnbuildableIdentifierException.class)
                .isThrownBy(() -> Identifier.inSector("path").build());
    }

    @Test
    void rejects_blank_realm() {
        assertThatIllegalArgumentException().isThrownBy(() -> Identifier.inRealm(" "))
                .withMessage("Realm must not be blank");
    }

    @Test
    void rejects_blank_property_name() {
        assertThatIllegalArgumentException().isThrownBy(() -> Identifier.inRealm("domain").having(" ", "value"))
                .withMessage("Property name must not be blank");
    }

    @Test
    void builds_identifier_with_realm() {
        assertThat(id.realm()).isEqualTo("tropian.io");
    }

    @Test
    void builds_identifier_with_sectors() {
        assertThat(id.sectors()).containsExactly("scale", "compute");
    }

    @Test
    void builds_identifier_with_properties() {

        assertThat(id.get("dc")).isEqualTo("west1");
        assertThat(id.getNumber("room")).isEqualTo(442);
        assertThat(id.getString("isle")).isEqualTo("E");
        assertThat(id.get("rack")).isEqualTo("E5");

        assertThatIllegalStateException().isThrownBy(() -> id.getNumber("rack"))
                .withMessage("Property 'rack' is not a number");
        assertThatIllegalStateException().isThrownBy(() -> id.getString("room"))
                .withMessage("Property 'room' is not a string");
        assertThatIllegalStateException().isThrownBy(() -> id.getString("missing"))
                .withMessage("Property 'missing' is not a string");
    }

    @Test
    void to_text_is_human_readable() {

        String text = id.toText();

        assertThat(text).contains("tropian.io").contains("scale/compute").contains("dc", "west1")
                .contains("room", "442").contains("isle", "E").contains("rack", "E5");
    }

    @Test
    void to_string_is_the_text_representation() {
        assertThat(id).hasToString(id.toText());
    }

    @Test
    void is_represented_as_base64() {

        String encoded = id.toBase64();

        String decoded = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);

        assertThat(encoded).isNotEqualTo(id.toText());
        assertThat(decoded).isEqualTo(id.toText());
    }

    @Test
    void is_represented_as_zbase32() {

        String encoded = id.toBase32();
        String decoded = ZBase32.decode(encoded);

        assertThat(encoded).isNotEqualTo(id.toText());
        assertThat(decoded).isEqualTo(id.toText());
    }

    @Test
    void is_represented_as_json() throws Exception {

        JSONAssert.assertEquals("""
                {
                  "value": "%s",
                  "dc": "west1",
                  "room": "442",
                  "isle": "E",
                  "rack": "E5"
                }
                """.formatted(VALUE), id.toJSON(), true);
    }

    @Test
    void is_represented_as_pretty_json() throws Exception {

        JSONAssert.assertEquals("""
                {
                  "value": "%s",
                  "dc": "west1",
                  "room": "442",
                  "isle": "E",
                  "rack": "E5"
                }
                """.formatted(VALUE), id.toJSON(true), true);
    }

    @Test
    void escapes_json_special_characters() throws Exception {

        var id = Identifier.inRealm("tropian.io").inSector("scale").having("text", "line\n\"quoted\"\\").build();

        JSONAssert.assertEquals("""
                {"text":"line\\n\\"quoted\\"\\\\"}
                """, id.toJSON(), false);
    }

    @Test
    void is_represented_as_scalar_value() {

        assertThat(id.toValue()).isEqualTo(VALUE);
    }

    @Test
    void scalar_value_is_parsed() {

        Identifier parsedId = Identifier.fromValue(VALUE);

        assertThat(parsedId).isEqualTo(this.id);

        assertThat(parsedId.realm()).isEqualTo("tropian.io");
        assertThat(parsedId.sectors()).containsExactly("scale", "compute");

        assertThat(parsedId.get("dc")).isEqualTo("west1");
        assertThat(parsedId.getNumber("room")).isEqualTo(442);
        assertThat(parsedId.getString("isle")).isEqualTo("E");
        assertThat(parsedId.get("rack")).isEqualTo("E5");
    }

    @Test
    void round_trips_properties_with_reserved_characters() {

        var other = Identifier.inRealm("tropian.io").inSector("scale", "compute").having("dc", "west 1")
                .having("note", "a&b=c%d+e?f#g").having("city", "Malmö").build();

        var parsed = Identifier.fromValue(other.toValue());

        assertThat(parsed).isEqualTo(other);
        assertThat(parsed.getString("dc")).isEqualTo("west 1");
        assertThat(parsed.getString("note")).isEqualTo("a&b=c%d+e?f#g");
        assertThat(parsed.getString("city")).isEqualTo("Malmö");
    }

    @Test
    void round_trips_unicode_realm_and_sectors() {

        var other = Identifier.inRealm("exämple.com").inSector("scäle", "köpenhamn").having("city", "Malmö").build();

        var parsed = Identifier.fromValue(other.toValue());

        assertThat(parsed).isEqualTo(other);
        assertThat(parsed.realm()).isEqualTo("exämple.com");
        assertThat(parsed.sectors()).containsExactly("scäle", "köpenhamn");
    }
}
