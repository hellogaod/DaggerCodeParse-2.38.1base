package dagger.internal.codegen.base;


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
public final class ElementFormatter_Factory implements Factory<ElementFormatter> {
    @Override
    public ElementFormatter get() {
        return newInstance();
    }

    public static ElementFormatter_Factory create() {
        return InstanceHolder.INSTANCE;
    }

    public static ElementFormatter newInstance() {
        return new ElementFormatter();
    }

    private static final class InstanceHolder {
        private static final ElementFormatter_Factory INSTANCE = new ElementFormatter_Factory();
    }
}
