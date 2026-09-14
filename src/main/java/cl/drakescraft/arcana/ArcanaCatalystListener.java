package cl.drakescraft.arcana;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/** Handles Arcana ability input through the Catalyst with multi-mode selection. */
final class ArcanaCatalystListener implements Listener {
    public enum CatalystMode {
        PULSE("Pulso Arcano", ChatColor.LIGHT_PURPLE),
        DOMAIN("Dominio Arcano", ChatColor.GOLD),
        TRANSCENDENCE("Arte Trascendental", ChatColor.AQUA);

        private final String displayName;
        private final ChatColor color;

        CatalystMode(String displayName, ChatColor color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String displayName() { return displayName; }
        public ChatColor color() { return color; }

        public CatalystMode next() {
            CatalystMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private final DrakesArcanaPlugin plugin;
    private final ArcanaRepository repository;
    private final ArcanaEffects effects;
    private final TranscendenceService transcendence;
    private final NamespacedKey catalystKey;
    private final Map<UUID, CatalystMode> selectedModes = new HashMap<>();

    ArcanaCatalystListener(DrakesArcanaPlugin plugin, ArcanaRepository repository,
                           ArcanaEffects effects, TranscendenceService transcendence) {
        this.plugin = plugin;
        this.repository = repository;
        this.effects = effects;
        this.transcendence = transcendence;
        this.catalystKey = new NamespacedKey(plugin, "arcana_catalyst");
    }

    ItemStack createCatalyst() {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Catalizador Arcano");
            meta.setLore(java.util.List.of(
                    ChatColor.GRAY + "Clic derecho: ejecutar modo activo",
                    ChatColor.GRAY + "Agachado: forzar Dominio Arcano",
                    ChatColor.YELLOW + "Cambiar de mano (F): alternar modos",
                    ChatColor.DARK_AQUA + "  • Pulso | Dominio | Arte Trascendental",
                    ChatColor.DARK_GRAY + "/arcana artes para elegir tu técnica"
            ));
            meta.getPersistentDataContainer().set(catalystKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    boolean giveIfMissing(Player player) {
        if (player.getInventory().all(Material.AMETHYST_SHARD).values().stream().anyMatch(this::isCatalyst)) return false;
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(createCatalyst());
        if (!overflow.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), createCatalyst());
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onUse(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick() || !isCatalyst(event.getItem())) return;
        event.setCancelled(true);
        Player player = event.getPlayer();

        try {
            ArcanaProfile profile = repository.findOrAssign(player.getUniqueId());
            CatalystMode mode = selectedModes.getOrDefault(player.getUniqueId(), CatalystMode.PULSE);

            if (player.isSneaking()) {
                // Sneaking forces Domain
                boolean used = effects.castDomain(player, profile.affinity());
                player.sendActionBar(Component.text(used
                        ? ChatColor.GOLD + "✦ Dominio Arcano activado (" + profile.affinity().displayName() + ")"
                        : ChatColor.RED + "Dominio en recarga o no permitido aquí."));
                return;
            }

            switch (mode) {
                case PULSE -> {
                    boolean used = effects.castPulse(player, profile.affinity());
                    player.sendActionBar(Component.text(used
                            ? ChatColor.LIGHT_PURPLE + "⚡ Pulso Arcano liberado (" + profile.affinity().displayName() + ")"
                            : ChatColor.RED + "Pulso en recarga o mundo no ofensivo."));
                }
                case DOMAIN -> {
                    boolean used = effects.castDomain(player, profile.affinity());
                    player.sendActionBar(Component.text(used
                            ? ChatColor.GOLD + "✦ Dominio Arcano activado (" + profile.affinity().displayName() + ")"
                            : ChatColor.RED + "Dominio en recarga o límite de dominios activos alcanzado."));
                }
                case TRANSCENDENCE -> {
                    TranscendenceTechnique tech = profile.equippedTechnique();
                    if (tech == null || tech == TranscendenceTechnique.NONE) {
                        player.sendActionBar(Component.text(ChatColor.YELLOW + "Sin arte equipado. Usa /arcana artes para elegir uno."));
                        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0F, 1.2F);
                        return;
                    }
                    boolean used = transcendence.execute(player, tech, profile);
                    if (used) {
                        player.sendActionBar(Component.text(ChatColor.AQUA + "★ " + tech.displayName() + " desatado!"));
                    }
                }
            }
        } catch (SQLException exception) {
            plugin.getLogger().warning("No se pudo usar Catalizador Arcano: " + exception.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (!isCatalyst(event.getMainHandItem()) && !isCatalyst(event.getOffHandItem())) return;
        event.setCancelled(true);
        Player player = event.getPlayer();

        CatalystMode current = selectedModes.getOrDefault(player.getUniqueId(), CatalystMode.PULSE);
        CatalystMode next = current.next();
        selectedModes.put(player.getUniqueId(), next);

        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 0.7F, 1.4F);

        String extra = "";
        try {
            ArcanaProfile p = repository.findOrAssign(player.getUniqueId());
            if (next == CatalystMode.TRANSCENDENCE) {
                TranscendenceTechnique t = p.equippedTechnique();
                extra = " (" + (t != null && t != TranscendenceTechnique.NONE ? t.displayName() : "Ninguno - /arcana artes") + ")";
            }
        } catch (SQLException ignored) {}

        player.sendActionBar(Component.text(ChatColor.GRAY + "Catalizador: " + next.color() + next.displayName() + ChatColor.DARK_AQUA + extra));
    }

    private boolean isCatalyst(ItemStack item) {
        return item != null && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(catalystKey, PersistentDataType.BYTE);
    }
}
