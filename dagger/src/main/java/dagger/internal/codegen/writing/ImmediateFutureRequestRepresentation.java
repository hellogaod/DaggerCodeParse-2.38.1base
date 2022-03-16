package dagger.internal.codegen.writing;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import javax.lang.model.SourceVersion;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.Key;
import dagger.spi.model.RequestKind;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;
//当前被key匹配上的是ProvisionBinding对象，并且Binding绑定对象bindtype属性是FUTURE
final class ImmediateFutureRequestRepresentation extends RequestRepresentation {
    private final Key key;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerTypes types;
    private final SourceVersion sourceVersion;

    @AssistedInject
    ImmediateFutureRequestRepresentation(
            @Assisted Key key,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types,
            SourceVersion sourceVersion) {
        this.key = key;
        this.componentRequestRepresentations = checkNotNull(componentRequestRepresentations);
        this.types = checkNotNull(types);
        this.sourceVersion = checkNotNull(sourceVersion);
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {
        return Expression.create(
                types.wrapType(key.type().java(), ListenableFuture.class),
                CodeBlock.of("$T.immediateFuture($L)", Futures.class, instanceExpression(requestingClass)));
    }

    private CodeBlock instanceExpression(ClassName requestingClass) {
        Expression expression =
                componentRequestRepresentations.getDependencyExpression(
                        bindingRequest(key, RequestKind.INSTANCE), requestingClass);
        if (sourceVersion.compareTo(SourceVersion.RELEASE_7) <= 0) {
            // Java 7 type inference is not as strong as in Java 8, and therefore some generated code must
            // cast.
            //
            // For example, javac7 cannot detect that Futures.immediateFuture(ImmutableSet.of("T"))
            // can safely be assigned to ListenableFuture<Set<T>>.
            if (!types.isSameType(expression.type(), key.type().java())) {
                return CodeBlock.of(
                        "($T) $L",
                        types.accessibleType(key.type().java(), requestingClass),
                        expression.codeBlock());
            }
        }
        return expression.codeBlock();
    }

    @AssistedFactory
    static interface Factory {
        ImmediateFutureRequestRepresentation create(Key key);
    }
}
