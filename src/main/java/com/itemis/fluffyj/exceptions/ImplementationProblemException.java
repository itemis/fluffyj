package com.itemis.fluffyj.exceptions;

import static com.itemis.fluffyj.exceptions.ThrowablePrettyfier.pretty;
import static java.util.Objects.requireNonNull;

/**
 * <p>
 * Use instances of this exception to signal a problem that must definitely be caused by a faulty
 * implementation, e. g. implementors forgot to implement a proper default branch in a switch
 * statement.
 * </p>
 */
public final class ImplementationProblemException extends RuntimeException {

    private static final long serialVersionUID = -501582724292146370L;

    public static final String DEFAULT_MSG = "An implementation problem occurred";

    /**
     * Sets the {@link #DEFAULT_MSG}.
     */
    public ImplementationProblemException() {
        super(DEFAULT_MSG + ".");
    }

    /**
     * Sets a message based on a combination of {@link #DEFAULT_MSG} and {@code cause}.
     */
    public ImplementationProblemException(Throwable cause) {
        super(DEFAULT_MSG + ": " + pretty(requireNonNull(cause, "cause")));
    }

    /**
     * Sets a message based on a combination of {@link #DEFAULT_MSG}, {@code description} and
     * {@code cause}.
     */
    public ImplementationProblemException(String description, Throwable cause) {
        super(DEFAULT_MSG + ": " + requireNonNull(description, "description") + ": " + pretty(requireNonNull(cause, "cause")));
    }

    /**
     * Sets a message based on a combination of {@code msg} and {@link #DEFAULT_MSG}.
     *
     * @param msg
     */
    public ImplementationProblemException(String msg) {
        super(DEFAULT_MSG + ": " + requireNonNull(msg, "msg"));
    }
}
