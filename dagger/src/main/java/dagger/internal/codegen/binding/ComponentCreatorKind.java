package dagger.internal.codegen.binding;


import com.google.common.base.Ascii;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

/**
 * Enumeration of the different kinds of component creators.
 */
public enum ComponentCreatorKind {
    /**
     * {@code @Component.Builder} or one of its subcomponent/production variants.
     */
    BUILDER,

    /**
     * {@code @Component.Factory} or one of its subcomponent/production variants.
     */
    FACTORY,
    ;

    /**
     * Name to use as (or as part of) a type name for a creator of this kind.
     */
    public String typeName() {
        //  test_data----> TestData  (大写下划线 --> 大写驼峰)
        return UPPER_UNDERSCORE.to(UPPER_CAMEL, name());
    }

    /**
     * Name to use for a component's static method returning a creator of this kind.
     */
    public String methodName() {
        return Ascii.toLowerCase(name());
    }
}
