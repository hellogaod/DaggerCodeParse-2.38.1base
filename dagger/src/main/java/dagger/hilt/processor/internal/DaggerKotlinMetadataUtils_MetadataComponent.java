package dagger.hilt.processor.internal;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.codegen.kotlin.KotlinMetadataFactory;
import dagger.internal.codegen.kotlin.KotlinMetadataFactory_Factory;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil_Factory;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
final class DaggerKotlinMetadataUtils_MetadataComponent implements KotlinMetadataUtils.MetadataComponent {
    private final DaggerKotlinMetadataUtils_MetadataComponent metadataComponent = this;

    private Provider<KotlinMetadataFactory> kotlinMetadataFactoryProvider;

    private DaggerKotlinMetadataUtils_MetadataComponent() {

        initialize();

    }

    public static Builder builder() {
        return new Builder();
    }

    public static KotlinMetadataUtils.MetadataComponent create() {
        return new Builder().build();
    }

    @SuppressWarnings("unchecked")
    private void initialize() {
        this.kotlinMetadataFactoryProvider = DoubleCheck.provider(KotlinMetadataFactory_Factory.create());
    }

    @Override
    public KotlinMetadataUtil get() {
        return KotlinMetadataUtil_Factory.newInstance(kotlinMetadataFactoryProvider.get());
    }

    static final class Builder {
        private Builder() {
        }

        public KotlinMetadataUtils.MetadataComponent build() {
            return new DaggerKotlinMetadataUtils_MetadataComponent();
        }
    }
}
