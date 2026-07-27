package cl.drakescraft.arcana;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/** Visual combat effects are bounded, modify no blocks, and never damage players. */
final class ArcanaEffects {
    private final DrakesArcanaPlugin plugin;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private int activeDomains;

    ArcanaEffects(DrakesArcanaPlugin plugin) { this.plugin = plugin; }

    boolean castPulse(Player player, Affinity affinity) {
        if (!canUseOffensively(player) || !ready(player, "pulse", plugin.getConfig().getLong("effects.pulse-cooldown-seconds", 20))) return false;
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize().multiply(.55D);
        Location point = origin.clone();
        Particle.DustOptions dust = new Particle.DustOptions(affinity.color(), 1.3F);
        for (int step = 0; step < 18; step++) {
            point.add(direction);
            player.getWorld().spawnParticle(Particle.DUST, point, 4, .08D, .08D, .08D, 0, dust);
            for (LivingEntity target : nearbyMonsters(point, 1.2D)) {
                target.damage(4.0D, player);
                target.setVelocity(target.getVelocity().add(direction.clone().normalize().multiply(.32D)).setY(.14D));
                player.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 12, .25D, .35D, .25D, .08D);
                player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, .45F, 1.6F);
                return true;
            }
            if (!point.getBlock().isPassable()) break;
        }
        player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, .7F, 1.2F);
        return true;
    }

    boolean castDomain(Player player, Affinity affinity) {
        if (!canUseOffensively(player) || !ready(player, "domain", plugin.getConfig().getLong("effects.domain-cooldown-seconds", 180))) return false;
        if (activeDomains >= plugin.getConfig().getInt("effects.maximum-concurrent-domains", 6)) return false;
        activeDomains++;
        final Location center = player.getLocation().clone();
        final int maxTicks = (int) plugin.getConfig().getLong("effects.domain-duration-seconds", 8) * 20;
        final Particle.DustOptions dust = new Particle.DustOptions(affinity.color(), 1.7F);
        new BukkitRunnable() {
            private int ticks;
            @Override public void run() {
                if (!player.isOnline() || ticks >= maxTicks || player.getWorld() != center.getWorld()) {
                    activeDomains = Math.max(0, activeDomains - 1);
                    cancel();
                    return;
                }
                double radius = 7.0D;
                for (int index = 0; index < 28; index++) {
                    double angle = (Math.PI * 2D * index / 28D) + (ticks * .1D);
                    Location point = center.clone().add(Math.cos(angle) * radius, .15D + Math.sin(ticks * .2D) * .25D, Math.sin(angle) * radius);
                    center.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, dust);
                }
                for (LivingEntity target : nearbyMonsters(center, radius)) {
                    target.damage(.7D, player);
                    target.setVelocity(target.getVelocity().add(target.getLocation().toVector().subtract(center.toVector()).normalize().multiply(.04D)));
                }
                if (ticks % 20 == 0) center.getWorld().playSound(center, Sound.BLOCK_BEACON_AMBIENT, .7F, .8F);
                ticks += 4;
            }
        }.runTaskTimer(plugin, 0L, 4L);
        player.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, .7F, 1.5F);
        return true;
    }

    private boolean canUseOffensively(Player player) { return plugin.getConfig().getStringList("worlds.offensive-enabled").contains(player.getWorld().getName()); }
    private boolean ready(Player player, String spell, long seconds) {
        String key = player.getUniqueId() + ":" + spell;
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(key, 0L) > now) return false;
        cooldowns.put(key, now + seconds * 1000L);
        return true;
    }
    private static Collection<LivingEntity> nearbyMonsters(Location location, double radius) {
        return location.getWorld().getNearbyLivingEntities(location, radius, radius, radius, entity -> entity instanceof Monster && !entity.isDead());
    }
}
