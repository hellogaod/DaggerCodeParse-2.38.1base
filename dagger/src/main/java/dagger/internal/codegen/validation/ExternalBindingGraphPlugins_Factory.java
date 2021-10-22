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
public final class ExternalBindingGraphPlugins_Factory implements Factory<ExternalBindingGraphPlugins> {
    public ExternalBindingGraphPlugins_Factory() {
    }

    @Override
    public ExternalBindingGraphPlugins get() {
        return newInstance();
    }

    public static ExternalBindingGraphPlugins_Factory create() {
        return new ExternalBindingGraphPlugins_Factory();
    }

    public static ExternalBindingGraphPlugins newInstance() {
        return new ExternalBindingGraphPlugins();
    }
}
