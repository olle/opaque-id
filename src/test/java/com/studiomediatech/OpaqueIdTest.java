package com.studiomediatech;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class OpaqueIdTest {

    @Test
    void ensureScaffoldsBuilderWithSystem() throws Exception {
        IdentifierBuilder builder = IdentifierScaffolding.usingSystem().raise();
        assertThat(builder).isNotNull();
    }
}
