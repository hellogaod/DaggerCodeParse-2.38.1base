package dagger.android.processor;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;

import javax.annotation.processing.Filer;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;

import static javax.lang.model.util.ElementFilter.methodsIn;

/** Generates the implementation specified in {@code ContributesAndroidInjector}. */
final class ContributesAndroidInjectorGenerator implements BasicAnnotationProcessor.Step {

    private final AndroidInjectorDescriptor.Validator validator;
    private final Filer filer;
    private final Elements elements;
    private final boolean useStringKeys;
    private final SourceVersion sourceVersion;

    ContributesAndroidInjectorGenerator(
            AndroidInjectorDescriptor.Validator validator,
            boolean useStringKeys,
            Filer filer,
            Elements elements,
            SourceVersion sourceVersion) {
        this.validator = validator;
        this.useStringKeys = useStringKeys;
        this.filer = filer;
        this.elements = elements;
        this.sourceVersion = sourceVersion;
    }

    @Override
    public ImmutableSet<String> annotations() {
        return ImmutableSet.of(TypeNames.CONTRIBUTES_ANDROID_INJECTOR.toString());
    }

    @Override
    public ImmutableSet<Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
        ImmutableSet.Builder<Element> deferredElements = ImmutableSet.builder();
        for (ExecutableElement method : methodsIn(elementsByAnnotation.values())) {
            try {
                validator.createIfValid(method);
//                        .ifPresent(this::generate);
            } catch (TypeNotPresentException e) {
                deferredElements.add(method);
            }
        }
        return deferredElements.build();
    }
}
