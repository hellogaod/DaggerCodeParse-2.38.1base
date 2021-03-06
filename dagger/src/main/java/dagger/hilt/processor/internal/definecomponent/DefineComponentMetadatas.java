package dagger.hilt.processor.internal.definecomponent;


import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import dagger.hilt.processor.internal.AnnotationValues;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.ProcessorErrors;
import dagger.hilt.processor.internal.Processors;

import static com.google.auto.common.AnnotationMirrors.getAnnotationElementAndValue;
import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreTypes.asTypeElement;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.STATIC;

/** Metadata for types annotated with {@link dagger.hilt.DefineComponent}. */
final class DefineComponentMetadatas {
    static DefineComponentMetadatas create() {
        return new DefineComponentMetadatas();
    }

    private final Map<Element, DefineComponentMetadata> metadatas = new HashMap<>();

    private DefineComponentMetadatas() {}

    /** Returns the metadata for an element annotated with {@link dagger.hilt.DefineComponent}. */
    DefineComponentMetadata get(Element element) {
        return get(element, new LinkedHashSet<>());
    }

    private DefineComponentMetadata get(Element element, LinkedHashSet<Element> childPath) {
        if (!metadatas.containsKey(element)) {
            metadatas.put(element, getUncached(element, childPath));
        }
        return metadatas.get(element);
    }

    private DefineComponentMetadata getUncached(
            Element element, LinkedHashSet<Element> childPath) {

        //1. DefineComponent#parent()?????????????????????????????????????????????
        ProcessorErrors.checkState(
                childPath.add(element),
                element,
                "@DefineComponent cycle: %s -> %s",
                childPath.stream().map(Object::toString).collect(joining(" -> ")),
                element);

        ProcessorErrors.checkState(
                Processors.hasAnnotation(element, ClassNames.DEFINE_COMPONENT),
                element,
                "%s, expected to be annotated with @DefineComponent. Found: %s",
                element,
                element.getAnnotationMirrors());

        //2. @DefineComponent???????????????????????????????????????abstract??????????????????
        // TODO(bcorso): Allow abstract classes?
        ProcessorErrors.checkState(
                element.getKind().equals(ElementKind.INTERFACE),
                element,
                "@DefineComponent is only allowed on interfaces. Found: %s",
                element);
        TypeElement component = asType(element);

        //3. @DefineComponent??????????????????????????????????????????
        // TODO(bcorso): Allow extending interfaces?
        ProcessorErrors.checkState(
                component.getInterfaces().isEmpty(),
                component,
                "@DefineComponent %s, cannot extend a super class or interface. Found: %s",
                component,
                component.getInterfaces());

        //4. @DefineComponent????????????????????????????????????
        // TODO(bcorso): Allow type parameters?
        ProcessorErrors.checkState(
                component.getTypeParameters().isEmpty(),
                component,
                "@DefineComponent %s, cannot have type parameters.",
                component.asType());

        //5. @DefineComponent???????????????????????????????????????????????????static?????????
        // TODO(bcorso): Allow non-static abstract methods (aka EntryPoints)?
        List<ExecutableElement> nonStaticMethods =
                ElementFilter.methodsIn(component.getEnclosedElements()).stream()
                        .filter(method -> !method.getModifiers().contains(STATIC))
                        .collect(Collectors.toList());
        ProcessorErrors.checkState(
                nonStaticMethods.isEmpty(),
                component,
                "@DefineComponent %s, cannot have non-static methods. Found: %s",
                component,
                nonStaticMethods);

        // No need to check non-static fields since interfaces can't have them.

        ImmutableList<TypeElement> scopes =
                Processors.getScopeAnnotations(component).stream()
                        .map(AnnotationMirror::getAnnotationType)
                        .map(MoreTypes::asTypeElement)
                        .collect(toImmutableList());

        //6. @DefineComponent??????????????????????????????@AliasOf???????????????
        ImmutableList<AnnotationMirror> aliasScopes =
                Processors.getAnnotationsAnnotatedWith(component, ClassNames.ALIAS_OF);
        ProcessorErrors.checkState(
                aliasScopes.isEmpty(),
                component,
                "@DefineComponent %s, references invalid scope(s) annotated with @AliasOf. "
                        + "@DefineComponent scopes cannot be aliases of other scopes: %s",
                component,
                aliasScopes);

        AnnotationMirror mirror =
                Processors.getAnnotationMirror(component, ClassNames.DEFINE_COMPONENT);
        AnnotationValue parentValue = getAnnotationElementAndValue(mirror, "parent").getValue();

        //7. @DefineComponent#parent???????????????????????????error????????????????????????????????????
        ProcessorErrors.checkState(
                // TODO(bcorso): Contribute a check to auto/common AnnotationValues.
                !"<error>".contentEquals(parentValue.getValue().toString()),
                component,
                "@DefineComponent %s, references an invalid parent type: %s",
                component,
                mirror);

        TypeElement parent = asTypeElement(AnnotationValues.getTypeMirror(parentValue));

        //8.  @DefineComponent#parent???????????????????????????DefineComponentNoParent???????????????????????????????????????@DefineComponent??????????????????
        ProcessorErrors.checkState(
                ClassName.get(parent).equals(ClassNames.DEFINE_COMPONENT_NO_PARENT)
                        || Processors.hasAnnotation(parent, ClassNames.DEFINE_COMPONENT),
                component,
                "@DefineComponent %s, references a type not annotated with @DefineComponent: %s",
                component,
                parent);

        Optional<DefineComponentMetadata> parentComponent =
                ClassName.get(parent).equals(ClassNames.DEFINE_COMPONENT_NO_PARENT)
                        ? Optional.empty()
                        : Optional.of(get(parent, childPath));

        ClassName componentClassName = ClassName.get(component);

        //9. @DefineComponent#parent?????????????????????@DefineComponent?????? || ??????component????????????SingletonComponent?????????
        ProcessorErrors.checkState(
                parentComponent.isPresent()
                        || componentClassName.equals(ClassNames.SINGLETON_COMPONENT),
                component,
                "@DefineComponent %s is missing a parent declaration.\n"
                        + "Please declare the parent, for example: @DefineComponent(parent ="
                        + " SingletonComponent.class)",
                component);

        //10. component???????????????dagger.hilt.components?????????SingletonComponent????????????????????????????????????????????????SingletonComponent???????????????
        ProcessorErrors.checkState(
                componentClassName.equals(ClassNames.SINGLETON_COMPONENT)
                        || !componentClassName.simpleName().equals(ClassNames.SINGLETON_COMPONENT.simpleName()),
                component,
                "Cannot have a component with the same simple name as the reserved %s: %s",
                ClassNames.SINGLETON_COMPONENT.simpleName(),
                componentClassName);

        return new AutoValue_DefineComponentMetadatas_DefineComponentMetadata(
                component, scopes, parentComponent);
    }

    @AutoValue
    abstract static class DefineComponentMetadata {

        /** Returns the component annotated with {@link dagger.hilt.DefineComponent}. */
        abstract TypeElement component();

        /** Returns the scopes of the component. */
        abstract ImmutableList<TypeElement> scopes();

        /** Returns the parent component, if one exists. */
        abstract Optional<DefineComponentMetadata> parentMetadata();

        boolean isRoot() {
            return !parentMetadata().isPresent();
        }
    }
}
