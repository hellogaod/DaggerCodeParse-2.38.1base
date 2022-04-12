package dagger.hilt.processor.internal.root;


import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

/** The valid root types for Hilt applications. */
// TODO(erichang): Fix this class so we don't have to have placeholders
enum RootType {
    ROOT(ClassNames.HILT_ANDROID_APP),

    // Placeholder to make sure @HiltAndroidTest usages get processed
    HILT_ANDROID_TEST_ROOT(ClassNames.HILT_ANDROID_TEST),

    TEST_ROOT(ClassNames.INTERNAL_TEST_ROOT);

    @SuppressWarnings("ImmutableEnumChecker")
    private final ClassName annotation;

    RootType(ClassName annotation) {
        this.annotation = annotation;
    }

    public boolean isTestRoot() {
        return this == TEST_ROOT;
    }

    public ClassName className() {
        return annotation;
    }

    public static RootType of(TypeElement element) {
        if (Processors.hasAnnotation(element, ClassNames.HILT_ANDROID_APP)) {
            return ROOT;
        } else if (Processors.hasAnnotation(element, ClassNames.HILT_ANDROID_TEST)) {
            return TEST_ROOT;
        } else if (Processors.hasAnnotation(element, ClassNames.INTERNAL_TEST_ROOT)) {
            return TEST_ROOT;
        }
        throw new IllegalStateException("Unknown root type: " + element);
    }
}
