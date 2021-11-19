package dagger.android.processor;


import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.service.AutoService;
import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.googlejavaformat.java.filer.FormattingFiler;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;

import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING;

/**
 * An {@linkplain javax.annotation.processing.Processor annotation processor} to verify usage of
 * {@code dagger.android} code.
 *
 * <p>Additionally, if {@code -Adagger.android.experimentalUseStringKeys} is passed to the
 * compilation, a file will be generated to support obfuscated injected Android types used with
 * {@code @AndroidInjectionKey}. The fact that this is generated is deliberate: not all versions of
 * ProGuard/R8 support {@code -identifiernamestring}, so we can't include a ProGuard file in the
 * dagger-android artifact Instead, we generate the file in {@code META-INF/proguard} only when
 * users enable the flag. They should only be enabling it if their shrinker supports those files,
 * and any version that does so will also support {@code -identifiernamestring}. This was added to
 * R8 in <a href="https://r8.googlesource.com/r8/+/389123dfcc11e6dda0eec31ab62e1b7eb0da80d2">May
 * 2018</a>.
 */
@IncrementalAnnotationProcessor(ISOLATING)
@AutoService(Processor.class)
public final class AndroidProcessor extends BasicAnnotationProcessor {
    private static final String FLAG_EXPERIMENTAL_USE_STRING_KEYS =
            "dagger.android.experimentalUseStringKeys";

    @Override
    protected Iterable<? extends Step> steps() {
        Filer filer = new FormattingFiler(processingEnv.getFiler());
        Messager messager = processingEnv.getMessager();
        Elements elements = processingEnv.getElementUtils();
        Types types = processingEnv.getTypeUtils();

        return ImmutableList.of(
                new AndroidMapKeyValidator(elements, types, messager),
                new ContributesAndroidInjectorGenerator(
                        new AndroidInjectorDescriptor.Validator(messager),
                        useStringKeys(),
                        filer,
                        elements,
                        processingEnv.getSourceVersion()));
    }

    private boolean useStringKeys() {
        if (!processingEnv.getOptions().containsKey(FLAG_EXPERIMENTAL_USE_STRING_KEYS)) {
            return false;
        }
        String flagValue = processingEnv.getOptions().get(FLAG_EXPERIMENTAL_USE_STRING_KEYS);
        if (flagValue == null || Ascii.equalsIgnoreCase(flagValue, "true")) {
            return true;
        } else if (Ascii.equalsIgnoreCase(flagValue, "false")) {
            return false;
        } else {
            processingEnv
                    .getMessager()
                    .printMessage(
                            ERROR,
                            String.format(
                                    "Unknown flag value: %s. %s must be set to either 'true' or 'false'.",
                                    flagValue, FLAG_EXPERIMENTAL_USE_STRING_KEYS));
            return false;
        }
    }

    @Override
    public Set<String> getSupportedOptions() {
        return ImmutableSet.of(FLAG_EXPERIMENTAL_USE_STRING_KEYS);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
