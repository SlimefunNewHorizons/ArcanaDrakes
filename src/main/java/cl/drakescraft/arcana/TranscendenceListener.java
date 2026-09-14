package cl.drakescraft.arcana;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public final class TranscendenceListener implements Listener {
    private final DrakesArcanaPlugin plugin;
    private final TranscendenceService service;

    public TranscendenceListener(DrakesArcanaPlugin plugin, TranscendenceService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageReceived(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Ultra Instinct automatic dodge (75% chance)
        if (service.isUltraInstinct(player.getUniqueId())) {
            if (event.getCause() != EntityDamageEvent.DamageCause.VOID &&
                event.getCause() != EntityDamageEvent.DamageCause.SUICIDE) {

                if (ThreadLocalRandom.current().nextDouble() < 0.75D) {
                    event.setCancelled(true);
                    Location from = player.getLocation();

                    // Evasive micro-teleportation (dodge step)
                    double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
                    Vector offset = new Vector(Math.cos(angle) * 2.2D, 0, Math.sin(angle) * 2.2D);
                    Location targetLoc = from.clone().add(offset);
                    if (targetLoc.getBlock().isPassable()) {
                        player.teleport(targetLoc);
                    }

                    from.getWorld().spawnParticle(Particle.FLASH, from.add(0, 1, 0), 1, 0, 0, 0, 0);
                    from.getWorld().spawnParticle(Particle.SNOWFLAKE, from, 8, 0.3, 0.5, 0.3, 0.05);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.9F, 1.6F);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttackDelivered(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // Armament Haki bonus damage (+5.0)
        if (service.isArmament(player.getUniqueId())) {
            event.setDamage(event.getDamage() + 5.0D);
            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1.2D, 0), 15, 0.25, 0.4, 0.25, 0.1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 0.6F, 1.4F);
        }

        // Kaioken bonus damage (+4.0)
        if (service.isKaioken(player.getUniqueId())) {
            event.setDamage(event.getDamage() + 4.0D);
            target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1.0D, 0), 12, 0.3, 0.3, 0.3, 0.04);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8F, 1.2F);
        }
    }
}
