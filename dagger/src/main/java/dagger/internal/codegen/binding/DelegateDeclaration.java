package dagger.internal.codegen.binding;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.base.Equivalence;
import com.google.common.collect.Iterables;

import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;

import dagger.Binds;
import dagger.internal.codegen.base.ContributionType;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.DependencyRequest;

import static com.google.common.base.Preconditions.checkArgument;
import static dagger.internal.codegen.base.MoreAnnotationMirrors.wrapOptionalInEquivalence;
import static dagger.internal.codegen.binding.MapKeys.getMapKey;

/**
 * The declaration for a delegate binding established by a {@link Binds} method.
 * <p>
 * 使用Binds修饰的绑定方法
 */
@AutoValue
public abstract class DelegateDeclaration extends BindingDeclaration
        implements ContributionType.HasContributionType {

    abstract DependencyRequest delegateRequest();//Binds绑定的方法参数（有且仅有一个）生成的依赖

    abstract Optional<Equivalence.Wrapper<AnnotationMirror>> wrappedMapKey();//Binds绑定的方法是否还使用了MapKey注解修饰的注解

    @Memoized
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    /**
     * A {@link DelegateDeclaration} factory.
     * <p>
     * 用于创建DelegateDeclaration对象的工厂
     */
    public static final class Factory {
        private final DaggerTypes types;
        private final KeyFactory keyFactory;
        private final DependencyRequestFactory dependencyRequestFactory;

        @Inject
        Factory(
                DaggerTypes types,
                KeyFactory keyFactory,
                DependencyRequestFactory dependencyRequestFactory
        ) {
            this.types = types;
            this.keyFactory = keyFactory;
            this.dependencyRequestFactory = dependencyRequestFactory;
        }

        //核心入口
        public DelegateDeclaration create(
                ExecutableElement bindsMethod,
                TypeElement contributingModule
        ) {
            //判断当前方法必须使用Binds注解修饰
            checkArgument(MoreElements.isAnnotationPresent(bindsMethod, Binds.class));

            ExecutableType resolvedMethod =
                    MoreTypes.asExecutable(
                            types.asMemberOf(MoreTypes.asDeclared(contributingModule.asType()), bindsMethod));

            //当前方法的方法参数和方法参数类型生成一个依赖对象
            DependencyRequest delegateRequest =
                    dependencyRequestFactory.forRequiredResolvedVariable(
                            Iterables.getOnlyElement(bindsMethod.getParameters()),
                            Iterables.getOnlyElement(resolvedMethod.getParameterTypes()));

            return new AutoValue_DelegateDeclaration(
                    ContributionType.fromBindingElement(bindsMethod),
                    keyFactory.forBindsMethod(bindsMethod, contributingModule),
                    Optional.<Element>of(bindsMethod),
                    Optional.of(contributingModule),
                    delegateRequest,
                    wrapOptionalInEquivalence(getMapKey(bindsMethod)));
        }
    }

}
