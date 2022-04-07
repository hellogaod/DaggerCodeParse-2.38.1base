package dagger.hilt.internal;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import dagger.hilt.GeneratesRootInput;

/**
 * Do not use. Only for use from Hilt generators.
 */
@Target(ElementType.TYPE)
@GeneratesRootInput
public @interface GeneratedEntryPoint {
}