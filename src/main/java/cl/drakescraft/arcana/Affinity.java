package cl.drakescraft.arcana;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Color;

/**
 * The six random primary schools, plus Chaos.
 *
 * Light and shadow remain launch disciplines, not random primaries.
 *
 * Chaos is deliberately NOT one of the primaries: it is the rare school, and it only lands on a
 * player through {@link #roll(double)} at a configured chance. Handing it out at the same one in
 * seven as the rest would make the strongest affinity the most common thing on the server, which
 * is the opposite of what it is for.
 */
public enum Affinity {
    FIRE("Fuego", Color.fromRGB(242, 93, 54)),
    EARTH("Tierra", Color.fromRGB(112, 171, 76)),
    AIR("Aire", Color.fromRGB(181, 242, 255)),
    WATER("Agua", Color.fromRGB(52, 145, 232)),
    ICE("Hielo", Color.fromRGB(151, 232, 255)),
    ELECTRO("Electro", Color.fromRGB(240, 216, 64)),
    CHAOS("Caos", Color.fromRGB(148, 0, 211));

    /** Everything a new player can roll normally. Chaos is excluded on purpose. */
    private static final List<Affinity> PRIMARIES =
            List.of(FIRE, EARTH, AIR, WATER, ICE, ELECTRO);

    private final String displayName;
    private final Color color;

    Affinity(String displayName, Color color) { this.displayName = displayName; this.color = color; }
    public String displayName() { return displayName; }
    public Color color() { return color; }

    /** True for the schools that bend the rules the others follow. */
    public boolean rare() { return this == CHAOS; }

    public static List<Affinity> primaries() { return PRIMARIES; }

    /** A plain primary school. Never returns Chaos. */
    public static Affinity random() { return PRIMARIES.get(ThreadLocalRandom.current().nextInt(PRIMARIES.size())); }

    /**
     * Rolls an affinity, giving Chaos the supplied chance and splitting the rest evenly.
     *
     * @param chaosChance chance of Chaos, from 0 to 1; anything outside that range is clamped
     */
    public static Affinity roll(double chaosChance) {
        double chance = Math.max(0.0D, Math.min(1.0D, chaosChance));
        if (chance > 0.0D && ThreadLocalRandom.current().nextDouble() < chance) return CHAOS;
        return random();
    }
}
