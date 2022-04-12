package dagger.hilt.internal.aggregatedroot;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to aggregate {@link dagger.hilt.android.HiltAndroidApp} and {@link
 * dagger.hilt.android.testing.HiltAndroidTest} roots.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface AggregatedRoot {
    String root();

    String originatingRoot();

    Class<?> rootAnnotation();
}
