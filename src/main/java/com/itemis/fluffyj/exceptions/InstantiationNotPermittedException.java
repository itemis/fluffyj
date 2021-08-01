package com.itemis.fluffyj.exceptions;

/**
 * <p>
 * Use this exception to mark class constructors as being off limits.
 * </p>
 * <p>
 * Example:
 *
 * <pre>
 * public final class Clazz() {
 *     private Clazz() {
 *         throw new InstantionNotPermittedException();
 *     }
 * }
 * </pre>
 * </p>
 * <p>
 * This prevents "smart" implementations from instantiating static helper classes that are not
 * thought to be instantiated.
 * </p>
 */
public final class InstantiationNotPermittedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Construct a new instance.
     */
    public InstantiationNotPermittedException() {
        super("Instantiation of class not permitted.");
    }
}
