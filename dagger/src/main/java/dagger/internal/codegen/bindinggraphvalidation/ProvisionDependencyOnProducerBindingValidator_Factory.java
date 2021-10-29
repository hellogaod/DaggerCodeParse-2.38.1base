package dagger.internal.codegen.bindinggraphvalidation;


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
public final class ProvisionDependencyOnProducerBindingValidator_Factory implements Factory<ProvisionDependencyOnProducerBindingValidator> {
    @Override
    public ProvisionDependencyOnProducerBindingValidator get() {
        return newInstance();
    }

    public static ProvisionDependencyOnProducerBindingValidator_Factory create() {
        return InstanceHolder.INSTANCE;
    }

    public static ProvisionDependencyOnProducerBindingValidator newInstance() {
        return new ProvisionDependencyOnProducerBindingValidator();
    }

    private static final class InstanceHolder {
        private static final ProvisionDependencyOnProducerBindingValidator_Factory INSTANCE = new ProvisionDependencyOnProducerBindingValidator_Factory();
    }
}
