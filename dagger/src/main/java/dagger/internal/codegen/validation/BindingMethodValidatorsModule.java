package dagger.internal.codegen.validation;

import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.ClassName;

import java.util.Set;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

import static com.google.common.collect.Maps.uniqueIndex;

@Module
public interface BindingMethodValidatorsModule {

    //下面的都是该方法参数
    @Provides
    static ImmutableMap<ClassName, BindingMethodValidator> indexValidators(
            Set<BindingMethodValidator> validators) {
//        return uniqueIndex(validators, BindingMethodValidator::methodAnnotation);

        return uniqueIndex(validators, BindingMethodValidator::methodAnnotation);
    }

    @Binds
    @IntoSet
    BindingMethodValidator provides(ProvidesMethodValidator validator);

    @Binds
    @IntoSet
    BindingMethodValidator produces(ProducesMethodValidator validator);

    @Binds
    @IntoSet
    BindingMethodValidator binds(BindsMethodValidator validator);

    @Binds
    @IntoSet
    BindingMethodValidator multibinds(MultibindsMethodValidator validator);

    @Binds
    @IntoSet
    BindingMethodValidator bindsOptionalOf(BindsOptionalOfMethodValidator validator);
}
