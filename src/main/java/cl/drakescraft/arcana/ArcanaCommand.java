package cl.drakescraft.arcana;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

/** Player commands deliberately keep the divine bridge informational and non-destructive. */
final class ArcanaCommand implements CommandExecutor, TabCompleter {
    private final DrakesArcanaPlugin plugin;
    private final ArcanaRepository repository;
    private final ArcanaEffects effects;
    private final SpiritualityService spirituality;
    private final DivineBridge divine;
    private final ArcanaGuideMenu guide;

    ArcanaCommand(DrakesArcanaPlugin plugin, ArcanaRepository repository, ArcanaEffects effects,
                  SpiritualityService spirituality, DivineBridge divine, ArcanaGuideMenu guide) {
        this.plugin = plugin;
        this.repository = repository;
        this.effects = effects;
        this.spirituality = spirituality;
        this.divine = divine;
        this.guide = guide;
    }

    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("Only players can use Arcana."); return true; }
        try {
            ArcanaProfile profile = repository.findOrAssign(player.getUniqueId());
            if (args.length == 0 || args[0].equalsIgnoreCase("guide") || args[0].equalsIgnoreCase("menu")) { guide.open(player); return true; }
            if (args[0].equalsIgnoreCase("info")) return info(player, profile);
            if (args[0].equalsIgnoreCase("book")) return book(player, profile);
            if (args[0].equalsIgnoreCase("spirit")) return spirit(player, profile);
            if (args[0].equalsIgnoreCase("meditate")) return meditate(player, profile);
            if (args[0].equalsIgnoreCase("cast") && args.length >= 2) return cast(player, profile, args[1]);
            if (args[0].equalsIgnoreCase("staff") && args.length >= 4 && args[1].equalsIgnoreCase("set")) return setAffinity(player, args[2], args[3]);
            player.sendMessage(colour(plugin.message("&f/arcana&7 opens the guide. &f/arcana info&7, &f/arcana spirit&7, &f/arcana meditate&7, &f/arcana book&7, &f/arcana cast <pulse|domain>")));
        } catch (SQLException exception) {
            player.sendMessage(ChatColor.RED + "Arcana could not read your profile.");
            plugin.getLogger().warning(exception.getMessage());
        }
        return true;
    }

    private boolean info(Player player, ArcanaProfile profile) {
        DivineSnapshot snapshot = divine.snapshot(player.getUniqueId());
        player.sendMessage(colour(plugin.message("&dAffinity: &f" + profile.affinity().displayName() + "&d. Origin: &f" + profile.origin().displayName() + "&d. Rank: &f" + profile.rank().displayName() + "&d. Sigils: &f" + profile.sigils() + "&d. Essence: &f" + profile.spirit())));
        if (snapshot.hasPatron()) player.sendMessage(colour(plugin.message("&dPatron: &f" + snapshot.godDisplayName() + " &7(" + snapshot.pantheonName() + ") &d| Favor: &f" + snapshot.favor() + "&d. Use &f/arcana spirit&d for resonance.")));
        return true;
    }

    private boolean spirit(Player player, ArcanaProfile profile) {
        DivineSnapshot snapshot = divine.snapshot(player.getUniqueId());
        double multiplier = spirituality.resonanceMultiplier(profile.affinity(), snapshot);
        String patron = snapshot.hasPatron() ? snapshot.godDisplayName() + " | Favor: " + snapshot.favor() : "no active patron";
        player.sendMessage(colour(plugin.message("&dEssence: &f" + profile.spirit() + "&d. Patron: &f" + patron + "&d. Resonance: &f" + String.format(Locale.ROOT, "x%.2f", multiplier))));
        return true;
    }

    private boolean meditate(Player player, ArcanaProfile profile) throws SQLException {
        MeditationResult result = spirituality.meditate(player, profile);
        if (!result.success()) {
            player.sendMessage(colour(plugin.message("&7Your spirit is still settling. Try again later.")));
            return true;
        }
        repository.save(result.profile());
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1, 0), 28, .45D, .65D, .45D, .04D);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, .8F, 1.25F);
        player.sendMessage(colour(plugin.message("&dMeditation complete: &f+" + result.experienceGranted() + " XP, +" + result.spiritGranted() + " Essence" + (result.sigilsGranted() > 0 ? ", +" + result.sigilsGranted() + " Sigil" : "") + "&d. Resonance: &f" + String.format(Locale.ROOT, "x%.2f", result.resonanceMultiplier()))));
        return true;
    }

    private boolean book(Player player, ArcanaProfile profile) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setTitle("Arcana Codex");
        meta.setAuthor("DrakesCraft");
        meta.addPages(
                Component.text("THE CODEX\n\nAffinity: " + profile.affinity().displayName() + "\nOrigin: " + profile.origin().displayName() + "\nRank: " + profile.rank().displayName() + "\n\nUse /arcana cast pulse or /arcana cast domain."),
                Component.text("PROGRESSION\n\nArcane mines grant experience and Sigils. Ranks open difficult upgrades without replacing Slimefun endgame."),
                Component.text("SPIRIT\n\nUse /arcana meditate to gain Essence. A compatible DiosesDrakes patron improves resonance using existing favor; Arcana never creates divine favor."),
                Component.text("DISCIPLINES\n\nLight and Shadow are advanced paths. Your primary affinity is assigned once; staff corrections are traceable."));
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
        return true;
    }

    private boolean cast(Player player, ArcanaProfile profile, String spell) {
        boolean cast = switch (spell.toLowerCase(Locale.ROOT)) {
            case "pulso", "pulse" -> effects.castPulse(player, profile.affinity());
            case "dominio", "domain" -> effects.castDomain(player, profile.affinity());
            default -> false;
        };
        player.sendMessage(colour(plugin.message(cast ? "&dYour affinity answers." : "&7You cannot use that ability here, it is on cooldown, or the domain limit was reached.")));
        return true;
    }

    private boolean setAffinity(Player staff, String targetName, String requested) throws SQLException {
        if (!staff.hasPermission("drakesarcana.staff")) { staff.sendMessage(ChatColor.RED + "No permission."); return true; }
        Player target = plugin.getServer().getPlayerExact(targetName);
        if (target == null) { staff.sendMessage(ChatColor.RED + "Player is not online."); return true; }
        Affinity affinity;
        try { affinity = Affinity.valueOf(requested.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException invalid) { staff.sendMessage(ChatColor.RED + "Invalid affinity."); return true; }
        ArcanaProfile prior = repository.findOrAssign(target.getUniqueId());
        repository.save(new ArcanaProfile(target.getUniqueId(), affinity, ArcaneOrigin.randomFor(affinity), prior.experience(), prior.sigils(), prior.spirit()));
        staff.sendMessage(ChatColor.GREEN + "Affinity updated.");
        target.sendMessage(colour(plugin.message("&dStaff adjusted your affinity to &f" + affinity.displayName() + "&d.")));
        return true;
    }

    @Override public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) return filter(args[0], List.of("guide", "info", "spirit", "meditate", "book", "cast", "staff"));
        if (args.length == 2 && args[0].equalsIgnoreCase("cast")) return filter(args[1], List.of("pulse", "domain"));
        if (args.length == 2 && args[0].equalsIgnoreCase("staff")) return filter(args[1], List.of("set"));
        if (args.length == 3 && args[0].equalsIgnoreCase("staff")) return filter(args[2], plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
        if (args.length == 4 && args[0].equalsIgnoreCase("staff")) return filter(args[3], Stream.of(Affinity.values()).map(value -> value.name().toLowerCase(Locale.ROOT)).toList());
        return List.of();
    }

    private static List<String> filter(String prefix, List<String> values) { return values.stream().filter(value -> value.startsWith(prefix.toLowerCase(Locale.ROOT))).toList(); }
    private static String colour(String message) { return ChatColor.translateAlternateColorCodes('&', message); }
}
