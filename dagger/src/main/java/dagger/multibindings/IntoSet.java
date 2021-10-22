package dagger.multibindings;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The method's return type forms the generic type argument of a {@code Set<T>}, and the returned
 * value is contributed to the set. The object graph will pass dependencies to the method as
 * parameters. The {@code Set<T>} produced from the accumulation of values will be immutable.
 *
 * @see <a href="https://dagger.dev/multibindings#set-multibindings">Set multibinding</a>
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface IntoSet {}