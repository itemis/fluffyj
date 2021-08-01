package com.itemis.fluffyj.sneaky;

import com.itemis.fluffyj.exceptions.InstantiationNotPermittedException;

/**
 * <p>
 * Static helper class around the sneaky throws paradigm.
 * </p>
 *
 * @see <a href=
 *      "http://www.philandstuff.com/2012/04/28/sneakily-throwing-checked-exceptions.html">http://www.philandstuff.com/2012/04/28/sneakily-throwing-checked-exceptions.html</a>
 */
public final class Sneaky {

    private Sneaky() {
        throw new InstantiationNotPermittedException();
    }

    /**
     * <p>
     * (Re)throw the provided {@code throwable} without the need to declare it to be thrown. This is
     * handy in situations where it is not possible or advised to specify {@code throws} but
     * wrapping a checked exception inside a {@link RuntimeException} is considered not viable (i.
     * e. ugly).
     * </p>
     * <p>
     * <b>Be aware</b> that this may be a surprise for client code, so please take care when using
     * this functionality.
     * <p>
     * Example:
     *
     * <pre>
     *  *
     *  * This method throws an IOException even though it does not declare it with {@code throws}.
     *  *
     *  *
     *  public void operation() {
     *      Sneaky.throwThat(new IOException());
     *  }
     * </pre>
     * </p>
     *
     * @param t - The {@link Throwable} to rethrow.
     * @throws T - Will be inferred to an instance of {@link RuntimeException} by the compiler.
     * @see <a href=
     *      "http://www.philandstuff.com/2012/04/28/sneakily-throwing-checked-exceptions.html">http://www.philandstuff.com/2012/04/28/sneakily-throwing-checked-exceptions.html</a>
     */
    /*
     * The method uses the so called sneaky throws paradigm. This requires an "unchecked" (i. e.
     * dangerous) cast. However, the cast can be considered "safe", because at runtime it does not
     * matter which kind of Throwable will be thrown. The types do not need to match from a
     * technical point of view. The compiler tries to match types at compile time but at runtime,
     * 'throws T' disappears. Also at runtime the cast disappears due to type erasure, so no
     * ClassCastException etc. will be thrown.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void throwThat(Throwable t) throws T {
        throw (T) t;
    }
}
