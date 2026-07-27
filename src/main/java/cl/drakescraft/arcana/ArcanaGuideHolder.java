package cl.drakescraft.arcana;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Marker holder so every Arcana guide click is safely isolated from player inventories. */
final class ArcanaGuideHolder implements InventoryHolder {
    @Override public Inventory getInventory() { return null; }
}
