package dagger.internal.codegen.kotlin;


import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.base.ClearableCache;

/**
 * Factory creating Kotlin metadata data objects.
 *
 * <p>The metadata is cache since it can be expensive to parse the information stored in a proto
 * binary string format in the metadata annotation values.
 */
@Singleton
public final class KotlinMetadataFactory {

    @Inject
    KotlinMetadataFactory() {
    }
}
