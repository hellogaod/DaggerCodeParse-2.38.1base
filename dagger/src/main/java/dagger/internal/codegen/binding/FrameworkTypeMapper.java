package dagger.internal.codegen.binding;


import javax.inject.Provider;

import dagger.producers.Producer;
import dagger.spi.model.RequestKind;

import static dagger.internal.codegen.binding.BindingType.PRODUCTION;

/**
 * A mapper for associating a {@link RequestKind} to a {@link FrameworkType}, dependent on the type
 * of code to be generated (e.g., for {@link Provider} or {@link Producer}).
 */
public enum FrameworkTypeMapper {
    FOR_PROVIDER() {
        @Override
        public FrameworkType getFrameworkType(RequestKind requestKind) {
            switch (requestKind) {
                case INSTANCE:
                case PROVIDER:
                case PROVIDER_OF_LAZY:
                case LAZY:
                    return FrameworkType.PROVIDER;
                case PRODUCED:
                case PRODUCER:
                    throw new IllegalArgumentException(requestKind.toString());
                default:
                    throw new AssertionError(requestKind);
            }
        }
    },
    FOR_PRODUCER() {
        @Override
        public FrameworkType getFrameworkType(RequestKind requestKind) {
            switch (requestKind) {
                case INSTANCE:
                case PRODUCED:
                case PRODUCER:
                    return FrameworkType.PRODUCER_NODE;
                case PROVIDER:
                case PROVIDER_OF_LAZY:
                case LAZY:
                    return FrameworkType.PROVIDER;
                default:
                    throw new AssertionError(requestKind);
            }
        }
    };

    public static FrameworkTypeMapper forBindingType(BindingType bindingType) {
        return bindingType.equals(PRODUCTION) ? FOR_PRODUCER : FOR_PROVIDER;
    }

    public abstract FrameworkType getFrameworkType(RequestKind requestKind);
}
