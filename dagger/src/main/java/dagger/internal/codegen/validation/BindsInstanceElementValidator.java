package dagger.internal.codegen.validation;


import javax.lang.model.element.Element;

import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;

abstract class BindsInstanceElementValidator<E extends Element> extends BindingElementValidator<E> {
    BindsInstanceElementValidator(InjectionAnnotations injectionAnnotations) {
        super(
                TypeNames.BINDS_INSTANCE,
                AllowsMultibindings.NO_MULTIBINDINGS,
                AllowsScoping.NO_SCOPING,
                injectionAnnotations);
    }

    @Override
    protected final String bindingElements() {
        // Even though @BindsInstance may be placed on methods, the subject of errors is the
        // parameter
        return "@BindsInstance parameters";
    }

    @Override
    protected final String bindingElementTypeVerb() {
        return "be";
    }
}
