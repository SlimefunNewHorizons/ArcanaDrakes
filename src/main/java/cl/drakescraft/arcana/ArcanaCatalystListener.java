package cl.drakescraft.arcana;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/** Handles Arcana ability input through the Catalyst instead of chat commands. */
final class ArcanaCatalystListener implements Listener {
    private final DrakesArcanaPlugin plugin;
    private final ArcanaRepository repository;
    private final ArcanaEffects effects;
    private final NamespacedKey catalystKey;
    private final Map<UUID, Boolean> domainSelected = new HashMap<>();

    ArcanaCatalystListener(DrakesArcanaPlugin plugin, ArcanaRepository repository, ArcanaEffects effects) {
        this.plugin = plugin;
        this.repository = repository;
        this.effects = effects;
        this.catalystKey = new NamespacedKey(plugin, "arcana_catalyst");
    }

    ItemStack createCatalyst() {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Catalizador Arcano");
        meta.setLore(java.util.List.of(ChatColor.GRAY + "Clic derecho: habilidad seleccionada",
                ChatColor.GRAY + "Agachado: habilidad alternativa", ChatColor.DARK_GRAY + "Cambiar mano rota alterna la seleccion"));
        meta.getPersistentDataContainer().set(catalystKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    boolean giveIfMissing(org.bukkit.entity.Player player) {
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
        try {
            ArcanaProfile profile = repository.findOrAssign(event.getPlayer().getUniqueId());
            boolean domain = event.getPlayer().isSneaking() || domainSelected.getOrDefault(event.getPlayer().getUniqueId(), false);
            boolean used = domain ? effects.castDomain(event.getPlayer(), profile.affinity())
                    : effects.castPulse(event.getPlayer(), profile.affinity());
            event.getPlayer().sendActionBar(net.kyori.adventure.text.Component.text(used
                    ? (domain ? "Dominio arcano activado" : "Pulso arcano liberado")
                    : "Habilidad bloqueada: revisa mundo, cooldown o limite de dominios."));
        } catch (SQLException exception) {
            plugin.getLogger().warning("No se pudo usar Catalizador Arcano: " + exception.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (!isCatalyst(event.getMainHandItem()) && !isCatalyst(event.getOffHandItem())) return;
        event.setCancelled(true);
        boolean domain = !domainSelected.getOrDefault(event.getPlayer().getUniqueId(), false);
        domainSelected.put(event.getPlayer().getUniqueId(), domain);
        event.getPlayer().sendActionBar(net.kyori.adventure.text.Component.text(
                "Catalizador: " + (domain ? "Dominio" : "Pulso") + " seleccionado"));
    }

    private boolean isCatalyst(ItemStack item) {
        return item != null && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(catalystKey, PersistentDataType.BYTE);
    }
}
