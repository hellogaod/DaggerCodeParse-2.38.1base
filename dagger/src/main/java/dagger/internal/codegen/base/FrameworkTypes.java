package dagger.internal.codegen.base;


import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

import javax.inject.Provider;
import javax.lang.model.type.TypeMirror;

import dagger.Lazy;
import dagger.MembersInjector;
import dagger.producers.Produced;
import dagger.producers.Producer;

import static com.google.auto.common.MoreTypes.isType;

/**
 * A collection of utility methods for dealing with Dagger framework types. A framework type is any
 * type that the framework itself defines.
 */
public final class FrameworkTypes {

    private static final ImmutableSet<Class<?>> PROVISION_TYPES =
            ImmutableSet.of(Provider.class, Lazy.class, MembersInjector.class);

    // NOTE(beder): ListenableFuture is not considered a producer framework type because it is not
    // defined by the framework, so we can't treat it specially in ordinary Dagger.
    private static final ImmutableSet<Class<?>> PRODUCTION_TYPES =
            ImmutableSet.of(Produced.class, Producer.class);


    /**
     * Returns true if the type represents a producer-related framework type.
     */
    public static boolean isProducerType(TypeMirror type) {
        return isType(type) && typeIsOneOf(PRODUCTION_TYPES, type);
    }

    /**
     * Returns true if the type represents a framework type.
     */
    public static boolean isFrameworkType(TypeMirror type) {
        return isType(type) &&
                (typeIsOneOf(PROVISION_TYPES, type)
                        || typeIsOneOf(PRODUCTION_TYPES, type)
                );
    }

    private static boolean typeIsOneOf(Set<Class<?>> classes, TypeMirror type) {
        for (Class<?> clazz : classes) {
            if (MoreTypes.isTypeOf(clazz, type)) {
                return true;
            }
        }
        return false;
    }

    private FrameworkTypes() {
    }
}
