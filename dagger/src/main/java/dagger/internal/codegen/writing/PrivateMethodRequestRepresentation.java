package dagger.internal.codegen.writing;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.RequestKind;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static dagger.internal.codegen.writing.ComponentImplementation.MethodSpecKind.PRIVATE_METHOD;
import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * A binding expression that wraps the dependency expressions in a private, no-arg method.
 *
 * <p>Dependents of this binding expression will just call the no-arg private method.
 * <p>
 * BindingRequest的RequestKind是INSTANCE，被key匹配ProvisionBinding对象存在依赖（@Inject构造函数存在参数，@Provides修饰的bindingMethod方法存在参数），会携带RequestRepresentation对象
 */
final class PrivateMethodRequestRepresentation extends MethodRequestRepresentation {
    private final ComponentImplementation.ShardImplementation shardImplementation;
    private final ContributionBinding binding;
    private final BindingRequest request;
    private final RequestRepresentation wrappedRequestRepresentation;
    private final CompilerOptions compilerOptions;
    private final DaggerTypes types;
    private String methodName;

    @AssistedInject
    PrivateMethodRequestRepresentation(
            @Assisted BindingRequest request,
            @Assisted ContributionBinding binding,
            @Assisted RequestRepresentation wrappedRequestRepresentation,
            ComponentImplementation componentImplementation,
            DaggerTypes types,
            CompilerOptions compilerOptions) {
        super(componentImplementation.shardImplementation(binding), types);
        this.binding = checkNotNull(binding);
        this.request = checkNotNull(request);
        this.wrappedRequestRepresentation = checkNotNull(wrappedRequestRepresentation);
        this.shardImplementation = componentImplementation.shardImplementation(binding);
        this.compilerOptions = compilerOptions;
        this.types = types;
    }

    @Override
    protected CodeBlock methodCall() {
        return CodeBlock.of("$N()", methodName());
    }

    @Override
    protected TypeMirror returnType() {
        if (request.isRequestKind(RequestKind.INSTANCE)
                && binding.contributedPrimitiveType().isPresent()) {
            //方法的返回类型
            return binding.contributedPrimitiveType().get();
        }

        //绑定对象的key中的type类型 e.g.AssistedFactoryRequestRepresentation.Factory
        TypeMirror requestedType = request.requestedType(binding.contributedType(), types);
        return types.accessibleType(requestedType, shardImplementation.name());
    }

    private String methodName() {
        if (methodName == null) {
            // Have to set methodName field before implementing the method in order to handle recursion.
            methodName = shardImplementation.getUniqueMethodName(request);

            // TODO(bcorso): Fix the order that these generated methods are written to the component.

            shardImplementation.addMethod(
                    PRIVATE_METHOD,
                    methodBuilder(methodName)
                            .addModifiers(PRIVATE)
                            .returns(TypeName.get(returnType()))
                            .addStatement(
                                    "return $L",
                                    wrappedRequestRepresentation
                                            .getDependencyExpression(shardImplementation.name())
                                            .codeBlock())
                            .build());
        }
        return methodName;
    }

    @AssistedFactory
    static interface Factory {
        PrivateMethodRequestRepresentation create(
                BindingRequest request,
                ContributionBinding binding,
                RequestRepresentation wrappedRequestRepresentation);
    }
}
