package cl.drakescraft.arcana;

/** Rank-up milestones for the non-Slimefun survival route. */
public enum ArcanaRank {
    DESPERTAR(0, "Despertar"),
    EXPLORADOR(1_000, "Explorador"),
    CATALIZADOR(5_000, "Catalizador"),
    VANGUARDIA(15_000, "Vanguardia"),
    ASCENDIDO(40_000, "Ascendido"),
    ARCONTE(100_000, "Arconte");

    private final long requiredExperience;
    private final String displayName;
    ArcanaRank(long requiredExperience, String displayName) { this.requiredExperience = requiredExperience; this.displayName = displayName; }
    public long requiredExperience() { return requiredExperience; }
    public String displayName() { return displayName; }
    public static ArcanaRank forExperience(long experience) {
        ArcanaRank current = DESPERTAR;
        for (ArcanaRank value : values()) if (experience >= value.requiredExperience) current = value;
        return current;
    }
}
