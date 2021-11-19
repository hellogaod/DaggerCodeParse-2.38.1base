package dagger.internal.codegen;


import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;

import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementKindVisitor8;

import androidx.room.compiler.processing.XElement;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.internal.codegen.binding.InjectBindingRegistry;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;

/**
 * An annotation processor for generating Dagger implementation code based on the {@link Inject}
 * annotation.
 */
// TODO(gak): add some error handling for bad source files
final class InjectProcessingStep extends TypeCheckingProcessingStep<XElement> {

    private final ElementVisitor<Void, Void> visitor;
    private final Set<Element> processedElements = Sets.newLinkedHashSet();

    @Inject
    InjectProcessingStep(InjectBindingRegistry injectBindingRegistry) {

        //if，else判断，当前节点是构造函数，还是变量，还是普通方法
        this.visitor =
                new ElementKindVisitor8<Void, Void>() {
                    @Override
                    public Void visitExecutableAsConstructor(ExecutableElement constructorElement, Void aVoid) {//如果是构造函数方法，操作当前类

                        injectBindingRegistry.tryRegisterConstructor(constructorElement);
                        return null;
                    }

                    @Override
                    public Void visitVariableAsField(VariableElement fieldElement, Void aVoid) {//如果是变量，操作其所在类
                        injectBindingRegistry.tryRegisterMembersInjectedType(
                                MoreElements.asType(fieldElement.getEnclosingElement())
                        );
                        return null;
                    }

                    @Override
                    public Void visitExecutableAsMethod(ExecutableElement methodElement, Void aVoid) {//如果是普通方法，操作其所在类
                        injectBindingRegistry.tryRegisterMembersInjectedType(
                                MoreElements.asType(methodElement.getEnclosingElement())
                        );
                        return null;
                    }
                };
    }

    @Override
    protected ImmutableSet<ClassName> annotationClassNames() {
        return ImmutableSet.of(TypeNames.INJECT, TypeNames.ASSISTED_INJECT);
    }

    @Override
    protected void process(XElement xElement, ImmutableSet<ClassName> annotations) {
        // TODO(bcorso): Remove conversion to javac type and use XProcessing throughout.
        Element injectElement = XConverters.toJavac(xElement);//注意，这里的injectElement表示当前节点使用了Inject或AsistedInject注解

        // Only process an element once to avoid getting duplicate errors when an element is annotated
        // with multiple inject annotations.
        //如果处理过了，存储于processedElemented缓存中，不进行二次处理
        if (processedElements.contains(injectElement)) {
            return;
        }
        //对使用Inject注解或AssistedInject注解的节点遍历
        injectElement.accept(visitor, null);

        processedElements.add(injectElement);
    }

}
