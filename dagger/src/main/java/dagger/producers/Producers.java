package dagger.producers;


import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import dagger.internal.Beta;
import dagger.producers.internal.CancellableProducer;
import dagger.producers.internal.CancellationListener;

/**
 * Utility methods to create {@link Producer}s.
 */
@Beta
public final class Producers {
    /**
     * Returns a producer that succeeds with the given value.
     */
    public static <T> Producer<T> immediateProducer(final T value) {
        return new ImmediateProducer<>(Futures.immediateFuture(value));
    }

    /**
     * Returns a producer that fails with the given exception.
     */
    public static <T> Producer<T> immediateFailedProducer(final Throwable throwable) {
        return new ImmediateProducer<>(Futures.<T>immediateFailedFuture(throwable));
    }

    /**
     * A {@link CancellableProducer} with an immediate result.
     */
    private static final class ImmediateProducer<T> implements CancellableProducer<T> {
        private final ListenableFuture<T> future;

        ImmediateProducer(ListenableFuture<T> future) {
            this.future = future;
        }

        @Override
        public ListenableFuture<T> get() {
            return future;
        }

        @Override
        public void cancel(boolean mayInterruptIfRunning) {
        }

        @Override
        public Producer<T> newDependencyView() {
            return this;
        }

        @Override
        public Producer<T> newEntryPointView(CancellationListener cancellationListener) {
            return this;
        }
    }

    private Producers() {
    }
}
