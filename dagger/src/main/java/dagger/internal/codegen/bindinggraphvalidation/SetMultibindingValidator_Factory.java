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
public final class SetMultibindingValidator_Factory implements Factory<SetMultibindingValidator> {
    @Override
    public SetMultibindingValidator get() {
        return newInstance();
    }

    public static SetMultibindingValidator_Factory create() {
        return InstanceHolder.INSTANCE;
    }

    public static SetMultibindingValidator newInstance() {
        return new SetMultibindingValidator();
    }

    private static final class InstanceHolder {
        private static final SetMultibindingValidator_Factory INSTANCE = new SetMultibindingValidator_Factory();
    }
}
