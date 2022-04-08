package dagger.hilt.android.internal.testing;


import java.lang.reflect.InvocationTargetException;

/** Stores the {@link TestComponentData} for a Hilt test class. */
public abstract class TestComponentDataSupplier {

    /** Returns a {@link TestComponentData}. */
    protected abstract TestComponentData get();

    static TestComponentData get(Class<?> testClass) {
        String generatedClassName = getEnclosedClassName(testClass) + "_TestComponentDataSupplier";
        try {
            return Class.forName(generatedClassName)
                    .asSubclass(TestComponentDataSupplier.class)
                    .getDeclaredConstructor()
                    .newInstance()
                    .get();
            // We catch each individual exception rather than using a multicatch because multi-catch will
            // get compiled to the common but new super type ReflectiveOperationException, which is not
            // allowed on API < 19. See b/187826710.
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(errorMessage(testClass, generatedClassName), e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(errorMessage(testClass, generatedClassName), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(errorMessage(testClass, generatedClassName), e);
        } catch (InstantiationException e) {
            throw new RuntimeException(errorMessage(testClass, generatedClassName), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(errorMessage(testClass, generatedClassName), e);
        }
    }

    private static String errorMessage(Class<?> testClass, String generatedClassName) {
        return String.format(
                "Hilt test, %s, is missing generated file: %s. Check that the test class is "
                        + " annotated with @HiltAndroidTest and that the processor is running over your"
                        + " test.",
                testClass.getSimpleName(),
                generatedClassName);
    }

    private static String getEnclosedClassName(Class<?> testClass) {
        StringBuilder sb = new StringBuilder();
        Class<?> currClass = testClass;
        while (currClass != null) {
            Class<?> enclosingClass = currClass.getEnclosingClass();
            if (enclosingClass != null) {
                sb.insert(0, "_" + currClass.getSimpleName());
            } else {
                sb.insert(0, currClass.getCanonicalName());
            }
            currClass = enclosingClass;
        }
        return sb.toString();
    }
}
