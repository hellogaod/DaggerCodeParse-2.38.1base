package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import javax.lang.model.SourceVersion;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.base.OptionalType;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.DependencyRequest;

import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;
import static dagger.internal.codegen.langmodel.Accessibility.isTypeAccessibleFrom;

/** A binding expression for optional bindings. */
final class OptionalRequestRepresentation extends SimpleInvocationRequestRepresentation {
    private final ProvisionBinding binding;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerTypes types;
    private final SourceVersion sourceVersion;

    @AssistedInject
    OptionalRequestRepresentation(
            @Assisted ProvisionBinding binding,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types,
            SourceVersion sourceVersion) {
        super(binding);
        this.binding = binding;
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.types = types;
        this.sourceVersion = sourceVersion;
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {

        OptionalType optionalType = OptionalType.from(binding.key());
        OptionalType.OptionalKind optionalKind = optionalType.kind();

        if (binding.dependencies().isEmpty()) {
            if (sourceVersion.compareTo(SourceVersion.RELEASE_7) <= 0) {
                // When compiling with -source 7, javac's type inference isn't strong enough to detect
                // Futures.immediateFuture(Optional.absent()) for keys that aren't Object. It also has
                // issues
                // when used as an argument to some members injection proxy methods (see
                // https://github.com/google/dagger/issues/916)
                if (isTypeAccessibleFrom(binding.key().type().java(), requestingClass.packageName())) {
                    return Expression.create(
                            binding.key().type().java(),
                            optionalKind.parameterizedAbsentValueExpression(optionalType));
                }
            }
            return Expression.create(binding.key().type().java(), optionalKind.absentValueExpression());
        }
        DependencyRequest dependency = getOnlyElement(binding.dependencies());

        CodeBlock dependencyExpression =
                componentRequestRepresentations
                        .getDependencyExpression(bindingRequest(dependency), requestingClass)
                        .codeBlock();

        // If the dependency type is inaccessible, then we have to use Optional.<Object>of(...), or else
        // we will get "incompatible types: inference variable has incompatible bounds.
        return isTypeAccessibleFrom(dependency.key().type().java(), requestingClass.packageName())
                ? Expression.create(
                binding.key().type().java(), optionalKind.presentExpression(dependencyExpression))
                : Expression.create(
                types.erasure(binding.key().type().java()),
                optionalKind.presentObjectExpression(dependencyExpression));
    }

    @Override
    boolean requiresMethodEncapsulation() {
        // TODO(dpb): Maybe require it for present bindings.
        return false;
    }

    @AssistedFactory
    static interface Factory {
        OptionalRequestRepresentation create(ProvisionBinding binding);
    }
}
