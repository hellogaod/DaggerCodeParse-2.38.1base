package dagger.hilt.internal.componenttreedeps;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dagger.hilt.android.internal.earlyentrypoint.AggregatedEarlyEntryPoint;
import dagger.hilt.processor.internal.aggregateddeps.AggregatedDeps;
import dagger.hilt.processor.internal.uninstallmodules.AggregatedUninstallModulesMetadata;

/** An annotation that kicks off the generation of a component tree. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ComponentTreeDeps {

    /** Returns the set of {@link dagger.hilt.internal.aggregatedroot.AggregatedRoot} dependencies. */
    Class<?>[] rootDeps() default {};

    /**
     * Returns the set of {@link dagger.hilt.internal.definecomponent.DefineComponentClasses}
     * dependencies.
     */
    Class<?>[] defineComponentDeps() default {};

    /** Returns the set of {@link dagger.hilt.internal.aliasof.AliasOfPropagatedData} dependencies. */
    Class<?>[] aliasOfDeps() default {};

    /** Returns the set of {@link AggregatedDeps} dependencies. */
    Class<?>[] aggregatedDeps() default {};

    /**
     * Returns the set of {@link
     * AggregatedUninstallModulesMetadata} dependencies.
     */
    Class<?>[] uninstallModulesDeps() default {};

    /**
     * Returns the set of {@link AggregatedEarlyEntryPoint}
     * dependencies.
     */
    Class<?>[] earlyEntryPointDeps() default {};
}
