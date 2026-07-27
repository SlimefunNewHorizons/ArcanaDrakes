package cl.drakescraft.arcana;

import java.util.Locale;
import org.bukkit.configuration.file.FileConfiguration;

/** Pure configuration policy for matching an elemental affinity to a divine patron. */
final class ResonancePolicy {
    private ResonancePolicy() { }

    static boolean compatible(Affinity affinity, DivineSnapshot snapshot, FileConfiguration config) {
        if (!snapshot.hasPatron()) return false;
        return config.getStringList("spirituality.resonance.compatible-gods." + affinity.name()).stream()
                .map(value -> value.toUpperCase(Locale.ROOT))
                .anyMatch(snapshot.godId()::equals);
    }

    static double multiplier(Affinity affinity, DivineSnapshot snapshot, FileConfiguration config) {
        if (!compatible(affinity, snapshot, config)) return 1.0D;
        int threshold = Math.max(1, config.getInt("spirituality.resonance.favor-per-tier", 100));
        int maximumTiers = Math.max(0, config.getInt("spirituality.resonance.maximum-favor-tiers", 3));
        int tiers = Math.min(maximumTiers, snapshot.favor() / threshold);
        double base = Math.max(1.0D, config.getDouble("spirituality.resonance.compatible-base-multiplier", 1.10D));
        double perTier = Math.max(0.0D, config.getDouble("spirituality.resonance.bonus-per-favor-tier", .05D));
        return base + (tiers * perTier);
    }
}
