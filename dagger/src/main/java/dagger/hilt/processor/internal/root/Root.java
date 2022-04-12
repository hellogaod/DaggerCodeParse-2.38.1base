package dagger.hilt.processor.internal.root;


import com.google.auto.common.MoreElements;
import com.google.auto.value.AutoValue;
import com.squareup.javapoet.ClassName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.ClassNames;

/** Metadata for a root element that can trigger the {@link RootProcessor}. */
@AutoValue
abstract class Root {
    /**
     * Creates the default root for this (test) build compilation.
     *
     * <p>A default root installs only the global {@code InstallIn} and {@code TestInstallIn}
     * dependencies. Test-specific dependencies are not installed in the default root.
     *
     * <p>The default root is used for two purposes:
     *
     * <ul>
     *   <li>To inject {@code EarlyEntryPoint} annotated interfaces.
     *   <li>To inject tests that only depend on global dependencies
     * </ul>
     */
    static Root createDefaultRoot(ProcessingEnvironment env) {
        TypeElement rootElement =
                env.getElementUtils().getTypeElement(ClassNames.DEFAULT_ROOT.canonicalName());
        return new AutoValue_Root(rootElement, rootElement, /*isTestRoot=*/ true);
    }

    /** Creates a {@plainlink Root root} for the given {@plainlink Element element}. */
    static Root create(Element element, ProcessingEnvironment env) {
        TypeElement rootElement = MoreElements.asType(element);
        if (ClassNames.DEFAULT_ROOT.equals(ClassName.get(rootElement))) {
            return createDefaultRoot(env);
        }
        return new AutoValue_Root(rootElement, rootElement, RootType.of(rootElement).isTestRoot());
    }

    /** Returns the root element that should be used with processing. */
    abstract TypeElement element();

    /**
     * Returns the originating root element. In most cases this will be the same as {@link
     * #element()}.
     */
    abstract TypeElement originatingRootElement();

    /** Returns {@code true} if this is a test root. */
    abstract boolean isTestRoot();

    /** Returns the class name of the root element. */
    ClassName classname() {
        return ClassName.get(element());
    }

    /** Returns the class name of the originating root element. */
    ClassName originatingRootClassname() {
        return ClassName.get(originatingRootElement());
    }

    @Override
    public final String toString() {
        return originatingRootElement().toString();
    }

    /** Returns {@code true} if this uses the default root. */
    boolean isDefaultRoot() {
        return classname().equals(ClassNames.DEFAULT_ROOT);
    }
}
