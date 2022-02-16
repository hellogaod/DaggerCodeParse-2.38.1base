package dagger.internal.codegen.binding;


import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.common.base.Equivalence;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.langmodel.DaggerTypes;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;

/**
 * A class that defines proper {@code equals} and {@code hashcode} for a method signature.
 */
@AutoValue
public abstract class MethodSignature {

    abstract String name();

    abstract ImmutableList<? extends Equivalence.Wrapper<? extends TypeMirror>> parameterTypes();

    abstract ImmutableList<? extends Equivalence.Wrapper<? extends TypeMirror>> thrownTypes();

    public static MethodSignature forComponentMethod(
            ComponentDescriptor.ComponentMethodDescriptor componentMethod, DeclaredType componentType, DaggerTypes types) {
        ExecutableType methodType =
                MoreTypes.asExecutable(types.asMemberOf(componentType, componentMethod.methodElement()));
        return new AutoValue_MethodSignature(
                componentMethod.methodElement().getSimpleName().toString(),
                wrapInEquivalence(methodType.getParameterTypes()),
                wrapInEquivalence(methodType.getThrownTypes()));
    }

    private static ImmutableList<? extends Equivalence.Wrapper<? extends TypeMirror>>
    wrapInEquivalence(List<? extends TypeMirror> types) {
        return types.stream().map(MoreTypes.equivalence()::wrap).collect(toImmutableList());
    }

}
