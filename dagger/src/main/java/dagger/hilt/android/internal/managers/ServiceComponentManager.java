package dagger.hilt.android.internal.managers;


import android.app.Application;
import android.app.Service;

import dagger.hilt.EntryPoint;
import dagger.hilt.EntryPoints;
import dagger.hilt.InstallIn;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedComponentManager;
import dagger.hilt.internal.Preconditions;

/**
 * Do not use except in Hilt generated code!
 *
 * <p>A manager for the creation of components that live in the Service.
 *
 * <p>Note: This class is not typed since its type in generated code is always <?> or <Object>. This
 * is mainly due to the fact that we don't know the components at the time of generation, and
 * because even the injector interface type is not a valid type if we have a hilt base class.
 */
public final class ServiceComponentManager implements GeneratedComponentManager<Object> {
    /**
     * Entrypoint for {@link ServiceComponentBuilder}.
     */
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    public interface ServiceComponentBuilderEntryPoint {
        ServiceComponentBuilder serviceComponentBuilder();
    }

    private final Service service;
    private Object component;

    public ServiceComponentManager(Service service) {
        this.service = service;
    }

    // This isn't ever really publicly exposed on a service so it should be fine without
    // synchronization.
    @Override
    public Object generatedComponent() {
        if (component == null) {
            component = createComponent();
        }
        return component;
    }

    private Object createComponent() {
        Application application = service.getApplication();
        Preconditions.checkState(
                application instanceof GeneratedComponentManager,
                "Hilt service must be attached to an @AndroidEntryPoint Application. Found: %s",
                application.getClass());

        return EntryPoints.get(application, ServiceComponentBuilderEntryPoint.class)
                .serviceComponentBuilder()
                .service(service)
                .build();
    }
}
