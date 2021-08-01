package com.itemis.fluffyj.exceptions;

import static com.itemis.fluffyj.exceptions.ThrowablePrettyfier.pretty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class ImplementationProblemExceptionTest {

    @Test
    public void default_constructor_sets_expected_message() {
        var underTest = new ImplementationProblemException();

        assertThat(underTest.getMessage()).isEqualTo("An implementation problem occurred.");
    }

    @Test
    public void constructor_with_throwable_sets_expected_message() {
        var expectedThrowable = new Throwable("Expected throwable. Please ignore.");
        var underTest = new ImplementationProblemException(expectedThrowable);

        assertThat(underTest.getMessage()).isEqualTo("An implementation problem occurred: " + pretty(expectedThrowable));
    }

    @Test
    public void constructor_with_throwable_does_not_accept_null() {
        assertThatThrownBy(() -> new ImplementationProblemException((Throwable) null)).isInstanceOf(NullPointerException.class).hasMessageContaining("cause");
    }

    @Test
    public void constructor_with_string_and_throwable_does_not_accept_null_string() {
        var expectedThrowable = new Throwable("Expected throwable. Please ignore.");
        assertThatThrownBy(() -> new ImplementationProblemException(null, expectedThrowable)).isInstanceOf(NullPointerException.class)
            .hasMessageContaining("description");
    }

    @Test
    public void constructor_with_string_and_throwable_does_not_accept_null_throwable() {
        assertThatThrownBy(() -> new ImplementationProblemException("description", null)).isInstanceOf(NullPointerException.class)
            .hasMessageContaining("cause");
    }

    @Test
    public void constructor_with_string_and_throwable_sets_expected_message() {
        var expectedThrowable = new Throwable("Expected throwable. Please ignore.");
        var expectedDescription = "description";

        var underTest = new ImplementationProblemException(expectedDescription, expectedThrowable);

        assertThat(underTest.getMessage()).isEqualTo("An implementation problem occurred: " + expectedDescription + ": " + pretty(expectedThrowable));
    }

    @Test
    public void constructor_with_string_constructs_expected_message() {
        var msg = "msg";
        var underTest = new ImplementationProblemException(msg);
        assertThat(underTest.getMessage()).isEqualTo("An implementation problem occurred: " + msg);
    }

    @Test
    public void constructor_with_string_does_not_accept_null() {
        assertThatThrownBy(() -> new ImplementationProblemException((String) null)).isInstanceOf(NullPointerException.class).hasMessageContaining("msg");
    }
}
