package dagger.hilt.android.internal.managers;


import android.app.Activity;
import android.app.Application;

import androidx.activity.ComponentActivity;
import dagger.hilt.EntryPoint;
import dagger.hilt.EntryPoints;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityRetainedComponent;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.internal.GeneratedComponentManager;

/**
 * Do not use except in Hilt generated code!
 *
 * <p>A manager for the creation of components that live in the Activity.
 *
 * <p>Note: This class is not typed since its type in generated code is always <?> or <Object>. This
 * is mainly due to the fact that we don't know the components at the time of generation, and
 * because even the injector interface type is not a valid type if we have a hilt base class.
 */
public class ActivityComponentManager implements GeneratedComponentManager<Object> {

    /**
     * Entrypoint for {@link ActivityComponentBuilder}.
     */
    @EntryPoint
    @InstallIn(ActivityRetainedComponent.class)
    public interface ActivityComponentBuilderEntryPoint {
        ActivityComponentBuilder activityComponentBuilder();
    }

    private volatile Object component;
    private final Object componentLock = new Object();

    protected final Activity activity;

    private final GeneratedComponentManager<ActivityRetainedComponent>
            activityRetainedComponentManager;

    public ActivityComponentManager(Activity activity) {
        this.activity = activity;
        this.activityRetainedComponentManager =
                new ActivityRetainedComponentManager((ComponentActivity) activity);
    }

    @Override
    public Object generatedComponent() {
        if (component == null) {
            synchronized (componentLock) {
                if (component == null) {
                    component = createComponent();
                }
            }
        }
        return component;
    }

    protected Object createComponent() {
        if (!(activity.getApplication() instanceof GeneratedComponentManager)) {
            if (Application.class.equals(activity.getApplication().getClass())) {
                throw new IllegalStateException(
                        "Hilt Activity must be attached to an @HiltAndroidApp Application. "
                                + "Did you forget to specify your Application's class name in your manifest's "
                                + "<application />'s android:name attribute?");
            }
            throw new IllegalStateException(
                    "Hilt Activity must be attached to an @AndroidEntryPoint Application. Found: "
                            + activity.getApplication().getClass());
        }

        return EntryPoints.get(
                activityRetainedComponentManager, ActivityComponentBuilderEntryPoint.class)
                .activityComponentBuilder()
                .activity(activity)
                .build();
    }
}
