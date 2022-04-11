package dagger.hilt.internal.aliasof;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** An annotation used to aggregate AliasOf values in a common location. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface AliasOfPropagatedData {
    Class<? extends Annotation> defineComponentScope();

    Class<? extends Annotation> alias();
}
