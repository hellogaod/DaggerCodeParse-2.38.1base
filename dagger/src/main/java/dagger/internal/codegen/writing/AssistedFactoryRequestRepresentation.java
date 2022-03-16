package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.RequestKind;

import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreTypes.asDeclared;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.binding.AssistedInjectionAnnotations.assistedFactoryMethod;
import static dagger.internal.codegen.binding.AssistedInjectionAnnotations.assistedFactoryParameterSpecs;

/**
 * A {@link dagger.internal.codegen.writing.RequestRepresentation} for {@link
 * dagger.assisted.AssistedFactory} methods.
 *
 * AssistedFactory修饰的节点中的有且仅有的唯一方法
 */
final class AssistedFactoryRequestRepresentation extends SimpleInvocationRequestRepresentation {
    private final ProvisionBinding binding;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerElements elements;
    private final DaggerTypes types;

    @AssistedInject
    AssistedFactoryRequestRepresentation(
            @Assisted ProvisionBinding binding,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types,
            DaggerElements elements) {
        super(binding);
        this.binding = checkNotNull(binding);
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.elements = elements;
        this.types = types;
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {
        // An assisted factory binding should have a single request for an assisted injection type.
        DependencyRequest assistedInjectionRequest = getOnlyElement(binding.provisionDependencies());
        //@AssistedFactory修饰的节点的唯一方法的返回类型是使用@AssistedInject修饰的构造函数
        Expression assistedInjectionExpression =
                ((AssistedPrivateMethodRequestRepresentation)
                        componentRequestRepresentations.getRequestRepresentation(
                                BindingRequest.bindingRequest(
                                        assistedInjectionRequest.key(), RequestKind.INSTANCE)))
                        .getAssistedDependencyExpression(requestingClass.peerClass(""));
        return Expression.create(
                assistedInjectionExpression.type(),
                CodeBlock.of("$L", anonymousfactoryImpl(assistedInjectionExpression)));
    }

    private TypeSpec anonymousfactoryImpl(Expression assistedInjectionExpression) {
        TypeElement factory = asType(binding.bindingElement().get());
        DeclaredType factoryType = asDeclared(binding.key().type().java());
        ExecutableElement factoryMethod = assistedFactoryMethod(factory, elements);

        // We can't use MethodSpec.overriding directly because we need to control the parameter names.
        MethodSpec factoryOverride = MethodSpec.overriding(factoryMethod, factoryType, types).build();
        TypeSpec.Builder builder =
                TypeSpec.anonymousClassBuilder("")
                        .addMethod(
                                MethodSpec.methodBuilder(factoryMethod.getSimpleName().toString())
                                        .addModifiers(factoryOverride.modifiers)
                                        .addTypeVariables(factoryOverride.typeVariables)
                                        .returns(factoryOverride.returnType)
                                        .addAnnotations(factoryOverride.annotations)
                                        .addExceptions(factoryOverride.exceptions)
                                        .addParameters(assistedFactoryParameterSpecs(binding, elements, types))
                                        .addStatement("return $L", assistedInjectionExpression.codeBlock())
                                        .build());

        if (factory.getKind() == ElementKind.INTERFACE) {
            builder.addSuperinterface(TypeName.get(factoryType));
        } else {
            builder.superclass(TypeName.get(factoryType));
        }

        return builder.build();
    }

    @AssistedFactory
    static interface Factory {
        AssistedFactoryRequestRepresentation create(ProvisionBinding binding);
    }
}
