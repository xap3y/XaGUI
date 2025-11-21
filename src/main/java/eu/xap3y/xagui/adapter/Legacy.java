package eu.xap3y.xagui.adapter;

import eu.xap3y.xagui.models.GuiButton;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Legacy {

    public static ItemStack createBorderFiller() {
        // Try modern 1.13+ material by name
        Material modern = Material.matchMaterial("GRAY_STAINED_GLASS_PANE");
        if (modern != null) {
            return new GuiButton(modern).setName("&r").getItem();
        }

        // Fallback for 1.12.2/1.8.8: STAINED_GLASS_PANE with gray data value (7)
        Material legacy = Material.matchMaterial("STAINED_GLASS_PANE");
        if (legacy == null) {
            // Extremely old/odd environment safety
            legacy = Material.GLASS_PANE;
        }

        // If GuiButton(Material) doesn't let you set data/color,
        // create and color the stack first, then set the name yourself.
        ItemStack pane = new ItemStack(legacy, 1, (short) 7); // 7 = gray
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RESET.toString()); // "&r"
            pane.setItemMeta(meta);
        }
        return pane;
    }
}
