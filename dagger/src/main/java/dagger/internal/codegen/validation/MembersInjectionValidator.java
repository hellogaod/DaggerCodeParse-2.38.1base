package dagger.internal.codegen.validation;

import com.google.auto.common.MoreElements;

import javax.inject.Inject;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor8;

import dagger.internal.codegen.binding.InjectionAnnotations;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Validates members injection requests (members injection methods on components and requests for
 * {@code MembersInjector<Foo>}).
 * <p>
 * 成员注解校验：
 */
final class MembersInjectionValidator {
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    MembersInjectionValidator(
            InjectionAnnotations injectionAnnotations
    ) {
        this.injectionAnnotations = injectionAnnotations;
    }

    /**
     * Reports errors if a request for a {@code MembersInjector<Foo>}) is invalid.
     * <p>
     * 入口，成员注入请求
     * MembersInjector<Foo>的Foo进行校验：
     * 1.不能使用Qualifier注解修饰的注解修饰不能使用Qualifier注解修饰的注解修饰；
     * 2.节点只可以是类或接口，并且该类或接口是否使用了泛型：
     * （1）.如果没有使用泛型，那么不可以使用如List类似原始类型的写法（必须使用List<T>）;
     * （2）.如果使用了泛型，那么节点里面的泛型，只能是类或接口或数组，数组又只能是类或接口或原始类型或数组
     */
    ValidationReport validateMembersInjectionRequest(
            Element requestElement,
            TypeMirror membersInjectedType
    ) {
        ValidationReport.Builder report = ValidationReport.about(requestElement);
        checkQualifiers(report, requestElement);
        membersInjectedType.accept(VALIDATE_MEMBERS_INJECTED_TYPE, report);
        return report.build();
    }

    /**
     * Reports errors if a members injection method on a component is invalid.
     * <p>
     * 入口，
     * (Sub)Component方法注解成员校验：
     * 1.方法不能使用Qualifier注解修饰的注解修饰；
     * 2.方法里面的参数有且仅有一个，并且不能使用Qualifier注解修饰的注解修饰；
     * 3.节点只可以是类或接口，并且该类或接口是否使用了泛型：
     * （1）.如果没有使用泛型，那么不可以使用如List类似原始类型的写法（必须使用List<T>）;
     * （2）.如果使用了泛型，那么节点里面的泛型，只能是类或接口或数组，数组又只能是类或接口或原始类型或数组
     *
     * @throws IllegalArgumentException if the method doesn't have exactly one parameter
     */
    ValidationReport validateMembersInjectionMethod(
            ExecutableElement method,
            TypeMirror membersInjectedType
    ) {
        checkArgument(
                method.getParameters().size() == 1, "expected a method with one parameter: %s", method);

        ValidationReport.Builder report = ValidationReport.about(method);
        checkQualifiers(report, method);
        checkQualifiers(report, method.getParameters().get(0));
        membersInjectedType.accept(VALIDATE_MEMBERS_INJECTED_TYPE, report);
        return report.build();
    }

    //节点不能使用Qualifier注解修饰的注解修饰
    private void checkQualifiers(ValidationReport.Builder report, Element element) {
        for (AnnotationMirror qualifier : injectionAnnotations.getQualifiers(element)) {
            report.addError("Cannot inject members into qualified types", element, qualifier);
            break; // just report on the first qualifier, in case there is more than one
        }
    }

    //以下代码作用：注解成员必须是类或接口，判断是否使用了泛型：
    //1.如果没有使用泛型，那么不可以使用，如List类似原始类型的写法（必须使用List<T>）;
    //2.如果使用了泛型，那么节点里面的泛型，只能是类或接口或数组，数组又只能是类或接口或原始类型或数组
    private static final TypeVisitor<Void, ValidationReport.Builder> VALIDATE_MEMBERS_INJECTED_TYPE =
            new SimpleTypeVisitor8<Void, ValidationReport.Builder>() {
                // Only declared types can be members-injected.
                @Override
                protected Void defaultAction(TypeMirror type, ValidationReport.Builder report) {
                    report.addError("Cannot inject members into " + type);
                    return null;
                }

                @Override
                public Void visitDeclared(DeclaredType type, ValidationReport.Builder report) {
                    //if 类或接口

                    if (type.getTypeArguments().isEmpty()) {
                        // If the type is the erasure of a generic type, that means the user referred to
                        // Foo<T> as just 'Foo', which we don't allow.  (This is a judgement call; we
                        // *could* allow it and instantiate the type bounds, but we don't.)
                        if (!MoreElements.asType(type.asElement()).getTypeParameters().isEmpty()) {
                            report.addError("Cannot inject members into raw type " + type);
                        }
                    } else {
                        // If the type has arguments, validate that each type argument is declared.
                        // Otherwise the type argument may be a wildcard (or other type), and we can't
                        // resolve that to actual types.  For array type arguments, validate the type of the
                        // array.
                        for (TypeMirror arg : type.getTypeArguments()) {
                            if (!arg.accept(DECLARED_OR_ARRAY, null)) {
                                report.addError(
                                        "Cannot inject members into types with unbounded type arguments: " + type);
                            }
                        }
                    }
                    return null;
                }
            };

    // TODO(dpb): Can this be inverted so it explicitly rejects wildcards or type variables?
    // This logic is hard to describe.
    //如果是类或接口或者是数组，返回true。深入递归查询
    private static final TypeVisitor<Boolean, Void> DECLARED_OR_ARRAY =
            new SimpleTypeVisitor8<Boolean, Void>(false) {
                @Override
                public Boolean visitArray(ArrayType arrayType, Void p) {
                    return arrayType
                            .getComponentType()
                            .accept(
                                    new SimpleTypeVisitor8<Boolean, Void>(false) {
                                        @Override
                                        public Boolean visitDeclared(DeclaredType declaredType, Void p) {
                                            for (TypeMirror arg : declaredType.getTypeArguments()) {
                                                if (!arg.accept(this, null)) {
                                                    return false;
                                                }
                                            }
                                            return true;
                                        }

                                        @Override
                                        public Boolean visitArray(ArrayType arrayType, Void p) {
                                            return arrayType.getComponentType().accept(this, null);
                                        }

                                        @Override
                                        public Boolean visitPrimitive(PrimitiveType primitiveType, Void p) {
                                            return true;
                                        }
                                    },
                                    null);
                }

                @Override
                public Boolean visitDeclared(DeclaredType t, Void p) {
                    return true;
                }
            };
}
