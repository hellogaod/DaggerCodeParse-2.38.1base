package dagger.internal.codegen.validation;


import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableCollection;

import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import dagger.MembersInjector;
import dagger.assisted.Assisted;
import dagger.internal.codegen.base.FrameworkTypes;
import dagger.internal.codegen.base.RequestKinds;
import dagger.internal.codegen.binding.AssistedInjectionAnnotations;
import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.spi.model.RequestKind;

import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreElements.asVariable;
import static com.google.auto.common.MoreTypes.asTypeElement;
import static dagger.internal.codegen.binding.SourceFiles.membersInjectorNameForType;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.WILDCARD;

/**
 * Validation for dependency requests.
 * <p>
 * 验证依赖请求：依赖表示一个对象对另外一个对象的依赖，例如构造函数A(B b)，表示A使用到了b参数，所以A依赖B
 */
final class DependencyRequestValidator {
    private final MembersInjectionValidator membersInjectionValidator;
    private final InjectionAnnotations injectionAnnotations;
    private final KotlinMetadataUtil metadataUtil;
    private final DaggerElements elements;

    @Inject
    DependencyRequestValidator(
            MembersInjectionValidator membersInjectionValidator,
            InjectionAnnotations injectionAnnotations,
            KotlinMetadataUtil metadataUtil,
            DaggerElements elements) {
        this.membersInjectionValidator = membersInjectionValidator;
        this.injectionAnnotations = injectionAnnotations;
        this.metadataUtil = metadataUtil;
        this.elements = elements;
    }

    /**
     * Adds an error if the given dependency request has more than one qualifier annotation or is a
     * non-instance request with a wildcard type.
     * <p>
     * 入口，依赖校验规则
     */
    void validateDependencyRequest(
            ValidationReport.Builder report,
            Element requestElement,
            TypeMirror requestType
    ) {

        //前提：如果节点使用了Assisted注解，不需要继续往下校验
        //Assisted只能用于修饰参数
        if (MoreElements.isAnnotationPresent(requestElement, Assisted.class)) {
            // Don't validate assisted parameters. These are not dependency requests.
            return;
        }

        //如果是Kotlin文件，缺少有关其限定符的元数据，则出错
        if (missingQualifierMetadata(requestElement)) {

            report.addError(
                    "Unable to read annotations on an injected Kotlin property. The Dagger compiler must"
                            + " also be applied to any project containing @Inject properties.",
                    requestElement
            );

            // Skip any further validation if we don't have valid metadata for a type that needs it.
            return;
        }

        new Validator(report, requestElement, requestType).validate();
    }


    /**
     * Returns {@code true} if a kotlin inject field is missing metadata about its qualifiers.
     * <p>
     * 如果 kotlin 注入字段缺少有关其限定符的元数据，则返回 {@code true}。
     */
    private boolean missingQualifierMetadata(Element requestElement) {

        if (requestElement.getKind() == ElementKind.FIELD //字段
                // static injected fields are not supported, no need to get qualifier from kotlin metadata
                && !requestElement.getModifiers().contains(STATIC) //节点非static修饰
                && metadataUtil.hasMetadata(requestElement) //Kotlin文件
                && metadataUtil.isMissingSyntheticPropertyForAnnotations(asVariable(requestElement))
        ) {

            //节点所在的父类拼接上 "_MembersInjector"的节点是否存在，不存在返回true。存在返回false
            Optional<TypeElement> membersInjector =
                    Optional.ofNullable(
                            elements.getTypeElement(
                                    membersInjectorNameForType(asType(requestElement.getEnclosingElement()))
                            )
                    );
            return !membersInjector.isPresent();
        }
        return false;
    }

    private final class Validator {
        private final ValidationReport.Builder report;
        private final Element requestElement;
        private final TypeMirror requestType;
        private final TypeMirror keyType;
        private final RequestKind requestKind;
        private final ImmutableCollection<? extends AnnotationMirror> qualifiers;

