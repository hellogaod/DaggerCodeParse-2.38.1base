package dagger.internal.codegen.binding;

import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.base.ContributionType;
import dagger.internal.codegen.base.MapType;
import dagger.internal.codegen.base.SetType;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.multibindings.Multibinds;
import dagger.spi.model.Key;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * A declaration that a multibinding with a certain key is available to be injected in a component
 * even if the component has no multibindings for that key. Identified by a map- or set-returning
 * method annotated with {@link Multibinds @Multibinds}.
 * <p>
 * 使用Multibinds注解修饰的绑定
 */
@AutoValue
public abstract class MultibindingDeclaration extends BindingDeclaration
        implements ContributionType.HasContributionType {

    /**
     * The map or set key whose availability is declared. For maps, this will be {@code Map<K,
     * Provider<V>>}. For sets, this will be {@code Set<T>}.
     */
    @Override
    public abstract Key key();

    /**
     * {@link ContributionType#SET} if the declared type is a {@link Set}, or
     * {@link ContributionType#MAP} if it is a {@link Map}.
     */
    @Override
    public abstract ContributionType contributionType();

    @Memoized
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    /**
     * A factory for {@link MultibindingDeclaration}s.
     */
    public static final class Factory {
        private final DaggerTypes types;
        private final KeyFactory keyFactory;

        @Inject
        Factory(
                DaggerTypes types,
                KeyFactory keyFactory
        ) {
            this.types = types;
            this.keyFactory = keyFactory;
        }

        /**
         * A multibinding declaration for a {@link Multibinds @Multibinds} method.
         */
        MultibindingDeclaration forMultibindsMethod(
                ExecutableElement moduleMethod,
                TypeElement moduleElement
        ) {
            checkArgument(isAnnotationPresent(moduleMethod, Multibinds.class));

            return forDeclaredMethod(
                    moduleMethod,
                    MoreTypes.asExecutable(
                            types.asMemberOf(MoreTypes.asDeclared(moduleElement.asType()), moduleMethod)),
                    moduleElement);
        }

        private MultibindingDeclaration forDeclaredMethod(
                ExecutableElement method,
                ExecutableType methodType,
                TypeElement contributingType) {

            TypeMirror returnType = methodType.getReturnType();

            //使用@Multibinds修饰的方法的返回类型要么是Set类型要么是Map类型
            checkArgument(
                    SetType.isSet(returnType) || MapType.isMap(returnType),
                    "%s must return a set or map",
                    method);

            return new AutoValue_MultibindingDeclaration(
                    Optional.<Element>of(method),
                    Optional.of(contributingType),
                    keyFactory.forMultibindsMethod(methodType, method),
                    contributionType(returnType));
        }

        //只能是Map或Set类型，转换成ContributionType枚举状态
        private ContributionType contributionType(TypeMirror returnType) {
            if (MapType.isMap(returnType)) {
                return ContributionType.MAP;
            } else if (SetType.isSet(returnType)) {
                return ContributionType.SET;
            } else {
                throw new IllegalArgumentException("Must be Map or Set: " + returnType);
            }
        }

    }
}
