package dagger.internal.codegen.validation;

import javax.annotation.Generated;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class ValidationBindingGraphPlugins_Factory implements Factory<ValidationBindingGraphPlugins> {

    public ValidationBindingGraphPlugins_Factory() {
    }

    @Override
    public ValidationBindingGraphPlugins get() {
        return newInstance();
    }

    public static ValidationBindingGraphPlugins_Factory create() {
        return new ValidationBindingGraphPlugins_Factory();
    }

    public static ValidationBindingGraphPlugins newInstance() {
        return new ValidationBindingGraphPlugins();
    }
}
