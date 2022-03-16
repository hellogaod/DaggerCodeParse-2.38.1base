package dagger.internal.codegen.writing;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeSpec;

import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.ModuleKind;
import dagger.internal.codegen.binding.SourceFiles;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.Accessibility;
import dagger.internal.codegen.langmodel.DaggerElements;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static dagger.internal.codegen.langmodel.Accessibility.isElementAccessibleFrom;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.constructorsIn;


/**
 * Convenience methods for generating and using module constructor proxy methods.
 */
public final class ModuleProxies {

    private final DaggerElements elements;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    public ModuleProxies(DaggerElements elements, KotlinMetadataUtil metadataUtil) {
        this.elements = elements;
        this.metadataUtil = metadataUtil;
    }

    /**
     * Generates a {@code public static} proxy method for constructing module instances.
     */
    // TODO(dpb): See if this can become a SourceFileGenerator<ModuleDescriptor> instead. Doing so may
    // cause ModuleProcessingStep to defer elements multiple times.
    public static final class ModuleConstructorProxyGenerator
            extends SourceFileGenerator<TypeElement> {

        private final ModuleProxies moduleProxies;
        private final KotlinMetadataUtil metadataUtil;

        @Inject
        ModuleConstructorProxyGenerator(
                XFiler filer,
                DaggerElements elements,
                SourceVersion sourceVersion,
                ModuleProxies moduleProxies,
                KotlinMetadataUtil metadataUtil) {
            super(filer, elements, sourceVersion);
            this.moduleProxies = moduleProxies;
            this.metadataUtil = metadataUtil;
        }

        @Override
        public Element originatingElement(TypeElement moduleElement) {
            return moduleElement;
        }

        @Override
        public ImmutableList<TypeSpec.Builder> topLevelTypes(TypeElement moduleElement) {
            ModuleKind.checkIsModule(moduleElement, metadataUtil);
            return moduleProxies.nonPublicNullaryConstructor(moduleElement).isPresent()
                    ? ImmutableList.of(buildProxy(moduleElement))
                    : ImmutableList.of();
        }

        private TypeSpec.Builder buildProxy(TypeElement moduleElement) {
            return classBuilder(moduleProxies.constructorProxyTypeName(moduleElement))
                    .addModifiers(PUBLIC, FINAL)
                    .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
                    .addMethod(
                            methodBuilder("newInstance")
                                    .addModifiers(PUBLIC, STATIC)
                                    .returns(ClassName.get(moduleElement))
                                    .addStatement("return new $T()", moduleElement)
                                    .build());
        }
    }

    /**
     * The name of the class that hosts the module constructor proxy method.
     */
    private ClassName constructorProxyTypeName(TypeElement moduleElement) {
        ModuleKind.checkIsModule(moduleElement, metadataUtil);
        ClassName moduleClassName = ClassName.get(moduleElement);
        return moduleClassName
                .topLevelClassName()
                .peerClass(SourceFiles.classFileName(moduleClassName) + "_Proxy");
    }

    /**
     * The module constructor being proxied. A proxy is generated if it is not publicly accessible and
     * has no arguments. If an implicit reference to the enclosing class exists, or the module is
     * abstract, no proxy method can be generated.
     */
    private Optional<ExecutableElement> nonPublicNullaryConstructor(TypeElement moduleElement) {

        ModuleKind.checkIsModule(moduleElement, metadataUtil);
        //如果module节点是abstract修饰 || module节点是非static修饰的内部类。直接返回；
        if (moduleElement.getModifiers().contains(ABSTRACT)
                || (moduleElement.getNestingKind().isNested()
                && !moduleElement.getModifiers().contains(STATIC))) {
            return Optional.empty();
        }
        //找出当前module类 可访问 非private修饰 参数为空的 构造函数
        return constructorsIn(elements.getAllMembers(moduleElement)).stream()
                .filter(constructor -> !Accessibility.isElementPubliclyAccessible(constructor))
                .filter(constructor -> !constructor.getModifiers().contains(PRIVATE))
                .filter(constructor -> constructor.getParameters().isEmpty())
                .findAny();
    }

    /**
     * Returns a code block that creates a new module instance, either by invoking the nullary
     * constructor if it's accessible from {@code requestingClass} or else by invoking the
     * constructor's generated proxy method.
     *
     * 实例化当前module类，使用newInstance代理或直接new
     */
    public CodeBlock newModuleInstance(TypeElement moduleElement, ClassName requestingClass) {
        ModuleKind.checkIsModule(moduleElement, metadataUtil);
        String packageName = requestingClass.packageName();
        //对当前module的构造函数如果存在非private修饰，并且无参并且可访问
        return nonPublicNullaryConstructor(moduleElement)
                .filter(constructor -> !isElementAccessibleFrom(constructor, packageName))
                .map(
                        constructor ->
                                CodeBlock.of("$T.newInstance()", constructorProxyTypeName(moduleElement)))
                .orElse(CodeBlock.of("new $T()", moduleElement));
    }
}
