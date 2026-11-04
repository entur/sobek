package org.rutebanken.sobek.netex.id;

/**
 * The type of a codespace (the first part of a NeTEx id, e.g. "AKT" in "AKT:Vehicle:1"): who is allowed to assign the ids in it, which decides what Sobek does with an id it receives.
 */
public enum CodespaceType {

    /**
     * The codespaces of Sobek itself: the configured netex.validPrefix (NMR) and, for vehicle types, CVT.
     * Only Sobek assigns new ids in them. An id there that does not exist is rejected; one that exists is an update.
     */
    SOBEK,

    /**
     * The codespaces in netex.valid.codespaces.list. The client (Hathor, an organisation's own system) assigns the ids,
     * and Sobek respects and uses them as the NeTEx id, and never assigns ids in them.
     */
    REGISTERED,

    /**
     * Any other codespace, typically another system such as Autosys. Sobek assigns a new id in its own codespace
     * and keeps the external id as a key value ({@code imported-id}).
     */
    EXTERNAL
}
