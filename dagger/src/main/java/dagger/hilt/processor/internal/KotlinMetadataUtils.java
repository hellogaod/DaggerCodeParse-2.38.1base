package dagger.hilt.processor.internal;


import javax.inject.Singleton;

import dagger.Component;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;

/**
 * A single-use provider of {@link KotlinMetadataUtil}. Since the returned util has a cache, it is
 * better to reuse the same instance as much as possible, except for going across processor rounds
 * because the cache contains elements.
 */
// TODO(erichang):  Revert this, should be wrapped with a Dagger module.
public final class KotlinMetadataUtils {

    @Singleton
    @Component
    interface MetadataComponent {
        KotlinMetadataUtil get();
    }

    /** Gets the metadata util. */
    public static KotlinMetadataUtil getMetadataUtil() {
        return DaggerKotlinMetadataUtils_MetadataComponent.create().get();
    }

    private KotlinMetadataUtils() {}
}
