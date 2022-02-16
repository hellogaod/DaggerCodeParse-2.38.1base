package dagger.internal.codegen.validation;

import com.google.auto.common.MoreTypes;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Formatter;
import java.util.Map;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.ModuleDescriptor;
import dagger.internal.codegen.binding.ModuleKind;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.spi.model.Scope;

import static com.google.common.base.Functions.constant;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static dagger.internal.codegen.base.Scopes.getReadableSource;
import static dagger.internal.codegen.base.Scopes.uniqueScopeOf;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * Validates the relationships between parent components and subcomponents.
 */
final class ComponentHierarchyValidator {
    private static final Joiner COMMA_SEPARATED_JOINER = Joiner.on(", ");
    private final CompilerOptions compilerOptions;

    @Inject
    ComponentHierarchyValidator(CompilerOptions compilerOptions) {
        this.compilerOptions = compilerOptions;
    }

    ValidationReport validate(ComponentDescriptor componentDescriptor) {

        ValidationReport.Builder report = ValidationReport.about(componentDescriptor.typeElement());

        //5. 校验component节点上componentMethod方法返回类型是subcomponent节点：
        // - （1）componentMethod返回类型如果是subcomponent节点，那么当前subcomponent节点不允许出现creator内部类节点；
        // - （2）componentMethod返回类型是subcomponent节点，那么当前componentMethod方法参数必须是module节点，并且该module节点不允许出现在componentAnnotation#modules（componentMethod方法所在的component）中
        // - 注：该componentMethod的module节点参数应该存在于当前返回类型subcomponent关联的module节点（subcomponent#modules，module#includes）中；
        validateSubcomponentMethods(
                report,
                componentDescriptor,
                Maps.toMap(componentDescriptor.moduleTypes(), constant(componentDescriptor.typeElement())));

        //6. 筛选集component节点上所有module节点：
        // - 筛选条件：componentAnnotation#modules、moduleAnnotation#includes，
        // 这类所有module节点中bindingMethod方法和moduleAnnotation#subcomponents的subcomponent声明如果使用了Reusable除外）Scope修饰的注解；
        // - 该module节点上使用的Scope修饰的注解不会再次出现在以下：①componentMethod返回类型是subcomponent节点；
        // ②componentMethod返回类型是subcomponent.creator表示的subcomponent节点；
        // ③当前component-componentAnnotation#modules、moduleAnnotation#includes的module关联moduleAnnotation#subcomponent节点；
        // 以上subcomponent节点 - subcomponentAnntation#modules中；
        validateRepeatedScopedDeclarations(report, componentDescriptor, LinkedHashMultimap.create());

        if (compilerOptions.scopeCycleValidationType().diagnosticKind().isPresent()) {
            //7. component节点上使用的Scope修饰的注解（排除ProductionScope）最好不要再次出现在以下子component节点中：
            // - ① componentMethod返回类型是subcomponent节点；
            // ②componentMethod返回类型是subcomponent.creator的subcomponent节点；
            // ③component - componentAnnotation#modules、module#includes的所有module - moduleAnnotation#subcomponents收集的subcomponent节点；
            validateScopeHierarchy(
                    report, componentDescriptor, LinkedHashMultimap.<ComponentDescriptor, Scope>create());
        }

        //8. component节点上如果componentAnnotation#modules的module节点使用了ProducerModule注解，
        // 那么component节点关联的childComponents（所有childComponentAnnotation#modules）module节点不允许再次使用ProducerModule注解，childComponents来源：
        // - ① componentMethod返回类型是subcomponent节点；
        // ②componentMethod返回类型是subcomponent.creator的subcomponent节点；
        // ③component - componentAnnotation#modules、module#includes的所有module - moduleAnnotation#subcomponents收集的subcomponent节点；
        validateProductionModuleUniqueness(report, componentDescriptor, LinkedHashMultimap.create());

        return report.build();
    }

