package dagger.producers.internal;


import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.atomic.AtomicBoolean;

import dagger.producers.Producer;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

/** An abstract {@link Producer} implementation that memoizes the result of its compute method. */
public abstract class AbstractProducer<T> implements CancellableProducer<T> {
    private final AtomicBoolean requested = new AtomicBoolean();
    private final NonExternallyCancellableFuture<T> future = new NonExternallyCancellableFuture<T>();

    protected AbstractProducer() {}

    /** Computes this producer's future, which is then cached in {@link #get}. */
    protected abstract ListenableFuture<T> compute();

    @Override
    public final ListenableFuture<T> get() {
        if (requested.compareAndSet(false, true)) {
            future.setFuture(compute());
        }
        return future;
    }

    @Override
    public final void cancel(boolean mayInterruptIfRunning) {
        requested.set(true); // Avoid potentially starting the task later only to cancel it immediately.
        future.doCancel(mayInterruptIfRunning);
    }

    @Override
    public Producer<T> newDependencyView() {
        return new NonCancellationPropagatingView();
    }

    @Override
    public Producer<T> newEntryPointView(CancellationListener cancellationListener) {
        NonCancellationPropagatingView result = new NonCancellationPropagatingView();
        result.addCancellationListener(cancellationListener);
        return result;
    }

    /**
     * A view of this producer that returns a future that can be cancelled without cancelling the
     * producer itself.
     */
    private final class NonCancellationPropagatingView implements Producer<T> {
        /**
         * An independently cancellable view of this node. Needs to be cancellable by normal future
         * cancellation so that the view at an entry point can listen for its cancellation.
         */
        private final ListenableFuture<T> viewFuture = nonCancellationPropagating(future);

        @SuppressWarnings("FutureReturnValueIgnored")
        @Override
        public ListenableFuture<T> get() {
            AbstractProducer.this.get(); // force compute()
            return viewFuture;
        }

        void addCancellationListener(final CancellationListener cancellationListener) {
            viewFuture.addListener(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (viewFuture.isCancelled()) {
                                boolean mayInterruptIfRunning =
                                        viewFuture instanceof NonCancellationPropagatingFuture
                                                && ((NonCancellationPropagatingFuture) viewFuture).interrupted();
                                cancellationListener.onProducerFutureCancelled(mayInterruptIfRunning);
                            }
                        }
                    },
                    directExecutor());
        }
    }

    /** A settable future that can't be cancelled via normal future cancellation. */
    private static final class NonExternallyCancellableFuture<T> extends AbstractFuture<T> {

        @Override
        public boolean setFuture(ListenableFuture<? extends T> future) {
            return super.setFuture(future);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        /** Actually cancels this future. */
        void doCancel(boolean mayInterruptIfRunning) {
            super.cancel(mayInterruptIfRunning);
        }
    }

    private static <T> ListenableFuture<T> nonCancellationPropagating(ListenableFuture<T> future) {
        if (future.isDone()) {
            return future;
        }
        NonCancellationPropagatingFuture<T> output = new NonCancellationPropagatingFuture<T>(future);
        future.addListener(output, directExecutor());
        return output;
    }

    /**
     * Equivalent to {@code Futures.nonCancellationPropagating}, but allowing us to check whether or
     * not {@code mayInterruptIfRunning} was set when cancelling it.
     */
    private static final class NonCancellationPropagatingFuture<T> extends AbstractFuture<T>
            implements Runnable {
        // TODO(cgdecker): This is copied directly from Producers.nonCancellationPropagating, but try
        // to find out why this doesn't need to be volatile.
        private ListenableFuture<T> delegate;

        NonCancellationPropagatingFuture(final ListenableFuture<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            // This prevents cancellation from propagating because we don't call setFuture(delegate) until
            // delegate is already done, so calling cancel() on this future won't affect it.
            ListenableFuture<T> localDelegate = delegate;
            if (localDelegate != null) {
                setFuture(localDelegate);
            }
        }

        @Override
        protected String pendingToString() {
            ListenableFuture<T> localDelegate = delegate;
            if (localDelegate != null) {
                return "delegate=[" + localDelegate + "]";
            }
            return null;
        }

        @Override
        protected void afterDone() {
            delegate = null;
        }

        public boolean interrupted() {
            return super.wasInterrupted();
        }
    }
}
