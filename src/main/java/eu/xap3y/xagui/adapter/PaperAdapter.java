package eu.xap3y.xagui.adapter;

import eu.xap3y.xagui.GuiMenu;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PaperAdapter {

    public static void setName(ItemMeta meta, String name) {
        meta.displayName(ParseUtil.parseText(name));
    }

    public static void setLoreList(ItemMeta meta, List<String> lore) {
        meta.lore(lore.stream().map(ParseUtil::parseText).toList());
    }

    public static @NotNull Inventory createInventory(GuiMenu holder, int size, String title) {
        return Bukkit.createInventory(holder, size, ParseUtil.parseText(title));
    }

}
