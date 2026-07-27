package cl.drakescraft.arcana;

import java.sql.SQLException;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

final class ArcanaJoinListener implements Listener {
    private final DrakesArcanaPlugin plugin;
    private final ArcanaRepository repository;
    ArcanaJoinListener(DrakesArcanaPlugin plugin, ArcanaRepository repository) { this.plugin = plugin; this.repository = repository; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            boolean newProfile = repository.find(event.getPlayer().getUniqueId()).isEmpty();
            ArcanaProfile profile = repository.findOrAssign(event.getPlayer().getUniqueId());
            if (!newProfile) return;
            event.getPlayer().spawnParticle(Particle.DUST, event.getPlayer().getLocation().add(0, 1, 0), 42, .45, .65, .45, 0, new Particle.DustOptions(profile.affinity().color(), 1.35F));
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.message("&dLa afinidad te ha elegido: &f" + profile.affinity().displayName() + "&d. Tu origen es &f" + profile.origin().displayName() + "&d. Consulta &f/arcana info&d.")));
        } catch (SQLException exception) {
            plugin.getLogger().warning("No se pudo asignar afinidad a " + event.getPlayer().getUniqueId() + ": " + exception.getMessage());
        }
    }
}
