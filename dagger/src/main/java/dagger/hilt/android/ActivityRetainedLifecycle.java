package dagger.hilt.android;

import android.app.Activity;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

/**
 * A <code>ActivityRetainedLifecycle</code> class is associated with the lifecycle of the {@link
 * dagger.hilt.android.components.ActivityRetainedComponent}.
 */
public interface ActivityRetainedLifecycle {

    /**
     * Adds a new {@link OnClearedListener} for receiving a callback when the activity retained
     * instances will no longer be needed and destroyed.
     *
     * @param listener The listener that should be added.
     */
    @MainThread
    void addOnClearedListener(@NonNull OnClearedListener listener);

    /**
     * Removes a {@link OnClearedListener} previously added via {@link
     * #addOnClearedListener(OnClearedListener)}.
     *
     * @param listener The listener that should be removed.
     */
    @MainThread
    void removeOnClearedListener(@NonNull OnClearedListener listener);

    /**
     * Listener for receiving a callback for when the {@link
     * dagger.hilt.android.components.ActivityRetainedComponent} will no longer be used and destroyed.
     */
    interface OnClearedListener {

        /**
         * Called when the activity retained instances will no longer be used and destroyed.
         *
         * <p>Specifically this will be invoked during {@link Activity#onDestroy()} when {@link
         * Activity#isChangingConfigurations} is false.
         */
        void onCleared();
    }
}
