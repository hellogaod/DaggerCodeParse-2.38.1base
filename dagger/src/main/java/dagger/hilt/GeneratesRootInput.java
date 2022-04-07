package dagger.hilt;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** For annotating annotations that generate input for the {@link GenerateComponents}. */
// TODO(danysantiago): Rename to GenerateComponentsInput
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface GeneratesRootInput {}

