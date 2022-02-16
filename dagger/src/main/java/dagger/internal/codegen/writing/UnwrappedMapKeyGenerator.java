package dagger.internal.codegen.writing;

import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import androidx.room.compiler.processing.XFiler;
import dagger.MapKey;
import dagger.internal.codegen.langmodel.DaggerElements;

/**
 * Generates classes that create annotation instances for an unwrapped {@link MapKey} annotation
 * type whose nested value is an annotation. The generated class will have a private empty
 * constructor and a static method that creates each annotation type that is nested in the top-level
 * annotation type.
 *
 * <p>So for an example {@link MapKey} annotation:
 *
 * <pre>
 *   {@literal @MapKey}(unwrapValue = true)
 *   {@literal @interface} Foo {
 *     Bar bar();
 *   }
 *
 *   {@literal @interface} Bar {
 *     {@literal Class<?> baz();}
 *   }
 * </pre>
 * <p>
 * the generated class will look like:
 *
 * <pre>
 *   public final class FooCreator {
 *     private FooCreator() {}
 *
 *     public static Bar createBar({@literal Class<?> baz}) { … }
 *   }
 * </pre>
 */
public final class UnwrappedMapKeyGenerator extends AnnotationCreatorGenerator {

    @Inject
    UnwrappedMapKeyGenerator(XFiler filer, DaggerElements elements, SourceVersion sourceVersion) {
        super(filer, elements, sourceVersion);
    }

    //遍历annotationElement里面的所有方法，如果方法返回类型还是注解类，那么收集起来，并且收集的集合删除当前annotationElement节点
    @Override
    protected Set<TypeElement> annotationsToCreate(TypeElement annotationElement) {
        Set<TypeElement> nestedAnnotationElements = super.annotationsToCreate(annotationElement);
        nestedAnnotationElements.remove(annotationElement);
        return nestedAnnotationElements;
    }
}
