package dagger.internal.codegen.validation;


import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.ExecutableElement;

/** Validates any binding method. */
@Singleton
public final class AnyBindingMethodValidator{
    private final ImmutableMap<ClassName, BindingMethodValidator> validators;
    private final Map<ExecutableElement, ValidationReport> reports = new HashMap<>();

    @Inject
    AnyBindingMethodValidator(ImmutableMap<ClassName, BindingMethodValidator> validators) {
        this.validators = validators;
    }
}
