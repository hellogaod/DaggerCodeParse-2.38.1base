package dagger.hilt.android.internal;


import android.os.Looper;

/** Thread utility methods. */
public final class ThreadUtil {

    private static Thread mainThread;

    private ThreadUtil() {}

    /** Returns true if the current thread is the Main thread. */
    public static boolean isMainThread() {
        if (mainThread == null) {
            mainThread = Looper.getMainLooper().getThread();
        }
        return Thread.currentThread() == mainThread;
    }

    /** Checks that the current thread is the Main thread. Otherwise throws an exception. */
    public static void ensureMainThread() {
        if (!isMainThread()) {
            throw new IllegalStateException("Must be called on the Main thread.");
        }
    }
}
