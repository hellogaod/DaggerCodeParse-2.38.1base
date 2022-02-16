package dagger.internal.codegen.binding;


import dagger.MembersInjector;

/** Whether a binding or declaration is for provision, production, or a {@link MembersInjector}. */
public enum BindingType {
    /** A binding with this type is a {@link ProvisionBinding}. */
    PROVISION,

    /** A binding with this type is a {@link MembersInjectionBinding}. */
    MEMBERS_INJECTION,

    /** A binding with this type is a {@link ProductionBinding}. */
    PRODUCTION,
}
