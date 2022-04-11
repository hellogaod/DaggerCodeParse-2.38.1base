package dagger.hilt.migration;


import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dagger.hilt.GeneratesRootInput;

/**
 * Defines an alias between an existing Hilt scope and the annotated scope. For example, the
 * following code makes {@literal @}MyScope a functional replacement for {@literal @}ActivityScope.
 *
 * <p>
 *
 * <pre>
 *   {@literal @}Scope
 *   {@literal @}AliasOf(ActivityScope.class)
 *   public {@literal @}interface MyScope{}
 * </pre>
 *
 * <p>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.CLASS)
@GeneratesRootInput
public @interface AliasOf {
    /**
     * Returns the existing Hilt scope that the annotated scope is aliasing.
     */
    Class<? extends Annotation> value();
}
