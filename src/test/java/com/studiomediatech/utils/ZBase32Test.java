package com.studiomediatech.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class ZBase32Test {

    @Test
    void encodes_correctly() {

        String result = ZBase32.encode("The quick brown fox jumps over the lazy dog.");

        assertThat(result).isEqualTo("ktwgkedtqiwsg43ycj3g675qrbug66bypj4s4hdurbzzc3m1rb4go3jyptozw6jyctzsqmo");
    }

    @Test
    void decodes_correctly() {

        String result = ZBase32.decode("ktwgkedtqiwsg43ycj3g675qrbug66bypj4s4hdurbzzc3m1rb4go3jyptozw6jyctzsqmo");

        assertThat(result).isEqualTo("The quick brown fox jumps over the lazy dog.");
    }

    @Test
    void round_trips_unicode() {

        String value = "Grüße, 世界! 👋";

        assertThat(ZBase32.decode(ZBase32.encode(value))).isEqualTo(value);
    }

    @Test
    void round_trips_empty_string() {

        assertThat(ZBase32.decode(ZBase32.encode(""))).isEmpty();
    }

    @Test
    void rejects_characters_outside_the_alphabet() {

        assertThatIllegalArgumentException().isThrownBy(() -> ZBase32.decode("yb0")).withMessageContaining("index 2");
    }
}