    private void validateSubcomponentMethods(
            ValidationReport.Builder report,
            ComponentDescriptor componentDescriptor,
            ImmutableMap<TypeElement, TypeElement> existingModuleToOwners) {

        componentDescriptor
                //component类中方法返回类型是一个subcomponent类
                .childComponentsDeclaredByFactoryMethods()
                .forEach(
                        (method, childComponent) -> {
                            // 该subcomponent不能有creator节点
                            if (childComponent.hasCreator()) {
                                report.addError(
                                        "Components may not have factory methods for subcomponents that define a "
                                                + "builder.",
                                        method.methodElement());
                            } else {

                                //该方法的参数不能存在于当前component（及其父component）关联的module集合中
                                validateFactoryMethodParameters(report, method, existingModuleToOwners);
                            }

                            validateSubcomponentMethods(
                                    report,
                                    childComponent,
                                    new ImmutableMap.Builder<TypeElement, TypeElement>()
                                            .putAll(existingModuleToOwners)
                                            .putAll(//当前subcomponent关联的所有module类
                                                    Maps.toMap(
                                                            Sets.difference(
                                                                    childComponent.moduleTypes(), existingModuleToOwners.keySet()),
                                                            constant(childComponent.typeElement())))
                                            .build());
                        });
    }

    private void validateFactoryMethodParameters(
            ValidationReport.Builder report,
            ComponentDescriptor.ComponentMethodDescriptor subcomponentMethodDescriptor,
            ImmutableMap<TypeElement, TypeElement> existingModuleToOwners) {

        //component类中的方法返回类型是subcomponent，遍历该方法参数，确保参数不能存在于当前所在component关联的module类集合中
        for (VariableElement factoryMethodParameter :
                subcomponentMethodDescriptor.methodElement().getParameters()) {

            TypeElement moduleType = MoreTypes.asTypeElement(factoryMethodParameter.asType());
            TypeElement originatingComponent = existingModuleToOwners.get(moduleType);

            if (originatingComponent != null) {
                /* Factory method tries to pass a module that is already present in the parent.
                 * This is an error. */
                report.addError(
                        String.format(
                                "%s is present in %s. A subcomponent cannot use an instance of a "
                                        + "module that differs from its parent.",
                                moduleType.getSimpleName(), originatingComponent.getQualifiedName()),
                        factoryMethodParameter);
            }
        }
    }

    /**
     * Checks that components do not have any scopes that are also applied on any of their ancestors.
     * <p>
     * 排除ProductionScope scope注解。component类关联的childcomponent中不能使用和component类相同的Scope注解修饰的注解，根据实际情况是报错还是警告
     */
    private void validateScopeHierarchy(
            ValidationReport.Builder report,
            ComponentDescriptor subject,
            SetMultimap<ComponentDescriptor, Scope> scopesByComponent) {

        scopesByComponent.putAll(subject, subject.scopes());

        for (ComponentDescriptor childComponent : subject.childComponents()) {
            validateScopeHierarchy(report, childComponent, scopesByComponent);
        }

        scopesByComponent.removeAll(subject);

        //排除ProductionScope
        Predicate<Scope> subjectScopes =
                subject.isProduction()
                        // TODO(beder): validate that @ProductionScope is only applied on production components
                        ? and(in(subject.scopes()), not(Scope::isProductionScope))
                        : in(subject.scopes());

        SetMultimap<ComponentDescriptor, Scope> overlappingScopes =
                Multimaps.filterValues(scopesByComponent, subjectScopes);

        if (!overlappingScopes.isEmpty()) {
            StringBuilder error =
                    new StringBuilder()
                            .append(subject.typeElement().getQualifiedName())
                            .append(" has conflicting scopes:");
            for (Map.Entry<ComponentDescriptor, Scope> entry : overlappingScopes.entries()) {
                Scope scope = entry.getValue();
                error
                        .append("\n  ")
                        .append(entry.getKey().typeElement().getQualifiedName())
                        .append(" also has ")
                        .append(getReadableSource(scope));
            }
            report.addItem(
                    error.toString(),
                    compilerOptions.scopeCycleValidationType().diagnosticKind().get(),
                    subject.typeElement());
        }
    }

