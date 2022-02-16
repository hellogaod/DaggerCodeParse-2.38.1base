package dagger.internal.codegen.binding;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.base.ModuleAnnotation;
import dagger.spi.model.Key;

import static com.google.auto.common.AnnotationMirrors.getAnnotationElementAndValue;
import static dagger.internal.codegen.binding.ConfigurationAnnotations.getSubcomponentCreator;

/**
 * A declaration for a subcomponent that is included in a module via {@link
 * dagger.Module#subcomponents()}.
 * <p>
 * dagger.Module#subcomponents()里面的item生成的对象
 */
@AutoValue
public abstract class SubcomponentDeclaration extends BindingDeclaration {
    /**
     * Key for the {@link dagger.Subcomponent.Builder} or {@link
     * dagger.producers.ProductionSubcomponent.Builder} of {@link #subcomponentType()}.
     */
    @Override
    public abstract Key key();

    /**
     * The type element that defines the {@link dagger.Subcomponent} or {@link
     * dagger.producers.ProductionSubcomponent} for this declaration.
     */
    abstract TypeElement subcomponentType();

    /**
     * The module annotation.
     */
    public abstract ModuleAnnotation moduleAnnotation();

    @Memoized
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    /**
     * A {@link SubcomponentDeclaration} factory.
     */
    public static class Factory {
        private final KeyFactory keyFactory;

        @Inject
        Factory(KeyFactory keyFactory) {
            this.keyFactory = keyFactory;
        }

        ImmutableSet<SubcomponentDeclaration> forModule(TypeElement module) {

            ImmutableSet.Builder<SubcomponentDeclaration> declarations = ImmutableSet.builder();
            ModuleAnnotation moduleAnnotation = ModuleAnnotation.moduleAnnotation(module).get();

            //表示moduleAnnotation注解的subcomponents方法
            Element subcomponentAttribute =
                    getAnnotationElementAndValue(moduleAnnotation.annotation(), "subcomponents").getKey();

            //moduleAnnotation#subcomponent
            for (TypeElement subcomponent : moduleAnnotation.subcomponents()) {
                declarations.add(
                        new AutoValue_SubcomponentDeclaration(
                                Optional.of(subcomponentAttribute),//moduleAnnotation注解的subcomponents方法
                                Optional.of(module),//所在module类
                                keyFactory.forSubcomponentCreator(
                                        getSubcomponentCreator(subcomponent).get().asType()),//对subcomponent类中的creator节点生成一个key对象
                                subcomponent,//subcomponent类
                                moduleAnnotation)//使用的moduleAnnotation注解
                );
            }
            return declarations.build();
        }

    }
}
