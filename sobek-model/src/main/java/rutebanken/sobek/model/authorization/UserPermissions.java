package org.rutebanken.sobek.model.authorization;

public record UserPermissions(boolean isGuest, boolean allowNewStopEverywhere, String preferredName) {
}
