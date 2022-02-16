package dagger.internal.codegen.binding;


import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor8;

import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static dagger.internal.codegen.langmodel.DaggerElements.DECLARATION_ORDER;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A factory for {@link Binding} objects.
 */
final class InjectionSiteFactory {

    private final DaggerTypes types;
    private final DaggerElements elements;
    private final DependencyRequestFactory dependencyRequestFactory;

    @Inject
    InjectionSiteFactory(
            DaggerTypes types,
            DaggerElements elements,
            DependencyRequestFactory dependencyRequestFactory
    ) {
        this.types = types;
        this.elements = elements;
        this.dependencyRequestFactory = dependencyRequestFactory;
    }


    /**
     * Returns the injection sites for a type.
     */
    ImmutableSortedSet<MembersInjectionBinding.InjectionSite> getInjectionSites(DeclaredType declaredType) {
        Set<MembersInjectionBinding.InjectionSite> injectionSites = new HashSet<>();
        List<TypeElement> ancestors = new ArrayList<>();
        InjectionSiteVisitor injectionSiteVisitor = new InjectionSiteVisitor();

        //遍历当前类及其非Objec对象的父类
        for (Optional<DeclaredType> currentType = Optional.of(declaredType);
             currentType.isPresent();
             currentType = types.nonObjectSuperclass(currentType.get())) {

            DeclaredType type = currentType.get();
            ancestors.add(MoreElements.asType(type.asElement()));

            //对当前类和非Object对象的父类 遍历其中的所有节点，筛选
            //1.节点使用Inject修饰 && 没有使用private修饰 && 没有使用static修饰;
            //&&2.如果节点是方法，那么该方法没有被覆写；
            //筛选下来的节点生成了InjectionSite对象集合
            for (Element enclosedElement : type.asElement().getEnclosedElements()) {
                injectionSiteVisitor.visit(enclosedElement, type).ifPresent(injectionSites::add);
            }
        }

        //父类型在子类型之前
        return ImmutableSortedSet.copyOf(
                // supertypes before subtypes
                Comparator.comparing(
                        (MembersInjectionBinding.InjectionSite injectionSite) ->
                                ancestors.indexOf(injectionSite.element().getEnclosingElement()))
                        .reversed()
                        // fields before methods
                        .thenComparing(injectionSite -> injectionSite.element().getKind())
                        // then sort by whichever element comes first in the parent
                        // this isn't necessary, but makes the processor nice and predictable
                        .thenComparing(MembersInjectionBinding.InjectionSite::element, DECLARATION_ORDER),
                injectionSites);
    }

    private final class InjectionSiteVisitor
            extends ElementKindVisitor8<Optional<MembersInjectionBinding.InjectionSite>, DeclaredType> {

        private final SetMultimap<String, ExecutableElement> subclassMethodMap =
                LinkedHashMultimap.create();

        InjectionSiteVisitor() {
            super(Optional.empty());
        }

        @Override
        public Optional<MembersInjectionBinding.InjectionSite> visitExecutableAsMethod(
                ExecutableElement method,
                DeclaredType type
        ) {
            subclassMethodMap.put(method.getSimpleName().toString(), method);
            if (!shouldBeInjected(method)) {
                return Optional.empty();
            }

            // This visitor assumes that subclass methods are visited before superclass methods, so we can
            // skip any overridden method that has already been visited. To decrease the number of methods
            // that are checked, we store the already injected methods in a SetMultimap and only check the
            // methods with the same name.
            //这个访问者假设子类方法在超类方法之前被访问，所以我们可以跳过任何已经访问过的覆盖方法。
            // 为了减少被检查的方法的数量，我们将已经注入的方法存储在一个 SetMultimap 中，并且只检查具有相同名称的方法。
            String methodName = method.getSimpleName().toString();
            TypeElement enclosingType = MoreElements.asType(method.getEnclosingElement());
            for (ExecutableElement subclassMethod : subclassMethodMap.get(methodName)) {
                //方法已经过处理的覆盖方法
                if (method != subclassMethod && elements.overrides(subclassMethod, method, enclosingType)) {
                    return Optional.empty();
                }
            }

            //使用Inject修饰 && 没有使用private修饰 && 没有使用static修饰的方法和该方法参数（依赖集合）生成方法类型的InjectionSite对象
            ExecutableType resolved = MoreTypes.asExecutable(types.asMemberOf(type, method));
            return Optional.of(
                    MembersInjectionBinding.InjectionSite.method(
                            method,
                            dependencyRequestFactory.forRequiredResolvedVariables(
                                    method.getParameters(), resolved.getParameterTypes())));
        }

        @Override
        public Optional<MembersInjectionBinding.InjectionSite> visitVariableAsField(
                VariableElement field,
                DeclaredType type
        ) {
            if (!shouldBeInjected(field)) {
                return Optional.empty();
            }
            //使用Inject修饰 && 没有使用private修饰 && 没有使用static修饰的变量生成变量类型的InjectionSite对象
            TypeMirror resolved = types.asMemberOf(type, field);
            return Optional.of(
                    MembersInjectionBinding.InjectionSite.field(
                            field, dependencyRequestFactory.forRequiredResolvedVariable(field, resolved)
                    )
            );
        }

        //节点使用Inject修饰 && 没有使用private修饰 && 没有使用static修饰
        private boolean shouldBeInjected(Element injectionSite) {
            return isAnnotationPresent(injectionSite, Inject.class)
                    && !injectionSite.getModifiers().contains(PRIVATE)
                    && !injectionSite.getModifiers().contains(STATIC);
        }
    }
}
