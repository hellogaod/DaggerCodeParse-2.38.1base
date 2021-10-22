package dagger.internal.codegen.base;


/** A cache of objects that can be cleared. */
public interface ClearableCache {
    /** Releases cached references. */
    void clearCache();
}
