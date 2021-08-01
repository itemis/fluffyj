package com.itemis.fluffyj.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class InstantiationNotPermittedExceptionTest {

    @Test
    public void has_expected_message() {
        assertThat(new InstantiationNotPermittedException()).hasMessage("Instantiation of class not permitted.");
    }

    @Test
    public void exception_is_unchecked() {
        assertThat(new InstantiationNotPermittedException()).isInstanceOf(RuntimeException.class);
    }
}
