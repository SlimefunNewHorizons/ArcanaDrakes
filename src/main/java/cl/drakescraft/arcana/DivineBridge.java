package cl.drakescraft.arcana;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.plugin.Plugin;

/** Reads the public DiosesDrakes service through reflection, keeping Arcana bootable on its own. */
final class DivineBridge {
    private static final String ACCESS_TYPE = "cl.drakescraft.diosesdrakes.api.DivineAccess";
    private final DrakesArcanaPlugin plugin;
    private boolean warned;

    DivineBridge(DrakesArcanaPlugin plugin) { this.plugin = plugin; }

    DivineSnapshot snapshot(UUID playerId) {
        Plugin divinePlugin = plugin.getServer().getPluginManager().getPlugin("DiosesDrakes");
        if (divinePlugin == null || !divinePlugin.isEnabled()) return DivineSnapshot.unavailable();
        try {
            ClassLoader loader = divinePlugin.getClass().getClassLoader();
            Class<?> accessType = Class.forName(ACCESS_TYPE, false, loader);
            Object access = plugin.getServer().getServicesManager().load(accessType);
            if (access == null) return DivineSnapshot.unavailable();
            Method activeGod = accessType.getMethod("activeGod", UUID.class);
            Optional<?> god = (Optional<?>) activeGod.invoke(access, playerId);
            if (god.isEmpty()) return DivineSnapshot.unbound();
            Object deity = god.get();
            String identifier = ((Enum<?>) deity).name();
            String display = String.valueOf(deity.getClass().getMethod("displayName").invoke(deity));
            Object pantheon = deity.getClass().getMethod("pantheon").invoke(deity);
            String pantheonName = String.valueOf(pantheon.getClass().getMethod("displayName").invoke(pantheon));
            int favor = (int) accessType.getMethod("currentFavor", UUID.class).invoke(access, playerId);
            return new DivineSnapshot(true, identifier, display, pantheonName, Math.max(0, favor));
        } catch (ReflectiveOperationException | ClassCastException exception) {
            if (!warned) {
                warned = true;
                plugin.getLogger().warning("DiosesDrakes was found but its public service is unavailable: " + exception.getClass().getSimpleName());
            }
            return DivineSnapshot.unavailable();
        }
    }
}
