package dagger.internal.codegen.kotlin;

import javax.inject.Inject;

/** Utility class for interacting with Kotlin Metadata. */
public final class KotlinMetadataUtil {

    private final KotlinMetadataFactory metadataFactory;

    @Inject
    KotlinMetadataUtil(KotlinMetadataFactory metadataFactory) {
        this.metadataFactory = metadataFactory;
    }
}
