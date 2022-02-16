package dagger.internal;


import javax.inject.Inject;

import dagger.MembersInjector;

import static dagger.internal.Preconditions.checkNotNull;

/**
 * Basic {@link MembersInjector} implementations used by the framework.
 */
public final class MembersInjectors {
    /**
     * Returns a {@link MembersInjector} implementation that injects no members
     *
     * <p>Note that there is no verification that the type being injected does not have {@link Inject}
     * members, so care should be taken to ensure appropriate use.
     */
    @SuppressWarnings("unchecked")
    public static <T> MembersInjector<T> noOp() {
        return (MembersInjector<T>) NoOpMembersInjector.INSTANCE;
    }

    private static enum NoOpMembersInjector implements MembersInjector<Object> {
        INSTANCE;

        @Override public void injectMembers(Object instance) {
            checkNotNull(instance, "Cannot inject members into a null reference");
        }
    }

    private MembersInjectors() {}
}

