package dagger.internal.codegen.kotlin;


import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.base.ClearableCache;
import kotlin.Metadata;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static dagger.internal.codegen.langmodel.DaggerElements.closestEnclosingTypeElement;

/**
 * Factory creating Kotlin metadata data objects.
 *
 * <p>The metadata is cache since it can be expensive to parse the information stored in a proto
 * binary string format in the metadata annotation values.
 */
@Singleton
public final class KotlinMetadataFactory implements ClearableCache {

    private final Map<TypeElement, KotlinMetadata> metadataCache = new HashMap<>();

    @Inject
    KotlinMetadataFactory() {
    }
    /**
     * Parses and returns the {@link KotlinMetadata} out of a given element.
     *
     * @throws IllegalStateException if the element has no metadata or is not enclosed in a type
     *     element with metadata. To check if an element has metadata use {@link
     *     KotlinMetadataUtil#hasMetadata(Element)}
     */
    public KotlinMetadata create(Element element) {
        TypeElement enclosingElement = closestEnclosingTypeElement(element);
        if (!isAnnotationPresent(enclosingElement, Metadata.class)) {
            throw new IllegalStateException("Missing @Metadata for: " + enclosingElement);
        }
        return metadataCache.computeIfAbsent(enclosingElement, KotlinMetadata::from);
    }

    @Override
    public void clearCache() {
        metadataCache.clear();
    }
}
