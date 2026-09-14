package cl.drakescraft.arcana;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ArcanaTranscendenceMenu implements Listener {
    private final DrakesArcanaPlugin plugin;
    private final ArcanaRepository repository;
    private final TranscendenceService service;

    public static final class MenuHolder implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }

    public ArcanaTranscendenceMenu(DrakesArcanaPlugin plugin, ArcanaRepository repository, TranscendenceService service) {
        this.plugin = plugin;
        this.repository = repository;
        this.service = service;
    }

    public void open(Player player) {
        try {
            ArcanaProfile profile = repository.findOrAssign(player.getUniqueId());
            Inventory inv = Bukkit.createInventory(new MenuHolder(), 54, Component.text(ChatColor.DARK_PURPLE + "Artes Trascendentales"));

            ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
            for (int i = 0; i < 54; i++) inv.setItem(i, filler);

            // Profile summary at slot 4
            List<String> summaryLore = List.of(
                    ChatColor.DARK_AQUA + "Afinidad: " + ChatColor.WHITE + profile.affinity().displayName(),
                    ChatColor.GOLD + "Rango: " + ChatColor.WHITE + profile.rank().displayName(),
                    ChatColor.LIGHT_PURPLE + "Esencia Espiritual: " + ChatColor.WHITE + profile.spirit(),
                    ChatColor.YELLOW + "Técnica Equipada: " + ChatColor.GREEN + (profile.equippedTechnique() != null ? profile.equippedTechnique().displayName() : "Ninguna")
            );
            inv.setItem(4, createItem(Material.NETHER_STAR, ChatColor.AQUA + "Tu Perfil Arcano", summaryLore));

            // Section Banners
            inv.setItem(9, createItem(Material.DRAGON_BREATH, ChatColor.GOLD + "" + ChatColor.BOLD + "Vía del Ki",
                    List.of(ChatColor.GRAY + "Técnicas de combate Saiyajin y energía pura.")));
            inv.setItem(18, createItem(Material.BLAZE_POWDER, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Vía del Chakra",
                    List.of(ChatColor.GRAY + "Jutsus ancestrales y armaduras de chakra.")));
            inv.setItem(27, createItem(Material.TURTLE_HELMET, ChatColor.DARK_RED + "" + ChatColor.BOLD + "Vía del Haki",
                    List.of(ChatColor.GRAY + "Poder espiritual y voluntad de la Gran Línea.")));

            // Dragon Ball slots
            inv.setItem(11, buildTechniqueItem(TranscendenceTechnique.GENKIDAMA, profile));
            inv.setItem(13, buildTechniqueItem(TranscendenceTechnique.ULTRA_INSTINCT, profile));
            inv.setItem(15, buildTechniqueItem(TranscendenceTechnique.KAIOKEN, profile));

            // Naruto slots
            inv.setItem(20, buildTechniqueItem(TranscendenceTechnique.RASENGAN, profile));
            inv.setItem(22, buildTechniqueItem(TranscendenceTechnique.CHIDORI, profile));
            inv.setItem(24, buildTechniqueItem(TranscendenceTechnique.SUSANOO, profile));

            // One Piece slots
            inv.setItem(29, buildTechniqueItem(TranscendenceTechnique.CONQUERORS_HAKI, profile));
            inv.setItem(31, buildTechniqueItem(TranscendenceTechnique.ARMAMENT_HAKI, profile));
            inv.setItem(33, buildTechniqueItem(TranscendenceTechnique.GEAR_SECOND, profile));

            // Controls
            inv.setItem(45, createItem(Material.BARRIER, ChatColor.RED + "Desequipar Técnica", List.of(ChatColor.GRAY + "Clic para retirar la técnica actual.")));
            inv.setItem(49, createItem(Material.BOOK, ChatColor.YELLOW + "Volver a la Guía", List.of(ChatColor.GRAY + "Abrir el menú principal de Arcana.")));
            inv.setItem(53, createItem(Material.OAK_DOOR, ChatColor.WHITE + "Cerrar", null));

            player.openInventory(inv);
        } catch (SQLException ex) {
            player.sendMessage(ChatColor.RED + "Error al cargar tu perfil de artes.");
            plugin.getLogger().warning("Error abriendo menú de artes: " + ex.getMessage());
        }
    }

    private ItemStack buildTechniqueItem(TranscendenceTechnique t, ArcanaProfile profile) {
        boolean unlocked = profile.experience() >= t.minRank().requiredExperience();
        boolean equipped = profile.equippedTechnique() == t;

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Disciplina: " + ChatColor.YELLOW + t.path().displayName());
        lore.add(ChatColor.AQUA + "Rango Mínimo: " + (unlocked ? ChatColor.GREEN : ChatColor.RED) + t.minRank().displayName());
        lore.add(ChatColor.LIGHT_PURPLE + "Costo de Esencia: " + ChatColor.WHITE + t.spiritCost());
        lore.add(ChatColor.DARK_AQUA + "Tiempo de Recarga: " + ChatColor.WHITE + t.cooldownSeconds() + "s");
        lore.add("");
        for (String desc : t.description()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', desc));
        }
        lore.add("");
        if (equipped) {
            lore.add(ChatColor.GREEN + "★ ¡Actualmente Equipada en tu Catalizador!");
        } else if (unlocked) {
            lore.add(ChatColor.YELLOW + "▶ Haz clic para equipar esta técnica.");
        } else {
            lore.add(ChatColor.RED + "✖ Bloqueado: requiere rango " + t.minRank().displayName());
        }

        String prefix = equipped ? ChatColor.GREEN + "★ " : (unlocked ? ChatColor.YELLOW + "✦ " : ChatColor.GRAY + "✖ ");
        return createItem(t.icon(), prefix + ChatColor.BOLD + t.displayName(), lore);
    }

    private static ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot == 53) { player.closeInventory(); return; }
        if (slot == 49) { player.performCommand("arcana"); return; }

        try {
            ArcanaProfile profile = repository.findOrAssign(player.getUniqueId());

            if (slot == 45) { // Unequip
                ArcanaProfile updated = new ArcanaProfile(
                        profile.playerId(), profile.affinity(), profile.origin(),
                        profile.experience(), profile.sigils(), profile.spirit(),
                        TranscendenceTechnique.NONE
                );
                repository.save(updated);
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0F, 1.0F);
                player.sendMessage(plugin.message("&7Has desequipado tu técnica trascendental."));
                open(player);
                return;
            }

            TranscendenceTechnique selected = switch (slot) {
                case 11 -> TranscendenceTechnique.GENKIDAMA;
                case 13 -> TranscendenceTechnique.ULTRA_INSTINCT;
                case 15 -> TranscendenceTechnique.KAIOKEN;
                case 20 -> TranscendenceTechnique.RASENGAN;
                case 22 -> TranscendenceTechnique.CHIDORI;
                case 24 -> TranscendenceTechnique.SUSANOO;
                case 29 -> TranscendenceTechnique.CONQUERORS_HAKI;
                case 31 -> TranscendenceTechnique.ARMAMENT_HAKI;
                case 33 -> TranscendenceTechnique.GEAR_SECOND;
                default -> null;
            };

            if (selected != null) {
                if (profile.experience() < selected.minRank().requiredExperience()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                    player.sendMessage(plugin.message("&cNo tienes el rango suficiente (" + selected.minRank().displayName() + ") para desbloquear esta técnica."));
                    return;
                }

                ArcanaProfile updated = new ArcanaProfile(
                        profile.playerId(), profile.affinity(), profile.origin(),
                        profile.experience(), profile.sigils(), profile.spirit(),
                        selected
                );
                repository.save(updated);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.5F);
                player.sendMessage(plugin.message("&a¡Has equipado &f" + selected.displayName() + "&a en tu Catalizador Arcano!"));
                open(player);
            }
        } catch (SQLException ex) {
            plugin.getLogger().warning("Error en click de menú de artes: " + ex.getMessage());
        }
    }
}
