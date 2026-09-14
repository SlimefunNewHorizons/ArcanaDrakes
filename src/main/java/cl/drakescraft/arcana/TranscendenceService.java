package cl.drakescraft.arcana;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class TranscendenceService {
    private final DrakesArcanaPlugin plugin;
    private final ArcanaRepository repository;
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    private final Set<UUID> ultraInstinctActive = ConcurrentHashMap.newKeySet();
    private final Set<UUID> armamentActive = ConcurrentHashMap.newKeySet();
    private final Set<UUID> susanooActive = ConcurrentHashMap.newKeySet();
    private final Set<UUID> kaiokenActive = ConcurrentHashMap.newKeySet();

    public TranscendenceService(DrakesArcanaPlugin plugin, ArcanaRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public boolean isUltraInstinct(UUID playerId) { return ultraInstinctActive.contains(playerId); }
    public boolean isArmament(UUID playerId) { return armamentActive.contains(playerId); }
    public boolean isSusanoo(UUID playerId) { return susanooActive.contains(playerId); }
    public boolean isKaioken(UUID playerId) { return kaiokenActive.contains(playerId); }

    public boolean execute(Player player, TranscendenceTechnique technique, ArcanaProfile profile) {
        if (technique == null || technique == TranscendenceTechnique.NONE) {
            player.sendMessage(plugin.message("&7No tienes ninguna técnica trascendental seleccionada. Usa &f/arcana artes&7 para elegir una."));
            return false;
        }

        if (!canUseOffensively(player)) {
            player.sendMessage(plugin.message("&cLas artes trascendentales no están habilitadas en este mundo."));
            return false;
        }

        if (profile.experience() < technique.minRank().requiredExperience()) {
            player.sendMessage(plugin.message("&cNecesitas alcanzar el rango &e" + technique.minRank().displayName() + " &cpara usar esta técnica."));
            return false;
        }

        if (profile.spirit() < technique.spiritCost()) {
            player.sendMessage(plugin.message("&cNecesitas &f" + technique.spiritCost() + " &cde Esencia. Tienes &f" + profile.spirit() + "&c. Medita con &f/arcana meditar&c."));
            return false;
        }

        String cdKey = player.getUniqueId() + ":" + technique.name();
        long now = System.currentTimeMillis();
        long nextReady = cooldowns.getOrDefault(cdKey, 0L);
        if (now < nextReady) {
            long remainingSec = (nextReady - now) / 1000L;
            player.sendMessage(plugin.message("&cTécnica en recarga: espera &f" + remainingSec + "s&c."));
            return false;
        }

        boolean launched = switch (technique) {
            case GENKIDAMA -> castGenkidama(player);
            case ULTRA_INSTINCT -> castUltraInstinct(player);
            case KAIOKEN -> castKaioken(player);
            case RASENGAN -> castRasengan(player);
            case CHIDORI -> castChidori(player);
            case SUSANOO -> castSusanoo(player);
            case CONQUERORS_HAKI -> castConquerorsHaki(player);
            case ARMAMENT_HAKI -> castArmamentHaki(player);
            case GEAR_SECOND -> castGearSecond(player);
            default -> false;
        };

        if (launched) {
            cooldowns.put(cdKey, now + (technique.cooldownSeconds() * 1000L));
            if (technique.spiritCost() > 0) {
                try {
                    ArcanaProfile updated = new ArcanaProfile(
                            profile.playerId(), profile.affinity(), profile.origin(),
                            profile.experience(), profile.sigils(), profile.spirit() - technique.spiritCost(),
                            profile.equippedTechnique()
                    );
                    repository.save(updated);
                } catch (Exception ex) {
                    plugin.getLogger().warning("Error deduciendo espíritu tras técnica: " + ex.getMessage());
                }
            }
        }
        return launched;
    }

    // --- DRAGON BALL ---

    private boolean castGenkidama(Player player) {
        player.sendMessage(plugin.message("&b¡Reuniendo energía vital cósmica para la &eGenkidama&b!"));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 0.6F);

        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (!player.isOnline() || player.isDead()) { cancel(); return; }
                Location head = player.getEyeLocation().add(0, 2.5D, 0);

                double radius = 0.5D + (ticks * 0.05D);
                int points = 16;
                for (int i = 0; i < points; i++) {
                    double angle = (Math.PI * 2 * i / points) + (ticks * 0.2D);
                    Location p = head.clone().add(Math.cos(angle) * radius, Math.sin(ticks * 0.1D) * 0.4D, Math.sin(angle) * radius);
                    player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p, 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, p, 1, 0.02, 0.02, 0.02, 0.01);
                }
                player.getWorld().spawnParticle(Particle.END_ROD, head, 3, 0.2, 0.2, 0.2, 0.01);

                if (ticks % 10 == 0) {
                    player.playSound(head, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8F, 1.4F);
                }

                ticks += 2;
                if (ticks >= 40) { // 2 seconds charging completed
                    cancel();
                    launchGenkidamaProjectile(player, head);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);

        return true;
    }

    private void launchGenkidamaProjectile(Player player, Location startLocation) {
        Vector dir = player.getEyeLocation().getDirection().normalize().multiply(0.85D);
        Location cur = startLocation.clone();
        player.playSound(cur, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.2F, 0.8F);
        player.playSound(cur, Sound.ITEM_TRIDENT_THUNDER, 1.0F, 1.1F);

        new BukkitRunnable() {
            int steps = 0;
            @Override public void run() {
                if (!player.isOnline() || steps > 35) {
                    detonateGenkidama(player, cur);
                    cancel();
                    return;
                }
                cur.add(dir);

                // Orb visualization
                for (int i = 0; i < 12; i++) {
                    double theta = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
                    double phi = ThreadLocalRandom.current().nextDouble(0, Math.PI);
                    double r = 1.4D;
                    double x = r * Math.sin(phi) * Math.cos(theta);
                    double y = r * Math.sin(phi) * Math.sin(theta);
                    double z = r * Math.cos(phi);
                    Location p = cur.clone().add(x, y, z);
                    cur.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p, 1, 0, 0, 0, 0);
                }
                cur.getWorld().spawnParticle(Particle.FLASH, cur, 1, 0, 0, 0, 0);

                if (!cur.getBlock().isPassable()) {
                    detonateGenkidama(player, cur);
                    cancel();
                    return;
                }

                Collection<LivingEntity> targets = nearbyTargets(player, cur, 2.2D);
                if (!targets.isEmpty()) {
                    detonateGenkidama(player, cur);
                    cancel();
                    return;
                }
                steps++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void detonateGenkidama(Player player, Location loc) {
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 3, 0.5, 0.5, 0.5, 0.1);
        loc.getWorld().spawnParticle(Particle.FLASH, loc, 4, 1.0, 1.0, 1.0, 0.05);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5F, 0.7F);
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.5F, 1.2F);

        for (LivingEntity target : nearbyTargets(player, loc, 8.0D)) {
            target.damage(24.0D, player);
            Vector push = target.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(1.8D).setY(0.45D);
            target.setVelocity(target.getVelocity().add(push));
            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.1);
        }
    }

    private boolean castUltraInstinct(Player player) {
        UUID id = player.getUniqueId();
        ultraInstinctActive.add(id);

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 2, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 15 * 20, 1, false, false, true));

        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.8F);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.8F, 1.9F);
        player.sendMessage(plugin.message("&f¡Has despertado el &bUltra Instinto&f! Evasión absoluta activada durante 15s."));

        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (!player.isOnline() || !ultraInstinctActive.contains(id) || ticks >= 15 * 20) {
                    ultraInstinctActive.remove(id);
                    if (player.isOnline()) {
                        player.sendMessage(plugin.message("&7El estado de Ultra Instinto se ha disipado."));
                    }
                    cancel();
                    return;
                }

                // Silver & celestial aura rings
                Location loc = player.getLocation();
                for (int i = 0; i < 8; i++) {
                    double angle = (Math.PI * 2 * i / 8) + (ticks * 0.15D);
                    Location p = loc.clone().add(Math.cos(angle) * 0.9D, (ticks % 20) * 0.1D, Math.sin(angle) * 0.9D);
                    player.getWorld().spawnParticle(Particle.SNOWFLAKE, p, 1, 0, 0, 0, 0);
                    player.getWorld().spawnParticle(Particle.SOUL, p, 1, 0.01, 0.01, 0.01, 0.01);
                }
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        return true;
    }

    private boolean castKaioken(Player player) {
        UUID id = player.getUniqueId();
        kaiokenActive.add(id);

        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 12 * 20, 1, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12 * 20, 1, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 12 * 20, 0, false, false, true));

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.9F, 1.6F);
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.2F, 0.5F);
        player.sendMessage(plugin.message("&c¡KAIŌ-KEN activado! Multiplicando potencia física a costa de tu vigor corporal."));

        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (!player.isOnline() || !kaiokenActive.contains(id) || ticks >= 12 * 20) {
                    kaiokenActive.remove(id);
                    if (player.isOnline()) {
                        player.sendMessage(plugin.message("&7El Kaiō-ken ha finalizado. Tu cuerpo recupera su estado normal."));
                    }
                    cancel();
                    return;
                }

                // Crimson red aura
                Location loc = player.getLocation();
                Particle.DustOptions redDust = new Particle.DustOptions(Color.fromRGB(230, 20, 20), 1.6F);
                for (int i = 0; i < 6; i++) {
                    double offX = ThreadLocalRandom.current().nextDouble(-0.6D, 0.6D);
                    double offY = ThreadLocalRandom.current().nextDouble(0.1D, 1.9D);
                    double offZ = ThreadLocalRandom.current().nextDouble(-0.6D, 0.6D);
                    player.getWorld().spawnParticle(Particle.DUST, loc.clone().add(offX, offY, offZ), 1, 0, 0, 0, 0, redDust);
                    player.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(offX, offY, offZ), 1, 0, 0.04, 0, 0.01);
                }

                // Body strain every 3 seconds (60 ticks)
                if (ticks > 0 && ticks % 60 == 0) {
                    if (player.getHealth() > 2.5D) {
                        player.setHealth(player.getHealth() - 2.0D);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.6F, 1.0F);
                    }
                }
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        return true;
    }

    // --- NARUTO ---

    private boolean castRasengan(Player player) {
        Location start = player.getEyeLocation();
        Vector dir = start.getDirection().normalize();
        player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1.0F, 0.8F);
        player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1.0F, 1.5F);
        player.sendMessage(plugin.message("&b¡Técnica Shinobi: &3Rasengan&b liberado!"));

        Location cur = start.clone();
        for (int step = 0; step < 16; step++) {
            cur.add(dir.clone().multiply(0.65D));

            // Vortex particles
            for (int i = 0; i < 8; i++) {
                double angle = (Math.PI * 2 * i / 8) + (step * 0.4D);
                Location p = cur.clone().add(Math.cos(angle) * 0.7D, Math.sin(step * 0.3D) * 0.3D, Math.sin(angle) * 0.7D);
                cur.getWorld().spawnParticle(Particle.SWEEP_ATTACK, p, 1, 0, 0, 0, 0);
                cur.getWorld().spawnParticle(Particle.CLOUD, p, 1, 0.01, 0.01, 0.01, 0.01);
            }

            Collection<LivingEntity> targets = nearbyTargets(player, cur, 1.4D);
            if (!targets.isEmpty()) {
                for (LivingEntity t : targets) {
                    t.damage(14.0D, player);
                    Vector blast = dir.clone().multiply(1.7D).setY(0.4D);
                    t.setVelocity(t.getVelocity().add(blast));
                    t.getWorld().playSound(t.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1.2F, 1.2F);
                    t.getWorld().spawnParticle(Particle.EXPLOSION, t.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                }
                return true;
            }
            if (!cur.getBlock().isPassable()) break;
        }
        return true;
    }

    private boolean castChidori(Player player) {
        Vector dir = player.getEyeLocation().getDirection().normalize();
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 1.8F);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8F, 2.0F);
        player.sendMessage(plugin.message("&9¡Técnica de Rayo: &bChidori (Millar de Pájaros)&9!"));

        // Sonic dash
        player.setVelocity(dir.clone().multiply(1.85D).setY(0.2D));

        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (!player.isOnline() || ticks > 8) { cancel(); return; }
                Location loc = player.getLocation().add(0, 0.9D, 0);

                // Electric aura
                for (int i = 0; i < 10; i++) {
                    loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 2, 0.4, 0.4, 0.4, 0.1);
                    loc.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0.3, 0.3, 0.3, 0.05);
                }

                for (LivingEntity t : nearbyTargets(player, loc, 1.8D)) {
                    t.damage(16.0D, player);
                    t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3 * 20, 3, false, false, true));
                    t.getWorld().playSound(t.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 1.5F);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private boolean castSusanoo(Player player) {
        UUID id = player.getUniqueId();
        susanooActive.add(id);

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 10 * 20, 3, false, false, true));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0F, 0.7F);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 0.5F);
        player.sendMessage(plugin.message("&5¡Armadura Espectral: &dSusanoo&5 manifestado durante 10s!"));

        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (!player.isOnline() || !susanooActive.contains(id) || ticks >= 10 * 20) {
                    susanooActive.remove(id);
                    if (player.isOnline()) {
                        player.sendMessage(plugin.message("&7El Susanoo se ha desvanecido en el plano etéreo."));
                    }
                    cancel();
                    return;
                }

                // Ribcage & spiritual purple flames
                Location center = player.getLocation().add(0, 1.0D, 0);
                Particle.DustOptions purpleDust = new Particle.DustOptions(Color.fromRGB(160, 32, 240), 1.7F);
                for (int i = 0; i < 14; i++) {
                    double angle = Math.PI * 2 * i / 14;
                    double y = Math.sin(angle * 2) * 0.8D;
                    Location rib = center.clone().add(Math.cos(angle) * 1.5D, y, Math.sin(angle) * 1.5D);
                    center.getWorld().spawnParticle(Particle.DUST, rib, 1, 0, 0, 0, 0, purpleDust);
                    center.getWorld().spawnParticle(Particle.DRAGON_BREATH, rib, 1, 0, 0, 0, 0.01);
                }

                // Repulsion pulse every 30 ticks (1.5s)
                if (ticks > 0 && ticks % 30 == 0) {
                    for (LivingEntity t : nearbyTargets(player, center, 3.5D)) {
                        Vector push = t.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.3D).setY(0.35D);
                        t.setVelocity(t.getVelocity().add(push));
                        t.damage(4.0D, player);
                        t.getWorld().playSound(t.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8F, 1.3F);
                    }
                }
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        return true;
    }

    // --- ONE PIECE ---

    private boolean castConquerorsHaki(Player player) {
        Location center = player.getLocation();
        player.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.4F, 0.7F);
        player.playSound(center, Sound.BLOCK_BELL_RESONATE, 1.2F, 0.5F);
        player.sendMessage(plugin.message("&4¡Voluntad Suprema: &cHaoshoku Haki (Conquistador)&4 desatado!"));

        Particle.DustOptions blackRed = new Particle.DustOptions(Color.fromRGB(30, 0, 0), 2.0F);
        for (int r = 2; r <= 12; r += 2) {
            final int radius = r;
            new BukkitRunnable() {
                @Override public void run() {
                    for (int i = 0; i < 24; i++) {
                        double angle = Math.PI * 2 * i / 24;
                        Location p = center.clone().add(Math.cos(angle) * radius, 0.2D, Math.sin(angle) * radius);
                        center.getWorld().spawnParticle(Particle.DUST, p, 2, 0.1, 0.1, 0.1, 0, blackRed);
                        center.getWorld().spawnParticle(Particle.SONIC_BOOM, p, 1, 0, 0, 0, 0);
                    }
                }
            }.runTaskLater(plugin, (r / 2) * 2L);
        }

        new BukkitRunnable() {
            @Override public void run() {
                for (LivingEntity t : nearbyTargets(player, center, 12.0D)) {
                    // Knock out or heavy stagger
                    if (t.getHealth() <= 25.0D && !(t instanceof Player)) {
                        t.damage(26.0D, player);
                        t.getWorld().spawnParticle(Particle.FLASH, t.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
                    } else {
                        t.damage(12.0D, player);
                        t.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 6 * 20, 4, false, false, true));
                        t.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 6 * 20, 4, false, false, true));
                    }
                }
            }
        }.runTaskLater(plugin, 6L);

        return true;
    }

    private boolean castArmamentHaki(Player player) {
        UUID id = player.getUniqueId();
        armamentActive.add(id);

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 20 * 20, 1, false, false, true));
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.9F, 1.4F);
        player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0F, 0.8F);
        player.sendMessage(plugin.message("&8¡Endurecimiento: &0Busoshoku Haki (Armadura)&8 activo por 20s!"));

        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (!player.isOnline() || !armamentActive.contains(id) || ticks >= 20 * 20) {
                    armamentActive.remove(id);
                    if (player.isOnline()) {
                        player.sendMessage(plugin.message("&7El endurecimiento de Armadura se ha enfriado."));
                    }
                    cancel();
                    return;
                }

                Location loc = player.getLocation().add(0, 1.0D, 0);
                loc.getWorld().spawnParticle(Particle.SMOKE, loc, 3, 0.35, 0.5, 0.35, 0.02);
                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);

        return true;
    }

    private boolean castGearSecond(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 3, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 15 * 20, 1, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 15 * 20, 1, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 8 * 20, 1, false, false, true));

        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.2F, 1.6F);
        player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1.0F, 1.4F);
        player.sendMessage(plugin.message("&c¡Aceleración: &eGear Second (Segunda Marcha)&c activado por 15s!"));

        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (!player.isOnline() || ticks >= 15 * 20) {
                    cancel();
                    return;
                }
                Location loc = player.getLocation();
                loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0, 0.2D, 0), 2, 0.2, 0.1, 0.2, 0.01);
                loc.getWorld().spawnParticle(Particle.POOF, loc.clone().add(0, 1.0D, 0), 1, 0.1, 0.3, 0.1, 0.01);
                ticks += 3;
            }
        }.runTaskTimer(plugin, 0L, 3L);

        return true;
    }

    private boolean canUseOffensively(Player player) {
        List<String> worlds = plugin.getConfig().getStringList("combat.offensive-worlds");
        if (worlds.isEmpty()) worlds = plugin.getConfig().getStringList("worlds.offensive-enabled");
        return worlds.contains(player.getWorld().getName());
    }

    private Collection<LivingEntity> nearbyTargets(Player caster, Location location, double radius) {
        boolean soloMonstruos = plugin.getConfig().getBoolean("combat.monsters-only", false)
                || !plugin.getConfig().getBoolean("combat.allow-player-vs-player", true);

        return location.getWorld().getNearbyLivingEntities(location, radius, radius, radius, entity -> {
            if (entity.isDead() || entity.equals(caster)) return false;
            if (entity instanceof Monster) return true;
            if (soloMonstruos || !(entity instanceof Player victim)) return false;
            return !victim.isInvulnerable() && victim.getGameMode() != org.bukkit.GameMode.CREATIVE
                    && victim.getGameMode() != org.bukkit.GameMode.SPECTATOR && !victim.hasMetadata("vanished");
        });
    }
}
