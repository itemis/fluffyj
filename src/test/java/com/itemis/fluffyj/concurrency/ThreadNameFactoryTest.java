package com.itemis.fluffyj.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ThreadNameFactoryTest {
    private static final String EXPECTED_STRING = "expectedString";

    private static final class TestImpl implements ThreadNameFactory {
        @Override
        public String generate() {
            return EXPECTED_STRING;
        }
    }

    @Test
    public void get_calls_generate() {
        var underTest = new TestImpl();

        assertThat(underTest.get()).isEqualTo(EXPECTED_STRING);
    }
}
