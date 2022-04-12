package dagger.hilt.android.internal.earlyentrypoint;


import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.CLASS;

/** Holds aggregated data about {@link dagger.hilt.android.EarlyEntryPoint} elements. */
@Retention(CLASS)
public @interface AggregatedEarlyEntryPoint {

    /** Returns the entry point annotated with {@link dagger.hilt.android.EarlyEntryPoint}. */
    String earlyEntryPoint();
}
