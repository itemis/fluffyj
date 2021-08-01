package com.itemis.fluffyj.exceptions;

import static com.itemis.fluffyj.exceptions.ThrowablePrettyfier.pretty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class ThrowablePrettyfierTest {

    @Test
    public void cannot_call_constructor() {
        var constrcutors = ThrowablePrettyfier.class.getDeclaredConstructors();
        assertThat(constrcutors.length).isEqualTo(1);
        var constructor = constrcutors[0];
        constructor.setAccessible(true);

        assertThatThrownBy(() -> constructor.newInstance()).hasCauseExactlyInstanceOf(InstantiationNotPermittedException.class);
    }

    @Test
    public void prettyfies_throwable_with_message() {
        var expectedException = new Exception("Expected exception. Please ignore.");

        assertThat(pretty(expectedException)).isEqualTo(expectedException.getClass().getSimpleName() + ": " + expectedException.getMessage());
    }

    @Test
    public void pretty_does_not_accept_null() {
        assertThatThrownBy(() -> pretty(null)).isInstanceOf(NullPointerException.class)
            .hasMessageContaining("t");
    }

    @Test
    public void prettyfies_throwable_without_message() {
        var expectedException = new NullPointerException();

        assertThat(pretty(expectedException)).isEqualTo(expectedException.getClass().getSimpleName() + ": No further information");
    }
}
