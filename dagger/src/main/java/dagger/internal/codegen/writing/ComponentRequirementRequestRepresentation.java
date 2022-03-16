package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ComponentRequirement;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.javapoet.Expression;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A binding expression for instances bound with {@link dagger.BindsInstance} and instances of
 * {@linkplain dagger.Component#dependencies() component} and {@linkplain
 * dagger.producers.ProductionComponent#dependencies() production component dependencies}.
 * <p>
 * Component#dependencies()或ProductionComponent#dependencies()或BindsInstance修饰的(方法或方法)参数
 */
final class ComponentRequirementRequestRepresentation
        extends SimpleInvocationRequestRepresentation {
    private final ComponentRequirement componentRequirement;
    private final ComponentRequirementExpressions componentRequirementExpressions;

    @AssistedInject
    ComponentRequirementRequestRepresentation(
            @Assisted ContributionBinding binding,
            @Assisted ComponentRequirement componentRequirement,
            ComponentRequirementExpressions componentRequirementExpressions) {
        super(binding);
        this.componentRequirement = checkNotNull(componentRequirement);
        this.componentRequirementExpressions = componentRequirementExpressions;
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {
        return Expression.create(
                componentRequirement.type(),
                componentRequirementExpressions.getExpression(componentRequirement, requestingClass));
    }

    @AssistedFactory
    static interface Factory {
        ComponentRequirementRequestRepresentation create(
                ContributionBinding binding, ComponentRequirement componentRequirement);
    }
}