    //当前component关联的使用ProducerModule注解的module，那么该ProducerModule修饰的module在当前component关联的childcomponent节点上不能继续使用
    private void validateProductionModuleUniqueness(
            ValidationReport.Builder report,
            ComponentDescriptor componentDescriptor,
            SetMultimap<ComponentDescriptor, ModuleDescriptor> producerModulesByComponent) {

        //component关联的module类筛选使用了ProducerModule注解
        ImmutableSet<ModuleDescriptor> producerModules =
                componentDescriptor.modules().stream()
                        .filter(module -> module.kind().equals(ModuleKind.PRODUCER_MODULE))
                        .collect(toImmutableSet());

        producerModulesByComponent.putAll(componentDescriptor, producerModules);

        for (ComponentDescriptor childComponent : componentDescriptor.childComponents()) {
            validateProductionModuleUniqueness(report, childComponent, producerModulesByComponent);
        }
        producerModulesByComponent.removeAll(componentDescriptor);


        SetMultimap<ComponentDescriptor, ModuleDescriptor> repeatedModules =
                Multimaps.filterValues(producerModulesByComponent, producerModules::contains);

        if (repeatedModules.isEmpty()) {
            return;
        }

        StringBuilder error = new StringBuilder();
        Formatter formatter = new Formatter(error);

        formatter.format("%s repeats @ProducerModules:", componentDescriptor.typeElement());

        for (Map.Entry<ComponentDescriptor, Collection<ModuleDescriptor>> entry :
                repeatedModules.asMap().entrySet()) {
            formatter.format("\n  %s also installs: ", entry.getKey().typeElement());
            COMMA_SEPARATED_JOINER
                    .appendTo(error, Iterables.transform(entry.getValue(), m -> m.moduleElement()));
        }

        report.addError(error.toString());
    }

    //当前component关联的module如果存在scope修饰的注解（Reusable除外）修饰的bindingMethod绑定方法，那么该module不能继续存在于component关联的所有component中，否则表示module重复使用
    private void validateRepeatedScopedDeclarations(
            ValidationReport.Builder report,
            ComponentDescriptor component,
            // TODO(ronshapiro): optimize ModuleDescriptor.hashCode()/equals. Otherwise this could be
            // quite costly
            SetMultimap<ComponentDescriptor, ModuleDescriptor> modulesWithScopes) {

        //component类中的module集合，筛选出当前module：所有bindingMethod绑定(包括moduleAnnotation#subcomponents)中有使用Scope注解修饰的注解（非Reusable）
        ImmutableSet<ModuleDescriptor> modules =
                component.modules().stream().filter(this::hasScopedDeclarations).collect(toImmutableSet());

        modulesWithScopes.putAll(component, modules);

        for (ComponentDescriptor childComponent : component.childComponents()) {
            validateRepeatedScopedDeclarations(report, childComponent, modulesWithScopes);
        }

        modulesWithScopes.removeAll(component);

        SetMultimap<ComponentDescriptor, ModuleDescriptor> repeatedModules =
                Multimaps.filterValues(modulesWithScopes, modules::contains);

        if (repeatedModules.isEmpty()) {
            return;
        }

        report.addError(
                repeatedModulesWithScopeError(component, ImmutableSetMultimap.copyOf(repeatedModules)));
    }

    private boolean hasScopedDeclarations(ModuleDescriptor module) {
        return !moduleScopes(module).isEmpty();
    }

    private String repeatedModulesWithScopeError(
            ComponentDescriptor component,
            ImmutableSetMultimap<ComponentDescriptor, ModuleDescriptor> repeatedModules) {
        StringBuilder error =
                new StringBuilder()
                        .append(component.typeElement().getQualifiedName())
                        .append(" repeats modules with scoped bindings or declarations:");

        repeatedModules
                .asMap()
                .forEach(
                        (conflictingComponent, conflictingModules) -> {
                            error
                                    .append("\n  - ")
                                    .append(conflictingComponent.typeElement().getQualifiedName())
                                    .append(" also includes:");
                            for (ModuleDescriptor conflictingModule : conflictingModules) {
                                error
                                        .append("\n    - ")
                                        .append(conflictingModule.moduleElement().getQualifiedName())
                                        .append(" with scopes: ")
                                        .append(COMMA_SEPARATED_JOINER.join(moduleScopes(conflictingModule)));
                            }
                        });
        return error.toString();
    }

    private ImmutableSet<Scope> moduleScopes(ModuleDescriptor module) {
        return
                //module中的所有绑定包括moduleAnnotation#subcomponents中的节点
                FluentIterable.concat(module.allBindingDeclarations())
                        //该绑定使用了Scope注解修饰的注解
                        .transform(declaration -> uniqueScopeOf(declaration.bindingElement().get()))
                        //筛选出Scope注解修饰的注解不是Reusable
                        .filter(scope -> scope.isPresent() && !scope.get().isReusable())
                        .transform(scope -> scope.get())
                        .toSet();
    }
}
