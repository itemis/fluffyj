package com.itemis.fluffyj.concurrency;

import java.util.function.Supplier;

/**
 * A {@link Supplier} that is supposed to create viable names for threads. These names usually
 * appear in logs and may make debugging easier, because they help to identify which thread caused a
 * log entry. Thus, the names should be created in such a way so that threads can be easily
 * distinguished from another. Implementors are advised but not forced to provide unique ids.
 * Implementors should document how likely it is that different threads will end up with the same
 * name.
 */
@FunctionalInterface
public interface ThreadNameFactory extends Supplier<String> {

    /**
     * Calls {@link #generate()}.
     */
    @Override
    default String get() {
        return generate();
    }

    /**
     * Convenience method to make code more readable. {@link #get()} will always call this method.
     *
     * @return A new thread name.
     */
    String generate();
}
