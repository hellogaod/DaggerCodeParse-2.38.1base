package dagger.hilt.internal.generatesrootinput;


import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to aggregate {@link dagger.hilt.GeneratesRootInput} types in a common
 * location.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface GeneratesRootInputPropagatedData {
    Class<? extends Annotation> value();
}
