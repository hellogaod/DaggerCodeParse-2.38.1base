package dagger.internal.codegen.base;

import javax.lang.model.element.Element;

import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;

import static com.google.auto.common.MoreElements.isAnnotationPresent;

/**
 * Whether a binding or declaration is for a unique contribution or a map or set multibinding.
 *
 * 多重绑定类型，MAP,SET,SET_VALUE,或者UNIQUE不是多重绑定
 */
public enum ContributionType {
    /**
     * Represents map bindings.
     */
    MAP,
    /**
     * Represents set bindings.
     */
    SET,
    /**
     * Represents set values bindings.
     */
    SET_VALUES,
    /**
     * Represents a valid non-collection binding.
     */
    UNIQUE,
    ;


    /**
     * An object that is associated with a {@link ContributionType}.
     */
    public interface HasContributionType {

        /**
         * The contribution type of this object.
         */
        ContributionType contributionType();
    }

    /**
     * {@code true} if this is for a multibinding.
     */
    public boolean isMultibinding() {
        return !this.equals(UNIQUE);
    }

    /**
     * The contribution type from a binding element's annotations. Presumes a well-formed binding
     * element (at most one of @IntoSet, @IntoMap, @ElementsIntoSet and @Provides.type). {@link
     * dagger.internal.codegen.validation.BindingMethodValidator} and {@link
     * dagger.internal.codegen.validation.BindsInstanceProcessingStep} validate correctness on their
     * own.
     *
     * 根据element节点使用的注解，使用ContributionType枚举类型表示
     */
    public static ContributionType fromBindingElement(Element element) {
        // TODO(bcorso): Replace these class references with ClassName.
        if (isAnnotationPresent(element, IntoMap.class)) {
            return ContributionType.MAP;
        } else if (isAnnotationPresent(element, IntoSet.class)) {
            return ContributionType.SET;
        } else if (isAnnotationPresent(element, ElementsIntoSet.class)) {
            return ContributionType.SET_VALUES;
        }
        return ContributionType.UNIQUE;
    }
}
