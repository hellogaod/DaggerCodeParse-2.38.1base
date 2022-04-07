package dagger.hilt.processor.internal;


import com.squareup.javapoet.ClassName;

import java.util.function.Function;

/**
 * Utility class for getting the generated component name.
 *
 * <p>This should not be used externally.
 */
public final class ComponentNames {

    /**
     * Returns an instance of {@link ComponentNames} that will base all component names off of the
     * given root.
     */
    public static ComponentNames withoutRenaming() {
        return new ComponentNames(Function.identity());
    }

    /**
     * Returns an instance of {@link ComponentNames} that will base all component names off of the
     * given root after mapping it with {@code rootRenamer}.
     */
    public static ComponentNames withRenaming(Function<ClassName, ClassName> rootRenamer) {
        return new ComponentNames(rootRenamer);
    }

    private final Function<ClassName, ClassName> rootRenamer;

    private ComponentNames(Function<ClassName, ClassName> rootRenamer) {
        this.rootRenamer = rootRenamer;
    }

    public ClassName generatedComponentTreeDeps(ClassName root) {
        return Processors.append(
                Processors.getEnclosedClassName(rootRenamer.apply(root)), "_ComponentTreeDeps");
    }

    /** Returns the name of the generated component wrapper. */
    public ClassName generatedComponentsWrapper(ClassName root) {
        return Processors.append(
                Processors.getEnclosedClassName(rootRenamer.apply(root)), "_HiltComponents");
    }

    /** Returns the name of the generated component. */
    public ClassName generatedComponent(ClassName root, ClassName component) {
        //root拼接（Roo.Type那么使用Roo_Type） _HiltComponents
        return generatedComponentsWrapper(root).nestedClass(componentName(component));
    }

    /**
     * Returns the shortened component name by replacing the ending "Component" with "C" if it exists.
     *
     * Component$使用SingletonC替代
     *
     * <p>This is a hack because nested subcomponents in Dagger generate extremely long class names
     * that hit the 256 character limit.
     */
    // TODO(bcorso): See if this issue can be fixed in Dagger, e.g. by using static subcomponents.
    private static String componentName(ClassName component) {
        // TODO(bcorso): How do we want to handle collisions across packages? Currently, we only handle
        // collisions across enclosing elements since namespacing by package would likely lead to too
        // long of class names.
        // Note: This uses regex matching so we only match if the name ends in "Component"
        return Processors.getEnclosedName(component).replaceAll("Component$", "C");
    }
}
