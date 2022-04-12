package dagger.hilt.internal.componenttreedeps;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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

    /** Returns the set of {@link dagger.hilt.internal.aggregateddeps.AggregatedDeps} dependencies. */
    Class<?>[] aggregatedDeps() default {};

    /**
     * Returns the set of {@link
     * dagger.hilt.internal.uninstallmodules.AggregatedUninstallModulesMetadata} dependencies.
     */
    Class<?>[] uninstallModulesDeps() default {};

    /**
     * Returns the set of {@link dagger.hilt.android.earlyentrypoint.AggregatedEarlyEntryPoint}
     * dependencies.
     */
    Class<?>[] earlyEntryPointDeps() default {};
}
