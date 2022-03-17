/*
 * Copyright (C) 2017 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.android.processor;

import com.google.auto.common.BasicAnnotationProcessor.Step;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.squareup.javapoet.ClassName;

import javax.annotation.processing.Messager;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import dagger.MapKey;

import static com.google.auto.common.AnnotationMirrors.getAnnotatedAnnotations;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.android.processor.AndroidMapKeys.injectedTypeFromMapKey;

/**
 * Validates the correctness of {@link MapKey}s used with {@code dagger.android}.
 */
final class AndroidMapKeyValidator implements Step {
    private static final ImmutableMap<String, ClassName> SUPPORTED_ANNOTATIONS =
            ImmutableMap.of(
                    TypeNames.ANDROID_INJECTION_KEY.toString(), TypeNames.ANDROID_INJECTION_KEY,
                    TypeNames.CLASS_KEY.toString(), TypeNames.CLASS_KEY);

    private final Elements elements;
    private final Types types;
    private final Messager messager;

    AndroidMapKeyValidator(Elements elements, Types types, Messager messager) {
        this.elements = elements;
        this.types = types;
        this.messager = messager;
    }

    @Override
    public ImmutableSet<String> annotations() {
        return SUPPORTED_ANNOTATIONS.keySet();
    }

    @Override
    public ImmutableSet<Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
        ImmutableSet.Builder<Element> deferredElements = ImmutableSet.builder();
        elementsByAnnotation
                .entries()
                .forEach(
                        entry -> {
                            try {
                                validateMethod(entry.getKey(), MoreElements.asExecutable(entry.getValue()));
                            } catch (TypeNotPresentException e) {
                                deferredElements.add(entry.getValue());
                            }
                        });
        return deferredElements.build();
    }

    private void validateMethod(String annotation, ExecutableElement method) {
        if (!getAnnotatedAnnotations(method, Qualifier.class).isEmpty()) {
            return;
        }

        //该方法返回类型如果不是AndroidInjector.Factory及其子类，不需要继续校验了，该方法直接被忽略；
        TypeMirror returnType = method.getReturnType();
        if (!types.isAssignable(types.erasure(returnType), factoryElement().asType())) {
            // if returnType is not related to AndroidInjector.Factory, ignore the method
            return;
        }

        //如果该方法使用了Scope注解修饰的注解修饰，该方法必须使用@SuppressWarnings注解修饰，并且该@SuppressWarning注解包含dagger.android.ScopedInjectorFactory值
        // （意思就是最好别使用Scope修饰的注解修饰）；
        if (!getAnnotatedAnnotations(method, Scope.class).isEmpty()) {
            SuppressWarnings suppressedWarnings = method.getAnnotation(SuppressWarnings.class);
            if (suppressedWarnings == null
                    || !ImmutableSet.copyOf(suppressedWarnings.value())
                    .contains("dagger.android.ScopedInjectorFactory")) {
                AnnotationMirror mapKeyAnnotation =
                        getOnlyElement(getAnnotatedAnnotations(method, MapKey.class));
                TypeElement mapKeyValueElement =
                        elements.getTypeElement(injectedTypeFromMapKey(mapKeyAnnotation).get());
                messager.printMessage(
                        Kind.ERROR,
                        String.format(
                                "%s bindings should not be scoped. Scoping this method may leak instances of %s.",
                                TypeNames.ANDROID_INJECTOR_FACTORY.canonicalName(),
                                mapKeyValueElement.getQualifiedName()),
                        method);
            }
        }

        validateReturnType(method);

        // @Binds methods should only have one parameter, but we can't guarantee the order of Processors
        // in javac, so do a basic check for valid form
        if (MoreDaggerElements.isAnnotationPresent(method, TypeNames.BINDS)
                && method.getParameters().size() == 1) {
            validateMapKeyMatchesBindsParameter(annotation, method);
        }
    }

    /**
     * Report an error if the method's return type is not {@code AndroidInjector.Factory<?>}.
     */
    private void validateReturnType(ExecutableElement method) {
        TypeMirror returnType = method.getReturnType();
        DeclaredType requiredReturnType = injectorFactoryOf(types.getWildcardType(null, null));

        //该方法的返回类型必须是AndroidInjector.Factory<T>
        if (!types.isSameType(returnType, requiredReturnType)) {
            messager.printMessage(
                    Kind.ERROR,
                    String.format(
                            "%s should bind %s, not %s. See https://dagger.dev/android",
                            method, requiredReturnType, returnType),
                    method);
        }
    }

    /**
     * A valid @Binds method could bind an {@code AndroidInjector.Factory} for one type, while giving
     * it a map key of a different type. The return type and parameter type would pass typical @Binds
     * validation, but the map lookup in {@code DispatchingAndroidInjector} would retrieve the wrong
     * injector factory.
     *
     * <pre>{@code
     * {@literal @Binds}
     * {@literal @IntoMap}
     * {@literal @ClassKey(GreenActivity.class)}
     * abstract AndroidInjector.Factory<?> bindBlueActivity(
     *     BlueActivityComponent.Builder builder);
     * }</pre>
     * <p>
     * 如果该方法还使用了Binds注解，并且方法参数只有一个：
     * （对照下面的案例理解），该方法参数类型`BlueActivityComponent.Builder`必须可以匹配上（继承）
     * `AndroidInjector.Factory<ClassKey的value值：GreenActivity>`
     */
    private void validateMapKeyMatchesBindsParameter(String annotation, ExecutableElement method) {
        TypeMirror parameterType = getOnlyElement(method.getParameters()).asType();
        AnnotationMirror annotationMirror =
                MoreDaggerElements.getAnnotationMirror(method, SUPPORTED_ANNOTATIONS.get(annotation)).get();
        TypeMirror mapKeyType =
                elements.getTypeElement(injectedTypeFromMapKey(annotationMirror).get()).asType();
        if (!types.isAssignable(parameterType, injectorFactoryOf(mapKeyType))) {
            messager.printMessage(
                    Kind.ERROR,
                    String.format("%s does not implement AndroidInjector<%s>", parameterType, mapKeyType),
                    method,
                    annotationMirror);
        }
    }

    /**
     * Returns a {@link DeclaredType} for {@code AndroidInjector.Factory<implementationType>}.
     */
    private DeclaredType injectorFactoryOf(TypeMirror implementationType) {
        return types.getDeclaredType(factoryElement(), implementationType);
    }

    private TypeElement factoryElement() {
        return elements.getTypeElement(TypeNames.ANDROID_INJECTOR_FACTORY.canonicalName());
    }
}
