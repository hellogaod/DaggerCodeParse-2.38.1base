package dagger.hilt.processor.internal;


import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Hilt annotation processor options.
 */
// TODO(danysantiago): Consider consolidating with Dagger compiler options logic.
public final class HiltCompilerOptions {

    /**
     * Returns {@code true} if the superclass validation is disabled for
     * {@link dagger.hilt.android.AndroidEntryPoint}-annotated classes.
     * <p>
     * This flag is for internal use only! The superclass validation checks that the super class is a
     * generated {@code Hilt_} class. This flag is disabled by the Hilt Gradle plugin to enable
     * bytecode transformation to change the superclass.
     */
    public static boolean isAndroidSuperclassValidationDisabled(
            TypeElement element, ProcessingEnvironment env) {
        BooleanOption option = BooleanOption.DISABLE_ANDROID_SUPERCLASS_VALIDATION;
        return option.get(env);
    }

    /**
     * Returns {@code true} if the check for {@link dagger.hilt.InstallIn} is disabled.
     */
    public static boolean isModuleInstallInCheckDisabled(ProcessingEnvironment env) {
        return BooleanOption.DISABLE_MODULES_HAVE_INSTALL_IN_CHECK.get(env);
    }

    /**
     * Returns {@code true} if fragment code should use the fixed getContext() behavior where it
     * correctly returns null after a fragment is removed. This fixed behavior matches the behavior
     * of a regular fragment and can help catch issues where a removed or leaked fragment is
     * incorrectly used.
     */
    public static boolean useFragmentGetContextFix(ProcessingEnvironment env) {
        return BooleanOption.USE_FRAGMENT_GET_CONTEXT_FIX.get(env);
    }

    /**
     * Returns {@code true} if the aggregating processor is enabled (default is {@code true}).
     *
     * <p>Note:This is for internal use only!
     */
    public static boolean useAggregatingRootProcessor(ProcessingEnvironment env) {
        return BooleanOption.USE_AGGREGATING_ROOT_PROCESSOR.get(env);
    }

    /**
     * Processor options which can have true or false values.
     */
    private enum BooleanOption {
        /**
         * Do not use! This is for internal use only.
         */
        DISABLE_ANDROID_SUPERCLASS_VALIDATION(
                "android.internal.disableAndroidSuperclassValidation", false),

        /**
         * Do not use! This is for internal use only.
         */
        USE_AGGREGATING_ROOT_PROCESSOR("internal.useAggregatingRootProcessor", true),

        DISABLE_CROSS_COMPILATION_ROOT_VALIDATION("disableCrossCompilationRootValidation", false),

        DISABLE_MODULES_HAVE_INSTALL_IN_CHECK("disableModulesHaveInstallInCheck", false),

        SHARE_TEST_COMPONENTS("shareTestComponents", false),

        USE_FRAGMENT_GET_CONTEXT_FIX("android.useFragmentGetContextFix", false);

        private final String name;
        private final boolean defaultValue;

        BooleanOption(String name, boolean defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }

        boolean get(ProcessingEnvironment env) {
            String value = env.getOptions().get(getQualifiedName());
            if (value == null) {
                return defaultValue;
            }
            // TODO(danysantiago): Strictly verify input, either 'true' or 'false' and nothing else.
            return Boolean.parseBoolean(value);
        }

        String getQualifiedName() {
            return "dagger.hilt." + name;
        }
    }


    public static Set<String> getProcessorOptions() {
        return Arrays.stream(BooleanOption.values())
                .map(BooleanOption::getQualifiedName)
                .collect(Collectors.toSet());
    }
}
