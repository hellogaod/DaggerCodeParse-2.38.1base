package dagger.hilt.android.processor.internal.bindvalue;


import com.google.auto.common.MoreElements;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.KotlinMetadataUtils;
import dagger.hilt.processor.internal.ProcessorErrors;
import dagger.hilt.processor.internal.Processors;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;

/**
 * Represents metadata for a test class that has {@code BindValue} fields.
 */
@AutoValue
abstract class BindValueMetadata {
    static final ImmutableSet<ClassName> BIND_VALUE_ANNOTATIONS =
            ImmutableSet.of(
                    ClassNames.ANDROID_BIND_VALUE);
    static final ImmutableSet<ClassName> BIND_VALUE_INTO_SET_ANNOTATIONS =
            ImmutableSet.of(
                    ClassNames.ANDROID_BIND_VALUE_INTO_SET);
    static final ImmutableSet<ClassName> BIND_ELEMENTS_INTO_SET_ANNOTATIONS =
            ImmutableSet.of(
                    ClassNames.ANDROID_BIND_ELEMENTS_INTO_SET);
    static final ImmutableSet<ClassName> BIND_VALUE_INTO_MAP_ANNOTATIONS =
            ImmutableSet.of(
                    ClassNames.ANDROID_BIND_VALUE_INTO_MAP);

    /**
     * @return the {@code TestRoot} annotated class's name.
     */
    abstract TypeElement testElement();

    /**
     * @return a {@link ImmutableSet} of elements annotated with @BindValue.
     */
    abstract ImmutableSet<BindValueElement> bindValueElements();

    /**
     * @return a new BindValueMetadata instance.
     */
    static BindValueMetadata create(TypeElement testElement, Collection<Element> bindValueElements) {

        ImmutableSet.Builder<BindValueElement> elements = ImmutableSet.builder();
        for (Element element : bindValueElements) {
            elements.add(BindValueElement.create(element));
        }

        return new AutoValue_BindValueMetadata(testElement, elements.build());
    }

    @AutoValue
    abstract static class BindValueElement {
        abstract VariableElement variableElement();

        abstract ClassName annotationName();

        abstract Optional<AnnotationMirror> qualifier();

        abstract Optional<AnnotationMirror> mapKey();

        abstract Optional<ExecutableElement> getterElement();

        static BindValueElement create(Element element) {
            ImmutableList<ClassName> bindValues = BindValueProcessor.getBindValueAnnotations(element);
            //1. @BindValue???@BindValueIntoSet???@BindElementsIntoSet???@BindValueIntoMap???????????????????????????????????????
            ProcessorErrors.checkState(
                    bindValues.size() == 1,
                    element,
                    "Fields can be annotated with only one of @BindValue, @BindValueIntoMap,"
                            + " @BindElementsIntoSet, @BindValueIntoSet. Found: %s",
                    bindValues.stream().map(m -> "@" + m.simpleName()).collect(toImmutableList()));
            ClassName annotationClassName = bindValues.get(0);

            //2. bindValue????????????????????????
            ProcessorErrors.checkState(
                    element.getKind() == ElementKind.FIELD,
                    element,
                    "@%s can only be used with fields. Found: %s",
                    annotationClassName.simpleName(),
                    element);

            KotlinMetadataUtil metadataUtil = KotlinMetadataUtils.getMetadataUtil();
            Optional<ExecutableElement> propertyGetter =
                    //????????????element?????????????????????????????????????????????????????????????????????????????????@Metadata?????????Kotlin???
                    metadataUtil.hasMetadata(element)
                            //?????????kotlin?????????????????????????????????????????????????????? getter ?????????
                            ? metadataUtil.getPropertyGetter(MoreElements.asVariable(element))
                            : Optional.empty();

            //3. bindValue??????????????????????????????????????????????????????kotlin???????????????bindValue?????????getter??????????????????private???????????????kotlin?????????????????????bindValue??????????????????private?????????
            if (propertyGetter.isPresent()) {
                ProcessorErrors.checkState(
                        !propertyGetter.get().getModifiers().contains(Modifier.PRIVATE),
                        element,
                        "@%s field getter cannot be private. Found: %s",
                        annotationClassName.simpleName(),
                        element);
            } else {

                ProcessorErrors.checkState(
                        !element.getModifiers().contains(Modifier.PRIVATE),
                        element,
                        "@%s fields cannot be private. Found: %s",
                        annotationClassName.simpleName(),
                        element);
            }

            //4. bindValue??????????????????@Inject???????????????
            ProcessorErrors.checkState(
                    !Processors.hasAnnotation(element, Inject.class),
                    element,
                    "@%s fields cannot be used with @Inject annotation. Found %s",
                    annotationClassName.simpleName(),
                    element);

            //5. bindValue?????????????????????????????????@Qualifier????????????????????????
            ImmutableList<AnnotationMirror> qualifiers = Processors.getQualifierAnnotations(element);
            ProcessorErrors.checkState(
                    qualifiers.size() <= 1,
                    element,
                    "@%s fields cannot have more than one qualifier. Found %s",
                    annotationClassName.simpleName(),
                    qualifiers);

            //6. bindValue?????????????????????@BindValueIntoMap????????????????????????@MapKey??????????????????????????????????????????@MapKey???????????????????????????????????????
            ImmutableList<AnnotationMirror> mapKeys = Processors.getMapKeyAnnotations(element);
            Optional<AnnotationMirror> optionalMapKeys;
            if (BIND_VALUE_INTO_MAP_ANNOTATIONS.contains(annotationClassName)) {
                ProcessorErrors.checkState(
                        mapKeys.size() == 1,
                        element,
                        "@BindValueIntoMap fields must have exactly one @MapKey. Found %s",
                        mapKeys);
                optionalMapKeys = Optional.of(mapKeys.get(0));
            } else {
                ProcessorErrors.checkState(
                        mapKeys.isEmpty(),
                        element,
                        "@MapKey can only be used on @BindValueIntoMap fields, not @%s fields",
                        annotationClassName.simpleName());
                optionalMapKeys = Optional.empty();
            }

            //7. bindValue?????????????????????@Scope????????????????????????
            ImmutableList<AnnotationMirror> scopes = Processors.getScopeAnnotations(element);
            ProcessorErrors.checkState(
                    scopes.isEmpty(),
                    element,
                    "@%s fields cannot be scoped. Found %s",
                    annotationClassName.simpleName(),
                    scopes);

            return new AutoValue_BindValueMetadata_BindValueElement(
                    (VariableElement) element,
                    annotationClassName,
                    qualifiers.isEmpty()
                            ? Optional.<AnnotationMirror>empty()
                            : Optional.<AnnotationMirror>of(qualifiers.get(0)),
                    optionalMapKeys,
                    propertyGetter);
        }
    }
}
