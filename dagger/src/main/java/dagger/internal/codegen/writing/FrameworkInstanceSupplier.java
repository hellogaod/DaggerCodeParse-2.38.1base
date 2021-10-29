package dagger.internal.codegen.writing;


/** An object that supplies a {@link MemberSelect} for a framework instance. */
interface FrameworkInstanceSupplier {
    /** Returns a {@link MemberSelect}, with possible side effects on the first call. */
    MemberSelect memberSelect();
}
