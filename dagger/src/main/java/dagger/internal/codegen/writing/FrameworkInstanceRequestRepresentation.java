package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.FrameworkType;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.langmodel.Accessibility.isTypeAccessibleFrom;

/**
 * A binding expression that uses a {@link FrameworkType} field.
 */
abstract class FrameworkInstanceRequestRepresentation extends RequestRepresentation {
    private final ContributionBinding binding;
    private final FrameworkInstanceSupplier frameworkInstanceSupplier;
    private final DaggerTypes types;
    private final DaggerElements elements;

    FrameworkInstanceRequestRepresentation(
            ContributionBinding binding,
            FrameworkInstanceSupplier frameworkInstanceSupplier,
            DaggerTypes types,
            DaggerElements elements) {
        this.binding = checkNotNull(binding);
        this.frameworkInstanceSupplier = checkNotNull(frameworkInstanceSupplier);
        this.types = checkNotNull(types);
        this.elements = checkNotNull(elements);
    }

    /**
     * The expression for the framework instance for this binding. The field will be initialized and
     * added to the component the first time this method is invoked.
     */
    @Override
    Expression getDependencyExpression(ClassName requestingClass) {

        //获取LocalField对象
        MemberSelect memberSelect = frameworkInstanceSupplier.memberSelect();
        TypeMirror expressionType =
                isTypeAccessibleFrom(binding.contributedType(), requestingClass.packageName())
                        || isInlinedFactoryCreation(memberSelect)
                        ? types.wrapType(binding.contributedType(), frameworkType().frameworkClass())
                        : rawFrameworkType();
        return Expression.create(expressionType, memberSelect.getExpressionFor(requestingClass));
    }

    /**
     * Returns the framework type for the binding.
     */
    protected abstract FrameworkType frameworkType();

    /**
     * Returns {@code true} if a factory is created inline each time it is requested. For example, in
     * the initialization {@code this.fooProvider = Foo_Factory.create(Bar_Factory.create());}, {@code
     * Bar_Factory} is considered to be inline.
     *
     * <p>This is used in {@link #getDependencyExpression(ClassName)} when determining the type of a
     * factory. Normally if the {@link ContributionBinding#contributedType()} is not accessible from
     * the component, the type of the expression will be a raw {@link javax.inject.Provider}. However,
     * if the factory is created inline, even if contributed type is not accessible, javac will still
     * be able to determine the type that is returned from the {@code Foo_Factory.create()} method.
     */
    private static boolean isInlinedFactoryCreation(MemberSelect memberSelect) {
        return memberSelect.staticMember();
    }

    private DeclaredType rawFrameworkType() {
        return types.getDeclaredType(elements.getTypeElement(frameworkType().frameworkClass()));
    }
}
