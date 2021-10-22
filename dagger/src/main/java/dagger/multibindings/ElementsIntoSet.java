package dagger.multibindings;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The method's return type is {@code Set<T>} and all values are contributed to the set. The {@code
 * Set<T>} produced from the accumulation of values will be immutable. An example use is to provide
 * a default empty set binding, which is otherwise not possible using {@link IntoSet}.
 *
 * @see <a href="https://dagger.dev/multibindings#set-multibindings">Set multibinding</a>
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface ElementsIntoSet {}
