package cl.drakescraft.arcana;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** A thematic creature archetype; it grants flavor and future spells, never a literal permanent disguise. */
public enum ArcaneOrigin {
    PHOENIX(Affinity.FIRE, "Fénix"), BLAZE(Affinity.FIRE, "Blaze"),
    IRON_GUARDIAN(Affinity.EARTH, "Guardián de hierro"), RAVAGER(Affinity.EARTH, "Ravager"),
    BREEZE(Affinity.AIR, "Breeze"), PHANTOM(Affinity.AIR, "Phantom"),
    GUARDIAN(Affinity.WATER, "Guardián marino"), DROWNED(Affinity.WATER, "Ahogado"),
    STRAY(Affinity.ICE, "Stray"), SNOW_GUARDIAN(Affinity.ICE, "Guardián de nieve"),
    ENDER_WALKER(Affinity.ELECTRO, "Caminante Ender"), STORM_CALLER(Affinity.ELECTRO, "Invocador de tormentas");

    private final Affinity affinity;
    private final String displayName;
    ArcaneOrigin(Affinity affinity, String displayName) { this.affinity = affinity; this.displayName = displayName; }
    public Affinity affinity() { return affinity; }
    public String displayName() { return displayName; }
    public static ArcaneOrigin randomFor(Affinity affinity) {
        List<ArcaneOrigin> candidates = java.util.Arrays.stream(values()).filter(value -> value.affinity == affinity).toList();
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }
}
