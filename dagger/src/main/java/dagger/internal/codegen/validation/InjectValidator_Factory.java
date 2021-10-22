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
public final class InjectValidator_Factory implements Factory<InjectValidator> {

    public InjectValidator_Factory(){}

    @Override
    public InjectValidator get() {
        return newInstance();
    }

    public static InjectValidator_Factory create() {
        return new InjectValidator_Factory();
    }

    public static InjectValidator newInstance() {
        return new InjectValidator();
    }
}
