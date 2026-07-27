package cl.drakescraft.arcana;

import java.sql.SQLException;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** A Slimefun-style orientation surface: information first, optional actions in a clearly separated bottom row. */
final class ArcanaGuideMenu implements Listener {
    private final DrakesArcanaPlugin plugin;
    private final ArcanaRepository repository;
    private final DivineBridge divine;
    private final SpiritualityService spirituality;

    ArcanaGuideMenu(DrakesArcanaPlugin plugin, ArcanaRepository repository, DivineBridge divine, SpiritualityService spirituality) {
        this.plugin = plugin;
        this.repository = repository;
        this.divine = divine;
        this.spirituality = spirituality;
    }

    void open(Player player) {
        try {
            ArcanaProfile profile = repository.findOrAssign(player.getUniqueId());
            DivineSnapshot snapshot = divine.snapshot(player.getUniqueId());
            double resonance = spirituality.resonanceMultiplier(profile.affinity(), snapshot);
            Inventory inventory = Bukkit.createInventory(new ArcanaGuideHolder(), 54, Component.text("Arcana Guide | " + profile.affinity().displayName()));
            for (int slot = 0; slot < inventory.getSize(); slot++) inventory.setItem(slot, item(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));

            inventory.setItem(4, item(Material.COMPASS, "Arcane identity", List.of(
                    "Affinity: " + profile.affinity().displayName(),
                    "Origin: " + profile.origin().displayName(),
                    "Rank: " + profile.rank().displayName(),
                    "Sigils: " + profile.sigils() + " | Essence: " + profile.spirit(),
                    "",
                    "Your affinity is assigned on first join.",
                    "Staff can correct it after a verified bad roll."
            )));
            inventory.setItem(10, item(Material.FIRE_CHARGE, "Elemental magic", List.of(
                    "Pulse is a short PvE spell with a cooldown.",
                    "Domain is an area ultimate with a global limit.",
                    "Use: /arcana cast pulse | /arcana cast domain",
                    "",
                    "Effects never break, place or move blocks."
            )));
            inventory.setItem(12, item(Material.AMETHYST_SHARD, "Spirituality and resonance", List.of(
                    "Patron: " + (snapshot.hasPatron() ? snapshot.godDisplayName() + " (" + snapshot.pantheonName() + ")" : "No active DiosesDrakes patron"),
                    "Divine favor: " + snapshot.favor(),
                    "Current resonance: x" + String.format(java.util.Locale.ROOT, "%.2f", resonance),
                    "",
                    "Meditation grants Arcana-only Essence, XP and Sigils.",
                    "Arcana never mints, spends or edits divine favor."
            )));
            inventory.setItem(14, item(Material.DEEPSLATE_EMERALD_ORE, "Progression path", List.of(
                    "Explore safe Arcane Mines for materials and Sigils.",
                    "Ranks gate traders and crafted upgrades.",
                    "Finished Infinity, Supreme gear and machines are excluded.",
                    "AuraSkills remains the base stat system."
            )));
            inventory.setItem(16, item(Material.SHIELD, "Where spells work", List.of(
                    "PvE spells are enabled only in configured worlds.",
                    "boss_arena is the sanctioned Odysseia boss location.",
                    "Player versus player Arcana is disabled by default.",
                    "Claims and protection rules remain authoritative."
            )));
            inventory.setItem(28, item(Material.ENDER_EYE, "How to begin", List.of(
                    "1. Read your identity above.",
                    "2. Meditate when your spirit is ready.",
                    "3. Practice Pulse against monsters in an allowed world.",
                    "4. Enter boss_arena only through the approved boss flow."
            )));
            inventory.setItem(30, item(Material.BOOK, "Useful commands", List.of(
                    "/arcana opens this guide.",
                    "/arcana info shows a short status line.",
                    "/arcana spirit shows resonance details.",
                    "/arcana book gives the portable Codex."
            )));
            inventory.setItem(32, item(Material.LIGHTNING_ROD, "Integration boundaries", List.of(
                    "DiosesDrakes supplies optional patron resonance.",
                    "Odysseia supplies boss_arena.",
                    "ElementManipulation stays independent.",
                    "No integration bypasses protections or duplicates XP."
            )));

            inventory.setItem(45, item(Material.AMETHYST_CLUSTER, "Meditate", List.of(
                    "Gain bounded Arcana progress.",
                    "Uses your configured meditation cooldown.",
                    "Click to meditate."
            )));
            inventory.setItem(48, item(Material.WRITTEN_BOOK, "Receive Arcana Codex", List.of("Adds the portable reference book to your inventory.", "Click to receive it.")));
            inventory.setItem(50, item(Material.CLOCK, "Refresh guide", List.of("Reload your profile, patron and resonance display.", "Click to refresh.")));
            inventory.setItem(53, item(Material.BARRIER, "Close", List.of("Close this guide.")));
            player.openInventory(inventory);
        } catch (SQLException exception) {
            player.sendMessage("Arcana could not load your guide.");
            plugin.getLogger().warning("Could not open Arcana guide: " + exception.getMessage());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof ArcanaGuideHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        switch (event.getRawSlot()) {
            case 45 -> {
                player.closeInventory();
                player.performCommand("arcana meditate");
                plugin.getServer().getScheduler().runTask(plugin, () -> open(player));
            }
            case 48 -> {
                player.closeInventory();
                player.performCommand("arcana book");
            }
            case 50 -> open(player);
            case 53 -> player.closeInventory();
            default -> { }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ArcanaGuideHolder) event.setCancelled(true);
    }

    private static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(lore.stream().map(Component::text).toList());
        stack.setItemMeta(meta);
        return stack;
    }
}
