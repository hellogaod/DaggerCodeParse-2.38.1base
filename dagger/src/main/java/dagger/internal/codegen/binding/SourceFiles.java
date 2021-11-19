package dagger.internal.codegen.binding;

import com.google.auto.common.MoreElements;
import com.google.common.base.Joiner;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Utilities for generating files.
 */
public class SourceFiles {


    private static final Joiner CLASS_FILE_NAME_JOINER = Joiner.on('_');


    public static ClassName membersInjectorNameForType(TypeElement typeElement) {
        return siblingClassName(typeElement, "_MembersInjector");
    }

    //返回：类名.变量名
    public static String memberInjectedFieldSignatureForVariable(VariableElement variableElement) {
        return MoreElements.asType(variableElement.getEnclosingElement()).getQualifiedName()
                + "."
                + variableElement.getSimpleName();
    }

    // TODO(ronshapiro): when JavaPoet migration is complete, replace the duplicated code
    // which could use this.
    private static ClassName siblingClassName(TypeElement typeElement, String suffix) {
        ClassName className = ClassName.get(typeElement);
        return className.topLevelClassName().peerClass(classFileName(className) + suffix);
    }

    //如果className是内部类，如类A里面包含B，那么针对B生成的String：A_B
    public static String classFileName(ClassName className) {
        return CLASS_FILE_NAME_JOINER.join(className.simpleNames());
    }
}
