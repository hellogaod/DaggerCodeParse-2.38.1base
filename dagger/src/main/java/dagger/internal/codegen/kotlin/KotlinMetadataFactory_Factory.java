package dagger.internal.codegen.kotlin;


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
public final class KotlinMetadataFactory_Factory implements Factory<KotlinMetadataFactory> {
    @Override
    public KotlinMetadataFactory get() {
        return newInstance();
    }

    public static KotlinMetadataFactory_Factory create() {
        return InstanceHolder.INSTANCE;
    }

    public static KotlinMetadataFactory newInstance() {
        return new KotlinMetadataFactory();
    }

    private static final class InstanceHolder {
        private static final KotlinMetadataFactory_Factory INSTANCE = new KotlinMetadataFactory_Factory();
    }
}
