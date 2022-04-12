package dagger.hilt.android.internal.uninstallmodules;


import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.CLASS;

/** Holds aggregated data about {@link dagger.hilt.android.testing.UninstallModules} elements. */
@Retention(CLASS)
public @interface AggregatedUninstallModules {

    /** Returns the test annotated with {@link dagger.hilt.android.testing.UninstallModules}. */
    String test();

    /** Returns the list of modules to uninstall. */
    String[] uninstallModules();
}

