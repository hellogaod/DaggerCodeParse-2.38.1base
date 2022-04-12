package dagger.hilt.android.internal.legacy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Annotation for marking public proxies that reference package-private aggregating elements from
 * pre-stable versions of Hilt (version < 2.35).
 */
@Target(ElementType.TYPE)
@Retention(CLASS)
public @interface AggregatedElementProxy {
    /** A reference to the legacy package-private aggregating class. */
    Class<?> value();
}

