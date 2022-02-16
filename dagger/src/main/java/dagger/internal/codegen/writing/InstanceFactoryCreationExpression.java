package dagger.internal.codegen.writing;


import com.squareup.javapoet.CodeBlock;

import java.util.function.Supplier;

import dagger.internal.InstanceFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link FrameworkFieldInitializer.FrameworkInstanceCreationExpression} that creates an {@link InstanceFactory} for an
 * instance.
 */
final class InstanceFactoryCreationExpression implements FrameworkFieldInitializer.FrameworkInstanceCreationExpression {

    private final boolean nullable;
    private final Supplier<CodeBlock> instanceExpression;

    InstanceFactoryCreationExpression(Supplier<CodeBlock> instanceExpression) {
        this(false, instanceExpression);
    }

    InstanceFactoryCreationExpression(boolean nullable, Supplier<CodeBlock> instanceExpression) {
        this.nullable = nullable;
        this.instanceExpression = checkNotNull(instanceExpression);
    }

    @Override
    public CodeBlock creationExpression() {
        return CodeBlock.of(
                "$T.$L($L)",
                InstanceFactory.class,
                nullable ? "createNullable" : "create",
                instanceExpression.get());
    }

    @Override
    public boolean useSwitchingProvider() {
        return false;
    }
}
