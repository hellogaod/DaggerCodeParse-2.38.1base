package dagger.multibindings;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The method's return type forms the type argument for the value of a {@code Map<K, Provider<V>>},
 * and the combination of the annotated key and the returned value is contributed to the map as a
 * key/value pair. The {@code Map<K, Provider<V>>} produced from the accumulation of values will be
 * immutable.
 *
 * @see <a href="https://dagger.dev/multibindings#map-multibindings">Map multibinding</a>
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface IntoMap {}
