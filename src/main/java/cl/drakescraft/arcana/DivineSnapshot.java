package cl.drakescraft.arcana;

/** Immutable view of the optional DiosesDrakes service; it never owns divine data. */
record DivineSnapshot(boolean available, String godId, String godDisplayName, String pantheonName, int favor) {
    static DivineSnapshot unavailable() { return new DivineSnapshot(false, null, null, null, 0); }
    static DivineSnapshot unbound() { return new DivineSnapshot(true, null, null, null, 0); }
    boolean hasPatron() { return godId != null; }
}
