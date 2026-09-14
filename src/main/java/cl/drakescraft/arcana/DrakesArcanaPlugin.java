package cl.drakescraft.arcana;

import java.sql.SQLException;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class DrakesArcanaPlugin extends JavaPlugin {
    private ArcanaRepository repository;
    private ArcanaEffects effects;
    private DivineBridge divine;
    private SpiritualityService spirituality;
    private ArcanaGuideMenu guide;
    private ArcanaCatalystListener catalysts;
    private TranscendenceService transcendenceService;
    private ArcanaTranscendenceMenu transcendenceMenu;

    @Override public void onEnable() {
        saveDefaultConfig();
        try {
            repository = new ArcanaRepository(getDataFolder());
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo abrir la base de datos de Arcana", exception);
        }
        repository.chaosChance(getConfig().getDouble("affinities.chaos-chance", 0.02D));
        effects = new ArcanaEffects(this);
        divine = new DivineBridge(this);
        spirituality = new SpiritualityService(this, divine);
        guide = new ArcanaGuideMenu(this, repository, divine, spirituality);

        transcendenceService = new TranscendenceService(this, repository);
        transcendenceMenu = new ArcanaTranscendenceMenu(this, repository, transcendenceService);
        catalysts = new ArcanaCatalystListener(this, repository, effects, transcendenceService);

        ArcanaCommand command = new ArcanaCommand(this, repository, effects, spirituality, divine, guide, transcendenceMenu, transcendenceService);
        Objects.requireNonNull(getCommand("arcana")).setExecutor(command);
        Objects.requireNonNull(getCommand("arcana")).setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new ArcanaJoinListener(this, repository), this);
        getServer().getPluginManager().registerEvents(catalysts, this);
        getServer().getPluginManager().registerEvents(guide, this);
        getServer().getPluginManager().registerEvents(transcendenceMenu, this);
        getServer().getPluginManager().registerEvents(new TranscendenceListener(this, transcendenceService), this);

        getLogger().info("DrakesArcana ready: affinities, codex, safe domains, spirituality & transcendental anime arts (Dragon Ball, Naruto, One Piece).");
    }

    @Override public void onDisable() {
        if (repository == null) return;
        try { repository.close(); } catch (SQLException exception) { getLogger().warning("No se pudo cerrar Arcana SQLite: " + exception.getMessage()); }
    }

    public String message(String text) { return getConfig().getString("messages.prefix", "&8[&dArcana&8] ") + text; }

    public ArcanaCatalystListener catalysts() { return catalysts; }
    public TranscendenceService transcendence() { return transcendenceService; }
    public ArcanaTranscendenceMenu transcendenceMenu() { return transcendenceMenu; }
}
