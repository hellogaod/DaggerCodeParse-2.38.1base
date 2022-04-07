package dagger.hilt.processor.internal;


import javax.lang.model.element.Element;

/**
 * Exception to throw when a required {@link Element} is or inherits from an error kind.
 *
 * <p>Includes element to point to for the cause of the error
 */
public final class ErrorTypeException extends RuntimeException {
    private final Element badElement;

    public ErrorTypeException(String message, Element badElement) {
        super(message);
        this.badElement = badElement;
    }

    public Element getBadElement() {
        return badElement;
    }
}
