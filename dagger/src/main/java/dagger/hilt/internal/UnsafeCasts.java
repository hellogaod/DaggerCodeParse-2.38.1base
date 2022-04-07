package dagger.hilt.internal;


/**
 * Runtime utility method for performing a casting in generated code.
 */
public final class UnsafeCasts {

    // Only used where code generations makes it safe.
    @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
    public static <T> T unsafeCast(Object obj) {
        return (T) obj;
    }

    private UnsafeCasts() {
    }
}
