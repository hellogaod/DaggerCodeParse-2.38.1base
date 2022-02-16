package dagger.internal.codegen.binding;


import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import dagger.Component;
import dagger.Module;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.consumingIterable;
import static dagger.internal.codegen.base.ComponentAnnotation.subcomponentAnnotation;
import static dagger.internal.codegen.base.ModuleAnnotation.moduleAnnotation;
import static dagger.internal.codegen.binding.ComponentCreatorAnnotation.subcomponentCreatorAnnotations;
import static dagger.internal.codegen.langmodel.DaggerElements.isAnnotationPresent;
import static dagger.internal.codegen.langmodel.DaggerElements.isAnyAnnotationPresent;
import static javax.lang.model.util.ElementFilter.typesIn;

/**
 * Utility methods related to dagger configuration annotations (e.g.: {@link Component} and {@link
 * Module}).
 */
public final class ConfigurationAnnotations {

    //subcomponent类中找creator节点
    public static Optional<TypeElement> getSubcomponentCreator(TypeElement subcomponent) {
        //是否使用了(Production)Subcomponent注解
        checkArgument(subcomponentAnnotation(subcomponent).isPresent());

        //subcomponent类的方法使用了(Production)SubComponent建造者（Builder或Factory注解），返回该方法节点
        for (TypeElement nestedType : typesIn(subcomponent.getEnclosedElements())) {
            if (isSubcomponentCreator(nestedType)) {
                return Optional.of(nestedType);
            }
        }
        return Optional.empty();
    }

    //element使用了(Production)SubComponent建造者（Builder或Factory注解）。
    static boolean isSubcomponentCreator(Element element) {
        return isAnyAnnotationPresent(element, subcomponentCreatorAnnotations());
    }

    /**
     * Returns the first type that specifies this' nullability, or empty if none.
     *
     * element节点是否使用Nullable注解，返回该注解表示的类型
     */
    public static Optional<DeclaredType> getNullableType(Element element) {
        List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : mirrors) {
            if (mirror.getAnnotationType().asElement().getSimpleName().contentEquals("Nullable")) {
                return Optional.of(mirror.getAnnotationType());
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the full set of modules transitively {@linkplain Module#includes included} from the
     * given seed modules. If a module is malformed and a type listed in {@link Module#includes} is
     * not annotated with {@link Module}, it is ignored.
     * <p>
     * 1.返回当前seeModules以及Module#includes中所有的module类（module类必须使用了Producer(Module)注解才行）
     * 2.如果Module#includes中所有的module类有父类，而且父类也是用了Producer(Module)注解，那么会加入迭代器执行1操作
     *
     * @deprecated Use {@link ComponentDescriptor#modules()}.
     */
    @Deprecated
    public static ImmutableSet<TypeElement> getTransitiveModules(
            DaggerTypes types,
            DaggerElements elements,
            Iterable<TypeElement> seedModules
    ) {
        TypeMirror objectType = elements.getTypeElement(Object.class).asType();

        Queue<TypeElement> moduleQueue = new ArrayDeque<>();
        Iterables.addAll(moduleQueue, seedModules);
        Set<TypeElement> moduleElements = Sets.newLinkedHashSet();

        //遍历所有的seedModules（module节点）
        for (TypeElement moduleElement : consumingIterable(moduleQueue)) {

            moduleAnnotation(moduleElement)
                    .ifPresent(
                            moduleAnnotation -> {
                                ImmutableSet.Builder<TypeElement> moduleDependenciesBuilder =
                                        ImmutableSet.builder();
                                //moduleAnnotation#icludes里面的module类
                                moduleDependenciesBuilder.addAll(moduleAnnotation.includes());
                                // We don't recur on the parent class because we don't want the parent class as a
                                // root that the component depends on, and also because we want the dependencies
                                // rooted against this element, not the parent.
                                //moduleElement父类遍历，查找module类
                                addIncludesFromSuperclasses(
                                        types, moduleElement, moduleDependenciesBuilder, objectType);

                                ImmutableSet<TypeElement> moduleDependencies = moduleDependenciesBuilder.build();


                                moduleElements.add(moduleElement);

                                //还可以在遍历过程中添加
                                for (TypeElement dependencyType : moduleDependencies) {
                                    if (!moduleElements.contains(dependencyType)) {
                                        moduleQueue.add(dependencyType);
                                    }
                                }
                            });
        }
        return ImmutableSet.copyOf(moduleElements);
    }

    /**
     * Returns the enclosed types annotated with the given annotation.
     * <p>
     * 返回当前typeElement类中内部类使用annotation注解的类或接口
     */
    public static ImmutableList<DeclaredType> enclosedAnnotatedTypes(
            TypeElement typeElement, ClassName annotation) {

        final ImmutableList.Builder<DeclaredType> builders = ImmutableList.builder();
        for (TypeElement element : typesIn(typeElement.getEnclosedElements())) {
            if (isAnnotationPresent(element, annotation)) {
                builders.add(MoreTypes.asDeclared(element.asType()));
            }
        }
        return builders.build();
    }


    /**
     * Traverses includes from superclasses and adds them into the builder.
     * <p>
     * 一级级遍历element父类（保证父类必须是DECLARED类或接口类型，遍历前提：不是objectType类型），
     * 并且添加到builder集合（并且使用了moduleAnnotation注解）
     */
    private static void addIncludesFromSuperclasses(
            DaggerTypes types,
            TypeElement element,
            ImmutableSet.Builder<TypeElement> builder,
            TypeMirror objectType) {
        // Also add the superclass to the queue, in case any @Module definitions were on that.
        TypeMirror superclass = element.getSuperclass();
        while (!types.isSameType(objectType, superclass)
                && superclass.getKind().equals(TypeKind.DECLARED)) {
            element = MoreElements.asType(types.asElement(superclass));
            moduleAnnotation(element)
                    .ifPresent(moduleAnnotation -> builder.addAll(moduleAnnotation.includes()));
            superclass = element.getSuperclass();
        }
    }

    private ConfigurationAnnotations() {
    }
}
