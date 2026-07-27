package cl.drakescraft.arcana;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;

final class ArcanaCommand implements CommandExecutor, TabCompleter {
    private final DrakesArcanaPlugin plugin;
    private final ArcanaRepository repository;
    private final ArcanaEffects effects;
    ArcanaCommand(DrakesArcanaPlugin plugin, ArcanaRepository repository, ArcanaEffects effects) { this.plugin = plugin; this.repository = repository; this.effects = effects; }

    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Solo jugadores."); return true; }
        try {
            ArcanaProfile profile = repository.findOrAssign(player.getUniqueId());
            if (args.length == 0 || args[0].equalsIgnoreCase("info")) return info(player, profile);
            if (args[0].equalsIgnoreCase("book")) return book(player, profile);
            if (args[0].equalsIgnoreCase("cast") && args.length >= 2) return cast(player, profile, args[1]);
            if (args[0].equalsIgnoreCase("staff") && args.length >= 4 && args[1].equalsIgnoreCase("set")) return setAffinity(player, args[2], args[3]);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.message("&f/arcana info&7, &f/arcana book&7, &f/arcana cast <pulso|dominio>")));
        } catch (SQLException exception) { player.sendMessage(ChatColor.RED + "Arcana no pudo leer tu afinidad."); plugin.getLogger().warning(exception.getMessage()); }
        return true;
    }

    private boolean info(Player player, ArcanaProfile profile) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.message("&dAfinidad: &f" + profile.affinity().displayName() + "&d. &7Luz y Sombra se desbloquean como disciplinas avanzadas.")));
        return true;
    }
    private boolean book(Player player, ArcanaProfile profile) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setTitle("Codice de Arcana"); meta.setAuthor("DrakesCraft");
        meta.addPages(Component.text("EL CODICE\n\nTu afinidad primaria: " + profile.affinity().displayName() + "\n\nUsa /arcana cast pulso o /arcana cast dominio.\n\nLos dominios son visuales, no rompen bloques y solo afectan criaturas hostiles."),
            Component.text("DISCIPLINAS\n\nLuz y Sombra llegan como rutas avanzadas. La afinidad primaria se asigna una vez; el staff puede corregirla con trazabilidad."));
        item.setItemMeta(meta); player.getInventory().addItem(item); return true;
    }
    private boolean cast(Player player, ArcanaProfile profile, String spell) {
        boolean cast = switch (spell.toLowerCase(Locale.ROOT)) {
            case "pulso", "pulse" -> effects.castPulse(player, profile.affinity());
            case "dominio", "domain" -> effects.castDomain(player, profile.affinity());
            default -> false;
        };
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.message(cast ? "&dLa afinidad responde." : "&7No puedes usar esa habilidad aquí, está en cooldown o el dominio está al límite.")));
        return true;
    }
    private boolean setAffinity(Player staff, String targetName, String requested) throws SQLException {
        if (!staff.hasPermission("drakesarcana.staff")) { staff.sendMessage(ChatColor.RED + "Sin permiso."); return true; }
        Player target = plugin.getServer().getPlayerExact(targetName);
        if (target == null) { staff.sendMessage(ChatColor.RED + "Jugador no conectado."); return true; }
        Affinity affinity;
        try { affinity = Affinity.valueOf(requested.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException invalid) { staff.sendMessage(ChatColor.RED + "Afinidad inválida."); return true; }
        repository.save(new ArcanaProfile(target.getUniqueId(), affinity));
        staff.sendMessage(ChatColor.GREEN + "Afinidad actualizada.");
        target.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.message("&dEl staff ajustó tu afinidad a &f" + affinity.displayName() + "&d.")));
        return true;
    }
    @Override public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) return filter(args[0], List.of("info", "book", "cast", "staff"));
        if (args.length == 2 && args[0].equalsIgnoreCase("cast")) return filter(args[1], List.of("pulso", "dominio"));
        if (args.length == 2 && args[0].equalsIgnoreCase("staff")) return filter(args[1], List.of("set"));
        if (args.length == 3 && args[0].equalsIgnoreCase("staff")) return filter(args[2], plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
        if (args.length == 4 && args[0].equalsIgnoreCase("staff")) return filter(args[3], Stream.of(Affinity.values()).map(value -> value.name().toLowerCase(Locale.ROOT)).toList());
        return List.of();
    }
    private static List<String> filter(String prefix, List<String> values) { return values.stream().filter(value -> value.startsWith(prefix.toLowerCase(Locale.ROOT))).toList(); }
}
