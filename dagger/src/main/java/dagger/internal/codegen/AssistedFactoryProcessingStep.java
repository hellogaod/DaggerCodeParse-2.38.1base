package dagger.internal.codegen;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import androidx.room.compiler.processing.XFiler;
import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.internal.codegen.binding.AssistedInjectionAnnotations;
import dagger.internal.codegen.binding.BindingFactory;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import androidx.room.compiler.processing.XProcessingEnv;

import dagger.internal.codegen.validation.TypeCheckingProcessingStep;
import dagger.internal.codegen.validation.ValidationReport;

import static com.google.auto.common.MoreTypes.asTypeElement;
import static java.util.stream.Collectors.joining;

/**
 * An annotation processor for {@link dagger.assisted.AssistedFactory}-annotated types.
 */
final class AssistedFactoryProcessingStep extends TypeCheckingProcessingStep<XTypeElement> {

    private final XProcessingEnv processingEnv;
    private final XMessager messager;
    private final XFiler filer;
    private final SourceVersion sourceVersion;
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final BindingFactory bindingFactory;

    @Inject
    AssistedFactoryProcessingStep(
            XProcessingEnv processingEnv,
            XMessager messager,
            XFiler filer,
            SourceVersion sourceVersion,
            DaggerElements elements,
            DaggerTypes types,
            BindingFactory bindingFactory
    ) {
        this.processingEnv = processingEnv;
        this.messager = messager;
        this.filer = filer;
        this.sourceVersion = sourceVersion;
        this.elements = elements;
        this.types = types;
        this.bindingFactory = bindingFactory;
    }

    @Override
    public ImmutableSet<ClassName> annotationClassNames() {
        return ImmutableSet.of(TypeNames.ASSISTED_FACTORY);
    }

    @Override
    protected void process(XTypeElement factory, ImmutableSet<ClassName> annotations) {

        ValidationReport report = new AssistedFactoryValidator().validate(factory);
        report.printMessagesTo(messager);
//        if (report.isClean()) {
//
//        }
    }

    private final class AssistedFactoryValidator {

        //入口，校验使用AssistedFactory修饰的类或接口
        ValidationReport validate(XTypeElement factory) {

            ValidationReport.Builder report = ValidationReport.about(factory);

            //1.使用AssistedFactory修饰的类型仅仅支持抽象类或接口
            if (!factory.isAbstract()) {
                return report
                        .addError(
                                "The @AssistedFactory-annotated type must be either an abstract class or "
                                        + "interface.",
                                factory)
                        .build();
            }

            TypeElement javaFactory = XConverters.toJavac(factory);

            //2.如果AssistedFactory修饰的元素是内部类，那么必须使用static修饰
            if (javaFactory.getNestingKind().isNested() && !factory.isStatic()) {
                report.addError("Nested @AssistedFactory-annotated types must be static. ", factory);
            }

            //节点abstract、非static、非private的方法
            ImmutableSet<ExecutableElement> abstractFactoryMethods =
                    AssistedInjectionAnnotations.assistedFactoryMethods(javaFactory, elements);

            //3.如果AssistedFactory修饰的元素必须包含abstract、非static、非private的方法
            if (abstractFactoryMethods.isEmpty()) {
                report.addError(
                        "The @AssistedFactory-annotated type is missing an abstract, non-default method "
                                + "whose return type matches the assisted injection type.",
                        factory);
            }

            for (ExecutableElement method : abstractFactoryMethods) {
                ExecutableType methodType = types.resolveExecutableType(method, javaFactory.asType());

                //4.AssistedFactory修饰的类，该类中的abstract、非static、非private的方法，
                // 如果方法返回类型不是使用AssistdInject修饰注解修饰，则报错
                if (!isAssistedInjectionType(methodType.getReturnType())) {
                    report.addError(
                            String.format(
                                    "Invalid return type: %s. An assisted factory's abstract method must return a "
                                            + "type with an @AssistedInject-annotated constructor.",
                                    methodType.getReturnType()),
                            XConverters.toXProcessing(method, processingEnv));
                }

                //5.AssistedFactory修饰的类，该类中的abstract、非static、非private的方法，
                //该方法不能存在泛型信息
                if (!method.getTypeParameters().isEmpty()) {
                    report.addError(
                            "@AssistedFactory does not currently support type parameters in the creator "
                                    + "method. See https://github.com/google/dagger/issues/2279",
                            XConverters.toXProcessing(method, processingEnv));
                }
            }

            //6.AssistedFactory修饰的类，该类中的abstract、非static、非private的方法有且仅有一个
            if (abstractFactoryMethods.size() > 1) {
                report.addError(
                        "The @AssistedFactory-annotated type should contain a single abstract, non-default"
                                + " method but found multiple: "
                                + abstractFactoryMethods,
                        factory);
            }

            //如果以上已有报错，则返回报错。否则继续往下判断
            if (!report.build().isClean()) {
                return report.build();
            }

            AssistedInjectionAnnotations.AssistedFactoryMetadata metadata =
                    AssistedInjectionAnnotations.AssistedFactoryMetadata.create(javaFactory.asType(), elements, types);


            // Note: We check uniqueness of the @AssistedInject constructor parameters in
            // AssistedInjectProcessingStep. We need to check uniqueness for here too because we may
            // have resolved some type parameters that were not resolved in the @AssistedInject type.
            Set<AssistedInjectionAnnotations.AssistedParameter> uniqueAssistedParameters = new HashSet<>();
            for (AssistedInjectionAnnotations.AssistedParameter assistedParameter : metadata.assistedFactoryAssistedParameters()) {

                //7.AssistedFactory修饰的类，该类中的abstract、非static、非private的方法的参数，那么不允许出现重复参数
                //例如C （@Assited A a,Assited A b）,因为a和b的Assited#value相同，并且类型都是A。所以会报错
                if (!uniqueAssistedParameters.add(assistedParameter)) {
                    report.addError(
                            "@AssistedFactory method has duplicate @Assisted types: " + assistedParameter,
                            XConverters.toXProcessing(assistedParameter.variableElement(), processingEnv));
                }
            }

            //8.@AssistedFactory修饰的抽象类（或接口）里面的唯一方法规则：
            //该方法传递的参数必须 和 该方法返回类型的构造函数中使用@Assisted注解的参数 保持一致
            if (!ImmutableSet.copyOf(metadata.assistedInjectAssistedParameters())
                    .equals(ImmutableSet.copyOf(metadata.assistedFactoryAssistedParameters()))) {
                report.addError(
                        String.format(
                                "The parameters in the factory method must match the @Assisted parameters in %s."
                                        + "\n      Actual: %s#%s"
                                        + "\n    Expected: %s#%s(%s)",
                                metadata.assistedInjectType(),
                                metadata.factory().getQualifiedName(),
                                metadata.factoryMethod(),
                                metadata.factory().getQualifiedName(),
                                metadata.factoryMethod().getSimpleName(),
                                metadata.assistedInjectAssistedParameters().stream()
                                        .map(AssistedInjectionAnnotations.AssistedParameter::type)
                                        .map(Object::toString)
                                        .collect(joining(", "))),
                        XConverters.toXProcessing(metadata.factoryMethod(), processingEnv));
            }

            return report.build();
        }

        //类中有且仅有一个构造函数使用了AssistdInject修饰
        private boolean isAssistedInjectionType(TypeMirror type) {
            return type.getKind() == TypeKind.DECLARED
                    && AssistedInjectionAnnotations.isAssistedInjectionType(asTypeElement(type));
        }
    }


}
