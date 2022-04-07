package dagger.hilt.processor.internal;


import com.google.common.collect.ImmutableList;

import javax.lang.model.element.Element;

/**
 * Exception to throw when input code has caused an error.
 * Includes elements to point to for the cause of the error
 */
public final class BadInputException extends RuntimeException {
    private final ImmutableList<Element> badElements;

    public BadInputException(String message, Element badElement) {
        super(message);
        this.badElements = ImmutableList.of(badElement);
    }

    public BadInputException(String message, Iterable<? extends Element> badElements) {
        super(message);
        this.badElements = ImmutableList.copyOf(badElements);
    }

    public BadInputException(String message) {
        super(message);
        this.badElements = ImmutableList.of();
    }

    public ImmutableList<Element> getBadElements() {
        return badElements;
    }
}
