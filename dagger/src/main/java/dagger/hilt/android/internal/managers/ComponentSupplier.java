package dagger.hilt.android.internal.managers;

/**
 * Interface for supplying a component. This is separate from the Supplier interface so that
 * optimizers can strip this method (and therefore all the Dagger code) from the main dex even if a
 * Supplier is referenced in code kept in the main dex.
 */
public interface ComponentSupplier {
    Object get();
}
