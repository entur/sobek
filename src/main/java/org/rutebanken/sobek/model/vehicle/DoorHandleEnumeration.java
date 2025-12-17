package org.rutebanken.sobek.model.vehicle;

public enum DoorHandleEnumeration {
    NONE("none"),
    LEVER("lever"),
    BUTTON("button"),
    KNOB("knob"),
    CRASH_BAR("crashBar"),
    DOOR_HANDLE("doorHandle"),
    GRAB_RAIL("grabRail"),
    WINDOW_LEVER("windowLever"),
    VERTICAL("vertical"),
    OTHER("other");

    private final String value;

    private DoorHandleEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static DoorHandleEnumeration fromValue(String v) {
        for(DoorHandleEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}
