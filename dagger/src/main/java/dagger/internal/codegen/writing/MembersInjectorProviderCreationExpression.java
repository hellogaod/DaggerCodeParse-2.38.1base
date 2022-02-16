package dagger.internal.codegen.writing;

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ProvisionBinding;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.binding.SourceFiles.membersInjectorNameForType;
import static dagger.internal.codegen.javapoet.TypeNames.INSTANCE_FACTORY;
import static dagger.internal.codegen.javapoet.TypeNames.MEMBERS_INJECTORS;

/** A {@code Provider<MembersInjector<Foo>>} creation expression. */
final class MembersInjectorProviderCreationExpression
        implements FrameworkFieldInitializer.FrameworkInstanceCreationExpression {

    private final ComponentImplementation.ShardImplementation shardImplementation;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final ProvisionBinding binding;

    @AssistedInject
    MembersInjectorProviderCreationExpression(
            @Assisted ProvisionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations) {
        this.binding = checkNotNull(binding);
        this.shardImplementation = componentImplementation.shardImplementation(binding);
        this.componentRequestRepresentations = checkNotNull(componentRequestRepresentations);
    }

    @Override
    public CodeBlock creationExpression() {
        TypeMirror membersInjectedType =
                getOnlyElement(MoreTypes.asDeclared(binding.key().type().java()).getTypeArguments());

        boolean castThroughRawType = false;
        CodeBlock membersInjector;
        if (binding.injectionSites().isEmpty()) {
            membersInjector = CodeBlock.of("$T.<$T>noOp()", MEMBERS_INJECTORS, membersInjectedType);
        } else {
            TypeElement injectedTypeElement = MoreTypes.asTypeElement(membersInjectedType);
            while (!hasLocalInjectionSites(injectedTypeElement)) {
                // Cast through a raw type since we're going to be using the MembersInjector for the
                // parent type.
                castThroughRawType = true;
                injectedTypeElement = MoreTypes.asTypeElement(injectedTypeElement.getSuperclass());
            }

            membersInjector =
                    CodeBlock.of(
                            "$T.create($L)",
                            membersInjectorNameForType(injectedTypeElement),
                            componentRequestRepresentations.getCreateMethodArgumentsCodeBlock(
                                    binding, shardImplementation.name()));
        }

        // TODO(ronshapiro): consider adding a MembersInjectorRequestRepresentation to return this
        // directly
        // (as it's rarely requested as a Provider).
        CodeBlock providerExpression = CodeBlock.of("$T.create($L)", INSTANCE_FACTORY, membersInjector);
        // If needed we cast through raw type around the InstanceFactory type as opposed to the
        // MembersInjector since we end up with an InstanceFactory<MembersInjector> as opposed to a
        // InstanceFactory<MembersInjector<Foo>> and that becomes unassignable. To fix it would require
        // a second cast. If we just cast to the raw type InstanceFactory though, that becomes
        // assignable.
        return castThroughRawType
                ? CodeBlock.of("($T) $L", INSTANCE_FACTORY, providerExpression)
                : providerExpression;
    }

    private boolean hasLocalInjectionSites(TypeElement injectedTypeElement) {
        return binding.injectionSites().stream()
                .anyMatch(
                        injectionSite ->
                                injectionSite.element().getEnclosingElement().equals(injectedTypeElement));
    }

    @Override
    public boolean useSwitchingProvider() {
        return !binding.injectionSites().isEmpty();
    }

    @AssistedFactory
    static interface Factory {
        MembersInjectorProviderCreationExpression create(ProvisionBinding binding);
    }
}
