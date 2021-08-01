package com.itemis.fluffyj.exceptions;

import static java.util.Objects.requireNonNull;

/**
 * A static helper class that helps with "pretty" logging of {@link Throwable Throwables} or
 * {@link Exception Exceptions}.
 */
public final class ThrowablePrettyfier {

    private ThrowablePrettyfier() {
        throw new InstantiationNotPermittedException();
    }

    /**
     * Construct a nice message out of a throwable's type and its message (if any). If the
     * {@code throwable} does not have a message, a default one will be used. May be used for
     * logging purposes.
     *
     * @param t The {@link Throwable} to construct the message for.
     * @return A nice message.
     */
    public static String pretty(Throwable t) {
        requireNonNull(t, "t");

        var msg = t.getMessage() == null ? "No further information" : t.getMessage();
        return t.getClass().getSimpleName() + ": " + msg;
    }
}
