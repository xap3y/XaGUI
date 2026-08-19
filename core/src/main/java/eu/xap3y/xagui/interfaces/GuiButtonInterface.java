package eu.xap3y.xagui.interfaces;

import eu.xap3y.xagui.interfaces.listeners.GuiClickInterface;
import eu.xap3y.xagui.models.GuiButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public interface GuiButtonInterface {

    Material getIcon();
    ItemStack getItem();
    void setItem(ItemStack item);

    GuiButton withListener(GuiClickInterface newListener);

    GuiButton setName(String name);
    GuiButton setLoreList(List<String> newLore);
    GuiButton setLore(String... args);
    GuiButton setLoreArray(String[] array);
    GuiButton addLoreLine(String line);
    GuiButton addLoreList(List<String> lines);
    GuiButton addLore(String... args);
    GuiButton addLoreArray(String[] array);
    GuiButton clearLore();

    GuiButton addItemFlag(ItemFlag flag);
    GuiButton removeItemFlag(ItemFlag flag);
    GuiButton setAmount(int amount);

    GuiButton addEnchantment(Enchantment enchantment);
    GuiButton addEnchantment(Enchantment enchantment, int level);
    GuiButton removeEnchantment(Enchantment enchantment);
    GuiButton removeAllEnchantments();

    GuiButton setDurability(short durability);

    GuiClickInterface getClickListener();
    Sound getClickSound();
    float getClickSoundVolume();
    GuiButton clone();

    GuiButton withRedirect(Supplier<GuiMenuInterface> menu);
    GuiButton withClickSound(Sound sound, float volume);
    default GuiButton withClickSound(Sound sound) {
        return withClickSound(sound, 1.0f);
    }

    void callRedirect(Player p);
}
