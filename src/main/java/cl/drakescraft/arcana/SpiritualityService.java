package cl.drakescraft.arcana;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

/** Bounded meditation rewards Arcana progress without minting or spending divine favor. */
final class SpiritualityService {
    private final DrakesArcanaPlugin plugin;
    private final DivineBridge divine;
    private final Map<UUID, Long> meditationCooldowns = new HashMap<>();

    SpiritualityService(DrakesArcanaPlugin plugin, DivineBridge divine) { this.plugin = plugin; this.divine = divine; }

    MeditationResult meditate(Player player, ArcanaProfile profile) {
        if (!plugin.getConfig().getBoolean("spirituality.enabled", true) || !ready(player.getUniqueId())) return MeditationResult.unavailable(profile);
        DivineSnapshot snapshot = divine.snapshot(player.getUniqueId());
        double multiplier = resonanceMultiplier(profile.affinity(), snapshot);
        long maximumSpirit = Math.max(0L, plugin.getConfig().getLong("spirituality.maximum-spirit", 10000L));
        long spirit = Math.min(maximumSpirit, profile.spirit() + Math.max(0L, plugin.getConfig().getLong("spirituality.meditation.base-spirit", 12L)));
        long experience = Math.round(Math.max(0L, plugin.getConfig().getLong("spirituality.meditation.base-experience", 40L)) * multiplier);
        long sigils = Math.round(Math.max(0L, plugin.getConfig().getLong("spirituality.meditation.base-sigils", 1L)) * multiplier);
        ArcanaProfile updated = new ArcanaProfile(profile.playerId(), profile.affinity(), profile.origin(), profile.experience() + experience,
                profile.sigils() + sigils, spirit);
        return new MeditationResult(true, updated, experience, spirit - profile.spirit(), sigils, multiplier);
    }

    double resonanceMultiplier(Affinity affinity, DivineSnapshot snapshot) {
        return ResonancePolicy.multiplier(affinity, snapshot, plugin.getConfig());
    }

    private boolean ready(UUID playerId) {
        long now = System.currentTimeMillis();
        if (meditationCooldowns.getOrDefault(playerId, 0L) > now) return false;
        long cooldownMillis = Math.max(1L, plugin.getConfig().getLong("spirituality.meditation.cooldown-seconds", 300L)) * 1000L;
        meditationCooldowns.put(playerId, now + cooldownMillis);
        return true;
    }
}
