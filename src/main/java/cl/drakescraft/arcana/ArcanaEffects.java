package cl.drakescraft.arcana;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Los efectos de combate estan acotados y no modifican bloques.
 *
 * Si alcanzan a un jugador depende de combat.allow-player-vs-player y, sobre todo, de que el
 * sitio permita PvP: el dano se entrega con damage(), asi que las protecciones lo cancelan igual
 * que cancelarian un espadazo.
 */
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
        double damage = plugin.getConfig().getDouble("effects.pulse-damage", 4.0D);
        // Chaos hits harder and, unlike every other school, does not stop at the first body.
        if (affinity.rare()) damage *= plugin.getConfig().getDouble("effects.chaos.pulse-damage-multiplier", 1.75D);
        int remainingChains = affinity.rare() ? Math.max(1, plugin.getConfig().getInt("effects.chaos.pulse-chains", 3)) : 1;

        for (int step = 0; step < 18; step++) {
            point.add(direction);
            player.getWorld().spawnParticle(Particle.DUST, point, 4, .08D, .08D, .08D, 0, dust);
            for (LivingEntity target : nearbyTargets(player, point, 1.2D)) {
                target.damage(damage, player);
                target.setVelocity(target.getVelocity().add(direction.clone().normalize().multiply(.32D)).setY(.14D));
                player.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 12, .25D, .35D, .25D, .08D);
                player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, .45F, 1.6F);
                if (--remainingChains <= 0) return true;
                // Chaos keeps travelling: the bolt is spent on the body only for the other schools.
                player.getWorld().spawnParticle(Particle.WITCH, target.getLocation().add(0, 1, 0), 20, .4D, .5D, .4D, .05D);
                break;
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
        final boolean chaos = affinity.rare();
        final int maxTicks = (int) plugin.getConfig().getLong("effects.domain-duration-seconds", 8)
                * (chaos ? plugin.getConfig().getInt("effects.chaos.domain-duration-multiplier", 2) : 1) * 20;
        final Particle.DustOptions dust = new Particle.DustOptions(affinity.color(), 1.7F);
        // Anillo mas ancho y tick mas fuerte, sin tocar un solo bloque.
        final double radius = chaos ? plugin.getConfig().getDouble("effects.chaos.domain-radius", 11.0D) : 7.0D;
        final double tickDamage = chaos ? plugin.getConfig().getDouble("effects.chaos.domain-damage", 1.6D) : .7D;
        new BukkitRunnable() {
            private int ticks;
            @Override public void run() {
                if (!player.isOnline() || ticks >= maxTicks || player.getWorld() != center.getWorld()) {
                    activeDomains = Math.max(0, activeDomains - 1);
                    cancel();
                    return;
                }
                for (int index = 0; index < 28; index++) {
                    double angle = (Math.PI * 2D * index / 28D) + (ticks * .1D);
                    Location point = center.clone().add(Math.cos(angle) * radius, .15D + Math.sin(ticks * .2D) * .25D, Math.sin(angle) * radius);
                    center.getWorld().spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, dust);
                }
                for (LivingEntity target : nearbyTargets(player, center, radius)) {
                    target.damage(tickDamage, player);
                    target.setVelocity(target.getVelocity().add(target.getLocation().toVector().subtract(center.toVector()).normalize().multiply(.04D)));
                    // El caos es impredecible por definicion: cada segundo lanza una maldicion
                    // distinta a lo que haya dentro. Todas se pasan solas y ninguna mata al instante.
                    if (chaos && ticks % 20 == 0) target.addPotionEffect(randomCurse());
                }
                if (ticks % 20 == 0) center.getWorld().playSound(center, chaos ? Sound.ENTITY_WITHER_AMBIENT : Sound.BLOCK_BEACON_AMBIENT, .7F, .8F);
                ticks += 4;
            }
        }.runTaskTimer(plugin, 0L, 4L);
        player.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, .7F, 1.5F);
        return true;
    }

    private boolean canUseOffensively(Player player) {
        List<String> worlds = plugin.getConfig().getStringList("combat.offensive-worlds");
        // Keep old installations working while administrators migrate their config.
        if (worlds.isEmpty()) worlds = plugin.getConfig().getStringList("worlds.offensive-enabled");
        return worlds.contains(player.getWorld().getName());
    }
    private boolean ready(Player player, String spell, long seconds) {
        String key = player.getUniqueId() + ":" + spell;
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(key, 0L) > now) return false;
        cooldowns.put(key, now + seconds * 1000L);
        return true;
    }
    /**
     * One of the Chaos domain's curses, picked at random.
     *
     * All of them expire on their own and none is instant death: a domain that could simply delete
     * whatever walks in would make every other school pointless, which is the opposite of the idea.
     */
    private static PotionEffect randomCurse() {
        PotionEffectType[] curses = {
            PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS, PotionEffectType.BLINDNESS,
            PotionEffectType.WITHER, PotionEffectType.MINING_FATIGUE, PotionEffectType.NAUSEA
        };
        PotionEffectType curse = curses[ThreadLocalRandom.current().nextInt(curses.length)];
        return new PotionEffect(curse, 60, ThreadLocalRandom.current().nextInt(2), true, true);
    }

    /**
     * Lo que un hechizo puede alcanzar en un punto.
     *
     * Antes esto solo devolvia monstruos, asi que un hechizo atravesaba a un jugador sin rozarlo y
     * la magia no servia de nada en PvP. Las opciones allow-player-vs-player y monsters-only ya
     * estaban en el config pero no las leia nadie, de modo que cambiarlas tampoco hacia nada.
     *
     * No se comprueba aqui si el PvP esta permitido en el sitio. De eso se encarga el propio
     * damage(), que dispara el evento de dano normal: WorldGuard y ProtectionStones lo cancelan
     * igual que cancelarian un espadazo. Duplicar esa logica aqui seria arriesgarse a que la
     * magia y el acero acabasen respetando reglas distintas.
     */
    private Collection<LivingEntity> nearbyTargets(Player caster, Location location, double radius) {
        boolean soloMonstruos = plugin.getConfig().getBoolean("combat.monsters-only", false)
                || !plugin.getConfig().getBoolean("combat.allow-player-vs-player", true);

        return location.getWorld().getNearbyLivingEntities(location, radius, radius, radius, entity -> {
            if (entity.isDead()) {
                return false;
            }
            if (entity instanceof Monster) {
                return true;
            }
            if (soloMonstruos || !(entity instanceof Player victim)) {
                return false;
            }
            return esObjetivoValido(caster, victim);
        });
    }

    /** Descarta al propio lanzador y a quien no deberia recibir un golpe de nadie. */
    private static boolean esObjetivoValido(Player caster, Player victim) {
        if (victim.equals(caster) || victim.isInvulnerable()) {
            return false;
        }
        if (victim.getGameMode() == org.bukkit.GameMode.CREATIVE
                || victim.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
            return false;
        }
        // El staff en vanish no esta ahi para el resto del servidor; tampoco para un hechizo.
        return !victim.hasMetadata("vanished");
    }
}
