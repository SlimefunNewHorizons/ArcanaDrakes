package cl.drakescraft.arcana;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class ResonancePolicyTest {
    @Test
    void compatiblePatronUsesConfiguredFavorTiers() {
        YamlConfiguration config = configuration();
        DivineSnapshot thor = new DivineSnapshot(true, "THOR", "Thor", "Asgard", 250);

        assertTrue(ResonancePolicy.compatible(Affinity.ELECTRO, thor, config));
        assertEquals(1.20D, ResonancePolicy.multiplier(Affinity.ELECTRO, thor, config), .00001D);
    }

    @Test
    void incompatiblePatronCannotCreateArcaneResonance() {
        YamlConfiguration config = configuration();
        DivineSnapshot demeter = new DivineSnapshot(true, "DEMETER", "Demeter", "Olympus", 999);

        assertFalse(ResonancePolicy.compatible(Affinity.ELECTRO, demeter, config));
        assertEquals(1.0D, ResonancePolicy.multiplier(Affinity.ELECTRO, demeter, config));
    }

    private static YamlConfiguration configuration() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("spirituality.resonance.compatible-gods.ELECTRO", java.util.List.of("THOR", "ZEUS"));
        config.set("spirituality.resonance.compatible-base-multiplier", 1.10D);
        config.set("spirituality.resonance.favor-per-tier", 100);
        config.set("spirituality.resonance.maximum-favor-tiers", 3);
        config.set("spirituality.resonance.bonus-per-favor-tier", .05D);
        return config;
    }
}
