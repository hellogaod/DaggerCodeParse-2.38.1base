package dagger.internal.codegen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

import androidx.room.compiler.processing.XExecutableElement;
import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XProcessingEnv;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.internal.codegen.binding.AssistedInjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;
import dagger.internal.codegen.validation.ValidationReport;

import static com.google.auto.common.MoreTypes.asDeclared;
import static com.google.common.base.Preconditions.checkState;
import static dagger.internal.codegen.langmodel.DaggerElements.closestEnclosingTypeElement;

/**
 * An annotation processor for {@link dagger.assisted.AssistedInject}-annotated elements.
 */
final class AssistedInjectProcessingStep extends TypeCheckingProcessingStep<XExecutableElement> {

    private final DaggerTypes types;
    private final XMessager messager;
    private final XProcessingEnv processingEnv;

    @Inject
    AssistedInjectProcessingStep(
            DaggerTypes types,
            XMessager messager,
            XProcessingEnv processingEnv
    ) {
        this.types = types;
        this.messager = messager;
        this.processingEnv = processingEnv;
    }

    @Override
    public ImmutableSet<ClassName> annotationClassNames() {
        return ImmutableSet.of(TypeNames.ASSISTED_INJECT);
    }

    @Override
    protected void process(
            XExecutableElement assistedInjectElement, ImmutableSet<ClassName> annotations) {
        new AssistedInjectValidator().validate(assistedInjectElement).printMessagesTo(messager);
    }

    //1.AssistedInject必须修饰的是构造函数；
    // 2.AssistedInject修饰的构造函数里面的参数使用@Assited修饰进行校验，不允许重复（Assited#value和类型都一样情况）
    private final class AssistedInjectValidator {
        //入口，
        ValidationReport validate(XExecutableElement constructor) {

            //1.AssistedInject注解仅仅可以修饰构造函数
            ExecutableElement javaConstructor = XConverters.toJavac(constructor);
            checkState(javaConstructor.getKind() == ElementKind.CONSTRUCTOR);

            ValidationReport.Builder report = ValidationReport.about(constructor);

            DeclaredType assistedInjectType =
                    asDeclared(closestEnclosingTypeElement(javaConstructor).asType());

            //当前使用AssistedInject修饰的构造函数的参数，如果该参数使用了Assisted修饰了，使用该收集器收集
            ImmutableList<AssistedInjectionAnnotations.AssistedParameter> assistedParameters =
                    AssistedInjectionAnnotations.assistedInjectAssistedParameters(assistedInjectType, types);

            Set<AssistedInjectionAnnotations.AssistedParameter> uniqueAssistedParameters = new HashSet<>();

            //本step核心：校验使用AssistedInject修饰的构造函数，该函数的参数如果使用Assisted修饰，那么不允许出现重复参数
            //例如C （@Assited A a,Assited A b）,因为a和b的Assited#value相同，并且类型都是A。所以会报错
            for (AssistedInjectionAnnotations.AssistedParameter assistedParameter : assistedParameters) {
                if (!uniqueAssistedParameters.add(assistedParameter)) {
                    report.addError(
                            String.format("@AssistedInject constructor has duplicate @Assisted type: %s. "
                                            + "Consider setting an identifier on the parameter by using "
                                            + "@Assisted(\"identifier\") in both the factory and @AssistedInject constructor",
                                    assistedParameter),
                            XConverters.toXProcessing(assistedParameter.variableElement(), processingEnv));
                }
            }

            return report.build();
        }
    }
}
