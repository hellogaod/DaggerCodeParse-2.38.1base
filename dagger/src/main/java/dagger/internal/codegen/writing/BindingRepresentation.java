package dagger.internal.codegen.writing;


import dagger.internal.codegen.binding.BindingRequest;

/** A factory of code expressions to satisfy all kinds of requests for a binding in a component. */
interface BindingRepresentation {
    RequestRepresentation getRequestRepresentation(BindingRequest request);
}
