package cl.drakescraft.arcana;

import java.sql.SQLException;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class DrakesArcanaPlugin extends JavaPlugin {
    private ArcanaRepository repository;
    private ArcanaEffects effects;
    private DivineBridge divine;
    private SpiritualityService spirituality;

    @Override public void onEnable() {
        saveDefaultConfig();
        try {
            repository = new ArcanaRepository(getDataFolder());
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo abrir la base de datos de Arcana", exception);
        }
        effects = new ArcanaEffects(this);
        divine = new DivineBridge(this);
        spirituality = new SpiritualityService(this, divine);
        ArcanaCommand command = new ArcanaCommand(this, repository, effects, spirituality, divine);
        Objects.requireNonNull(getCommand("arcana")).setExecutor(command);
        Objects.requireNonNull(getCommand("arcana")).setTabCompleter(command);
        getServer().getPluginManager().registerEvents(new ArcanaJoinListener(this, repository), this);
        getLogger().info("DrakesArcana ready: affinities, codex, safe domains and spirituality.");
    }

    @Override public void onDisable() {
        if (repository == null) return;
        try { repository.close(); } catch (SQLException exception) { getLogger().warning("No se pudo cerrar Arcana SQLite: " + exception.getMessage()); }
    }

    String message(String text) { return getConfig().getString("messages.prefix", "&8[&dArcana&8] ") + text; }
}
