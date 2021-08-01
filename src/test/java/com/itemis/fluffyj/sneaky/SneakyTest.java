package com.itemis.fluffyj.sneaky;

import static com.itemis.fluffyj.sneaky.Sneaky.throwThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.itemis.fluffyj.exceptions.InstantiationNotPermittedException;

public class SneakyTest {

    private static final Exception EXPECTED_CHECKED_EXCEPTION = new Exception("Expected exception. Please ignore.");
    private static final RuntimeException EXPECTED_UNCHECKED_EXCEPTION = new RuntimeException("Expected exception. Please ignore.");

    @Test
    public void cannot_call_constructor() {
        var constrcutors = Sneaky.class.getDeclaredConstructors();
        assertThat(constrcutors.length).isEqualTo(1);
        var constructor = constrcutors[0];
        constructor.setAccessible(true);

        assertThatThrownBy(() -> constructor.newInstance()).hasCauseExactlyInstanceOf(InstantiationNotPermittedException.class);
    }

    @Test
    public void should_throw_unchecked_exception() {
        assertThatThrownBy(() -> throwThat(EXPECTED_UNCHECKED_EXCEPTION)).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_throw_same_unchecked_exception() {
        assertThatThrownBy(() -> throwThat(EXPECTED_UNCHECKED_EXCEPTION)).isSameAs(EXPECTED_UNCHECKED_EXCEPTION);
    }

    @Test
    public void should_throw_checked_exception() {
        assertThatThrownBy(() -> throwThat(EXPECTED_CHECKED_EXCEPTION)).isInstanceOf(Exception.class);
    }

    @Test
    public void should_throw_same_checked_exception() {
        assertThatThrownBy(() -> throwThat(EXPECTED_CHECKED_EXCEPTION)).isSameAs(EXPECTED_CHECKED_EXCEPTION);
    }

    @Test
    public void should_throw_throwable() {
        assertThatThrownBy(() -> throwThat(new Throwable())).isInstanceOf(Throwable.class);
    }

    @Test
    public void should_throw_same_throwable() {
        Throwable expectedThrowable = new Throwable();
        assertThatThrownBy(() -> throwThat(expectedThrowable)).isSameAs(expectedThrowable);
    }

    @Test
    public void sneaky_implementation_is_done_correctly() {
        assertThatThrownBy(() -> should_not_need_to_declare_thrown_exceptions());
    }

    /**
     * This "test" won't compile if Sneaky's implementation was done wrong.
     */
    public void should_not_need_to_declare_thrown_exceptions() {
        throwThat(EXPECTED_CHECKED_EXCEPTION);
    }
}
