package com.studiomediatech.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ZBase32Test {

    @Test
    void ensureEncodesCorrectly() {

        String result = ZBase32.encode("The quick brown fox jumps over the lazy dog.");
        assertThat(result).isEqualTo("ktwgkedtqiwsg43ycj3g675qrbug66bypj4s4hdurbzzc3m1rb4go3jyptozw6jyctzsqmo");
    }

    @Test
    void ensureDecodesCorrectly() throws Exception {

        String result = ZBase32.decode("ktwgkedtqiwsg43ycj3g675qrbug66bypj4s4hdurbzzc3m1rb4go3jyptozw6jyctzsqmo");
        assertThat(result).isEqualTo("The quick brown fox jumps over the lazy dog.");
    }

}