        Validator(ValidationReport.Builder report, Element requestElement, TypeMirror requestType) {
            this.report = report;
            this.requestElement = requestElement;
            this.requestType = requestType;
            this.keyType = RequestKinds.extractKeyType(requestType);//剥离外层架构层的泛型
            this.requestKind = RequestKinds.getRequestKind(requestType);//当前type属于架构层的请求类型
            this.qualifiers = injectionAnnotations.getQualifiers(requestElement);//当前节点使用的Qualifier注解的注解集
        }

        void validate() {
            checkQualifiers();
            checkType();
        }

        private void checkQualifiers() {//Qulifiers注解修饰的注解最多只能使用1个，否则报错
            if (qualifiers.size() > 1) {
                for (AnnotationMirror qualifier : qualifiers) {
                    report.addError(
                            "A single dependency request may not use more than one @Qualifier",
                            requestElement,
                            qualifier);
                }
            }
        }

        private void checkType() {
            //如果没有使用Qulifiers注解修饰的注解 && 依赖是类或接口
            if (qualifiers.isEmpty() && keyType.getKind() == TypeKind.DECLARED) {
                TypeElement typeElement = asTypeElement(keyType);

                //节点构造函数使用了AssistdInject修饰，报错
                if (AssistedInjectionAnnotations.isAssistedInjectionType(typeElement)) {

                    report.addError(
                            "Dagger does not support injecting @AssistedInject type, "
                                    + requestType
                                    + ". Did you mean to inject its assisted factory type instead?",
                            requestElement);
                }

                //如果 keyType节点使用了@AssistedFactory修饰，那么requestType要么是T，要么是Provider<T>
                if (!(requestKind == RequestKind.INSTANCE || requestKind == RequestKind.PROVIDER)
                        && AssistedInjectionAnnotations.isAssistedFactoryType(typeElement)) {
                    report.addError(
                            "Dagger does not support injecting Lazy<T>, Producer<T>, "
                                    + "or Produced<T> when T is an @AssistedFactory-annotated type such as "
                                    + keyType,
                            requestElement);
                }
            }

            //T不允许是通配符格式
            if (keyType.getKind().equals(WILDCARD)) {
                // TODO(ronshapiro): Explore creating this message using RequestKinds.
                report.addError(
                        "Dagger does not support injecting Provider<T>, Lazy<T>, Producer<T>, "
                                + "or Produced<T> when T is a wildcard type such as "
                                + keyType,
                        requestElement);
            }

            //如果keyType其实是MembersInjector<T>类型，那么：
            //必须是MembersInjector<T>而不是MembersInjector,对T进行校验：
            // (1).不能使用Qualifier注解修饰的注解修饰；
            // (2).T只可以是类或接口，并且该类或接口是否使用了泛型：
            //    ①.如果没有使用泛型，那么不可以使用如List类似原始类型的写法（必须使用List<T>）;
            //    ②.如果使用了泛型，那么节点里面的泛型，只能是类或接口或数组，数组又只能是类或接口或原始类型或数组
            if (MoreTypes.isType(keyType) && MoreTypes.isTypeOf(MembersInjector.class, keyType)) {

                DeclaredType membersInjectorType = MoreTypes.asDeclared(keyType);
                if (membersInjectorType.getTypeArguments().isEmpty()) {
                    report.addError("Cannot inject a raw MembersInjector", requestElement);
                } else {
                    report.addSubreport(
                            membersInjectionValidator.validateMembersInjectionRequest(
                                    requestElement, membersInjectorType.getTypeArguments().get(0)));
                }
            }
        }
    }

    /**
     * Adds an error if the given dependency request is for a {@link dagger.producers.Producer} or
     * {@link dagger.producers.Produced}.
     * <p>
     * 检查requestElement不是Producer或Producer类型，如果是提交给report错误信息：
     * “requestElement节点只能在被Produces修饰时才可以使用Producer或Producer类型”
     *
     * <p>Only call this when processing a provision binding.
     */
    // TODO(dpb): Should we disallow Producer entry points in non-production components?
    void checkNotProducer(ValidationReport.Builder report, VariableElement requestElement) {
        TypeMirror requestType = requestElement.asType();
        if (FrameworkTypes.isProducerType(requestType)) {
            report.addError(
                    String.format(
                            "%s may only be injected in @Produces methods",
                            MoreTypes.asTypeElement(requestType).getSimpleName()),
                    requestElement);
        }
    }
}
