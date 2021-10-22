package dagger.internal.codegen.validation;

import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

import androidx.room.compiler.processing.XElement;
import androidx.room.compiler.processing.XProcessingEnv;
import androidx.room.compiler.processing.XProcessingStep;

/**
 * A {@link XProcessingStep} that processes one element at a time and defers any for which {@link
 * TypeNotPresentException} is thrown.
 */
public abstract class TypeCheckingProcessingStep<E extends XElement> implements XProcessingStep {

    @Override
    public ImmutableSet<String> annotations() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<XElement> process(XProcessingEnv xProcessingEnv, Map<String, ? extends Set<? extends XElement>> map) {
        return ImmutableSet.of();
    }
}
