package dagger.hilt.internal.processedrootsentinel;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** An annotation used to aggregate sentinels for processed roots. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ProcessedRootSentinel {
    /** Returns the set of roots processed in a previous build. */
    String[] roots();
}

