package dagger.internal.codegen.validation;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.ExecutableElement;

import dagger.internal.codegen.base.ClearableCache;

import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.base.Util.reentrantComputeIfAbsent;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.langmodel.DaggerElements.isAnnotationPresent;
import static dagger.internal.codegen.langmodel.DaggerElements.isAnyAnnotationPresent;
import static java.util.stream.Collectors.joining;

/**
 * Validates any binding method.
 * <p>
 * 对所有绑定方法进行校验
 */
@Singleton
public final class AnyBindingMethodValidator implements ClearableCache {

    private final ImmutableMap<ClassName, BindingMethodValidator> validators;
    private final Map<ExecutableElement, ValidationReport> reports = new HashMap<>();

    @Inject
    AnyBindingMethodValidator(ImmutableMap<ClassName, BindingMethodValidator> validators) {
        this.validators = validators;
    }

    @Override
    public void clearCache() {
        reports.clear();
    }

    /**
     * Returns the binding method annotations considered by this validator.
     */
    ImmutableSet<ClassName> methodAnnotations() {
        return validators.keySet();
    }

    /**
     * Returns {@code true} if {@code method} is annotated with at least one of {@link
     * #methodAnnotations()}.
     * <p>
     * 如果method方法至少使用了一个methodAnnotations()中的注解，就返回true
     */
    boolean isBindingMethod(ExecutableElement method) {
        return isAnyAnnotationPresent(method, methodAnnotations());
    }


    /**
     * Returns a validation report for a method.
     * <p>
     * 校验一个绑定的方法入口
     *
     * <ul>
     *   <li>Reports an error if {@code method} is annotated with more than one {@linkplain
     *       #methodAnnotations() binding method annotation}.
     *   <li>Validates {@code method} with the {@link BindingMethodValidator} for the single
     *       {@linkplain #methodAnnotations() binding method annotation}.
     * </ul>
     *
     * @throws IllegalArgumentException if {@code method} is not annotated by any {@linkplain
     *                                  #methodAnnotations() binding method annotation}
     */
    ValidationReport validate(ExecutableElement method) {
        return reentrantComputeIfAbsent(reports, method, this::validateUncached);
    }


    /**
     * Returns {@code true} if {@code method} was already {@linkplain #validate(ExecutableElement)
     * validated}.
     */
    boolean wasAlreadyValidated(ExecutableElement method) {
        return reports.containsKey(method);
    }


    //绑定方法使用了methodAnnotations()中的0个或超过1个注解都报错；如果使用了其中一个注解，在对这个绑定方法进行下一步校验
    private ValidationReport validateUncached(ExecutableElement method) {
        ValidationReport.Builder report = ValidationReport.about(method);

        //在methodAnnotations()中收集当前方法使用了哪些注解
        ImmutableSet<ClassName> bindingMethodAnnotations =
                methodAnnotations().stream().
                        filter(
                                annotation -> isAnnotationPresent(method, annotation)
                        )
                        .collect(toImmutableSet());

        switch (bindingMethodAnnotations.size()) {
            case 0:
                throw new IllegalArgumentException(
                        String.format("%s has no binding method annotation", method));

            case 1://bindingMethod有且仅又五种注解类型中的一个
                report.addSubreport(
                        validators.get(getOnlyElement(bindingMethodAnnotations)).validate(method));
                break;

            default:
                report.addError(
                        String.format(
                                "%s is annotated with more than one of (%s)",
                                method.getSimpleName(),
                                methodAnnotations().stream().map(ClassName::canonicalName).collect(joining(", "))),
                        method);
                break;
        }
        return report.build();
    }
}
