package cl.drakescraft.arcana;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Color;

/** The six random primary schools. Light and shadow remain launch disciplines, not random primaries. */
public enum Affinity {
    FIRE("Fuego", Color.fromRGB(242, 93, 54)),
    EARTH("Tierra", Color.fromRGB(112, 171, 76)),
    AIR("Aire", Color.fromRGB(181, 242, 255)),
    WATER("Agua", Color.fromRGB(52, 145, 232)),
    ICE("Hielo", Color.fromRGB(151, 232, 255)),
    ELECTRO("Electro", Color.fromRGB(240, 216, 64));

    private static final List<Affinity> VALUES = List.of(values());
    private final String displayName;
    private final Color color;

    Affinity(String displayName, Color color) { this.displayName = displayName; this.color = color; }
    public String displayName() { return displayName; }
    public Color color() { return color; }
    public static Affinity random() { return VALUES.get(ThreadLocalRandom.current().nextInt(VALUES.size())); }
}
