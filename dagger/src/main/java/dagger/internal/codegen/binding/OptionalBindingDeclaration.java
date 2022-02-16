package dagger.internal.codegen.binding;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;

import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import dagger.BindsOptionalOf;
import dagger.spi.model.Key;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * A {@link BindsOptionalOf} declaration.
 */
@AutoValue
abstract class OptionalBindingDeclaration extends BindingDeclaration {

    /**
     * {@inheritDoc}
     *
     * <p>The key's type is the method's return type, even though the synthetic bindings will be for
     * {@code Optional} of derived types.
     */
    @Override
    public abstract Key key();

    @Memoized
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    static class Factory {
        private final KeyFactory keyFactory;

        @Inject
        Factory(KeyFactory keyFactory) {
            this.keyFactory = keyFactory;
        }

        OptionalBindingDeclaration forMethod(ExecutableElement method, TypeElement contributingModule) {

            checkArgument(isAnnotationPresent(method, BindsOptionalOf.class));

            return new AutoValue_OptionalBindingDeclaration(
                    Optional.<Element>of(method),
                    Optional.of(contributingModule),
                    keyFactory.forBindsOptionalOfMethod(method, contributingModule));
        }
    }
}
