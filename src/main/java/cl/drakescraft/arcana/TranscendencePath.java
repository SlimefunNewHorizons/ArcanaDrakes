package cl.drakescraft.arcana;

import org.bukkit.ChatColor;

public enum TranscendencePath {
    DRAGON_BALL("Vía del Ki (Súper Saiyajin)", ChatColor.GOLD, "Canalización de Ki, técnicas divinas y el despertar del Ultra Instinto."),
    NARUTO("Vía del Chakra (Arte Shinobi)", ChatColor.DARK_AQUA, "Moldeo de energía elemental, jutsus espirales y armaduras espectrales."),
    ONE_PIECE("Vía del Haki (Gran Línea)", ChatColor.DARK_RED, "Voluntad inquebrantable, armadura espiritual y aceleración corporal.");

    private final String displayName;
    private final ChatColor color;
    private final String description;

    TranscendencePath(String displayName, ChatColor color, String description) {
        this.displayName = displayName;
        this.color = color;
        this.description = description;
    }

    public String displayName() { return displayName; }
    public ChatColor color() { return color; }
    public String description() { return description; }
}
