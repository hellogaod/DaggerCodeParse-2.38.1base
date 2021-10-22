package dagger;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dagger.internal.Beta;

/**
 * Annotates a class that contributes to the object graph.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Module {
    /**
     * Additional {@code @Module}-annotated classes from which this module is
     * composed. The de-duplicated contributions of the modules in
     * {@code includes}, and of their inclusions recursively, are all contributed
     * to the object graph.
     */
    Class<?>[] includes() default {};

    /**
     * Any {@link Subcomponent}- or {@code @ProductionSubcomponent}-annotated classes which should be
     * children of the component in which this module is installed. A subcomponent may be listed in
     * more than one module in a component.
     *
     * @since 2.7
     */
    @Beta
    Class<?>[] subcomponents() default {};
}
