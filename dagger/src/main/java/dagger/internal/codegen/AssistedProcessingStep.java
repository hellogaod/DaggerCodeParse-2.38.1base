package dagger.internal.codegen;


import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XProcessingEnv;
import androidx.room.compiler.processing.XVariableElement;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.AssistedInjectionAnnotations;
import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;
import dagger.internal.codegen.validation.ValidationReport;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static dagger.internal.codegen.langmodel.DaggerElements.closestEnclosingTypeElement;

/**
 * An annotation processor for {@link dagger.assisted.Assisted}-annotated types.
 *
 * <p>This processing step should run after {@link AssistedFactoryProcessingStep}.
 */
final class AssistedProcessingStep extends TypeCheckingProcessingStep<XVariableElement> {

    private final KotlinMetadataUtil kotlinMetadataUtil;
    private final InjectionAnnotations injectionAnnotations;
    private final DaggerElements elements;
    private final XMessager messager;
    private final XProcessingEnv processingEnv;

    @Inject
    AssistedProcessingStep(
            KotlinMetadataUtil kotlinMetadataUtil,
            InjectionAnnotations injectionAnnotations,
            DaggerElements elements,
            XMessager messager,
            XProcessingEnv processingEnv) {
        this.kotlinMetadataUtil = kotlinMetadataUtil;
        this.injectionAnnotations = injectionAnnotations;
        this.elements = elements;
        this.messager = messager;
        this.processingEnv = processingEnv;
    }

    @Override
    public ImmutableSet<ClassName> annotationClassNames() {
        return ImmutableSet.of(TypeNames.ASSISTED);
    }

    @Override
    protected void process(XVariableElement assisted, ImmutableSet<ClassName> annotations) {
        new AssistedValidator().validate(assisted).printMessagesTo(messager);
    }

    private final class AssistedValidator {

        //校验入口
        ValidationReport validate(XVariableElement assisted) {
            ValidationReport.Builder report = ValidationReport.about(assisted);

            VariableElement javaAssisted = XConverters.toJavac(assisted);
            Element enclosingElement = javaAssisted.getEnclosingElement();

            //1.@Assisted只能修饰方法参数，并且必须满足一下三个条件之一，否则报错：
            //（1）@Assisted修饰的参数，位于一个被@AssistedInject修饰的构造函数中
            //(2)@Assisted修饰的参数所在的方法是所在类中唯一的一个abstract、非static、非private修饰，并且该方法所在类使用了@AssistedFactory注解修饰
            //(3)@Assisted修饰的参数所在的方法 ，该方法命名包含"copy" ，该方法所在类是一个data类型的kotlin文件
            if (!isAssistedInjectConstructor(enclosingElement)
                    && !isAssistedFactoryCreateMethod(enclosingElement)
                    // The generated java stubs for kotlin data classes contain a "copy" method that has
                    // the same parameters (and annotations) as the constructor, so just ignore it.
                    && !isKotlinDataClassCopyMethod(enclosingElement)) {
                report.addError(
                        "@Assisted parameters can only be used within an @AssistedInject-annotated "
                                + "constructor.",
                        assisted);
            }

            //2.@Assisted修饰的参数 不能被Qualifier注解修饰的注解修饰
            injectionAnnotations
                    .getQualifiers(javaAssisted)
                    .forEach(
                            qualifier ->
                                    report.addError(
                                            "Qualifiers cannot be used with @Assisted parameters.",
                                            assisted,
                                            XConverters.toXProcessing(qualifier, processingEnv)));

            return report.build();
        }
    }

    //节点必须是被@AssistedInject注解修饰的构造函数
    private boolean isAssistedInjectConstructor(Element element) {
        return element.getKind() == ElementKind.CONSTRUCTOR
                && isAnnotationPresent(element, AssistedInject.class);
    }

    //节点必须是方法 && 该方法所在类使用了@AssistedFactory注解修饰 && 该类上有且仅有的一个abstract、非static、非private 方法就是element
    //满足以上条件返回true
    private boolean isAssistedFactoryCreateMethod(Element element) {
        if (element.getKind() == ElementKind.METHOD) {
            TypeElement enclosingElement = closestEnclosingTypeElement(element);
            return AssistedInjectionAnnotations.isAssistedFactoryType(enclosingElement)
                    // This assumes we've already validated AssistedFactory and that a valid method exists.
                    && AssistedInjectionAnnotations.assistedFactoryMethod(enclosingElement, elements)
                    .equals(element);
        }
        return false;
    }

    //节点是方法 && 节点命名包含"copy" && 节点所在类是一个data类型的kotlin文件
    private boolean isKotlinDataClassCopyMethod(Element element) {
        // Note: This is a best effort. Technically, we could check the return type and parameters of
        // the copy method to verify it's the one associated with the constructor, but I'd rather keep
        // this simple to avoid encoding too many details of kapt's stubs. At worst, we'll be allowing
        // an @Assisted annotation that has no affect, which is already true for many of Dagger's other
        // annotations.
        return element.getKind() == ElementKind.METHOD
                && element.getSimpleName().contentEquals("copy")
                && kotlinMetadataUtil.isDataClass(closestEnclosingTypeElement(element));
    }
}
