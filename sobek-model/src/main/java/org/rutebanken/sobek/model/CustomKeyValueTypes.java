package org.rutebanken.sobek.model;

public final class CustomKeyValueTypes {

    private CustomKeyValueTypes() {
        // Prevent instantiation
    }
    public static final String ORIGINAL_ID_KEY = "imported-id";
    public static final String MERGED_ID_KEY = "merged-id";

    /** The key of the key value that the contract number of a fleet is in, in NeTEx. */
    public static final String CONTRACT_NUMBER_KEY = "contract-number";

}
