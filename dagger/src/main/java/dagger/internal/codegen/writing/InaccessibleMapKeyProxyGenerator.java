package dagger.internal.codegen.writing;


import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.TypeSpec;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.MapKeys;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Generates a class that exposes a non-{@code public} {@link
 * ContributionBinding#mapKeyAnnotation()} @MapKey} annotation.
 */
public final class InaccessibleMapKeyProxyGenerator
        extends SourceFileGenerator<ContributionBinding> {
    private final DaggerTypes types;
    private final DaggerElements elements;

    @Inject
    InaccessibleMapKeyProxyGenerator(
            XFiler filer,
            DaggerTypes types,
            DaggerElements elements,
            SourceVersion sourceVersion
    ) {
        super(filer, elements, sourceVersion);
        this.types = types;
        this.elements = elements;
    }

    @Override
    public Element originatingElement(ContributionBinding binding) {
        // a map key is only ever present on bindings that have a binding element
        return binding.bindingElement().get();
    }

    @Override
    public ImmutableList<TypeSpec.Builder> topLevelTypes(ContributionBinding binding) {
        return MapKeys.mapKeyFactoryMethod(binding, types, elements)
                .map(
                        method ->
                                classBuilder(MapKeys.mapKeyProxyClassName(binding))
                                        .addModifiers(PUBLIC, FINAL)
                                        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
                                        .addMethod(method))
                .map(ImmutableList::of)
                .orElse(ImmutableList.of());
    }
}
