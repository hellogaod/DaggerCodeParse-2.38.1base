package dagger.hilt.android.internal.testing;


import java.lang.reflect.InvocationTargetException;

/** Creates a test's early component. */
public abstract class EarlySingletonComponentCreator {
    private static final String EARLY_SINGLETON_COMPONENT_CREATOR_IMPL =
            "dagger.hilt.android.internal.testing.EarlySingletonComponentCreatorImpl";

    private static final String ERROR_MSG =
            "The EarlyComponent was requested but does not exist. Check that you have annotated "
                    + "your test class with @HiltAndroidTest and that the processor is running over your "
                    + "test.";

    static Object createComponent() {
        try {
            return Class.forName(EARLY_SINGLETON_COMPONENT_CREATOR_IMPL)
                    .asSubclass(EarlySingletonComponentCreator.class)
                    .getDeclaredConstructor()
                    .newInstance()
                    .create();
            // We catch each individual exception rather than using a multicatch because multi-catch will
            // get compiled to the common but new super type ReflectiveOperationException, which is not
            // allowed on API < 19. See b/187826710.
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(ERROR_MSG, e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(ERROR_MSG, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(ERROR_MSG, e);
        } catch (InstantiationException e) {
            throw new RuntimeException(ERROR_MSG, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(ERROR_MSG, e);
        }
    }

    /** Creates the early test component. */
    abstract Object create();
}
