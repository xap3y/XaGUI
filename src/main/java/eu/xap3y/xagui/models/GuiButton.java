package eu.xap3y.xagui.models;

import eu.xap3y.xagui.XaGui;
import eu.xap3y.xagui.adapter.PaperAdapter;
import eu.xap3y.xagui.interfaces.GuiButtonInterface;
import eu.xap3y.xagui.interfaces.listeners.GuiClickInterface;
import eu.xap3y.xagui.interfaces.GuiMenuInterface;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Represents a button in a GUI.
 * <p>
 * A GuiButton wraps an ItemStack and optional click behavior. It supports:
 * <ul>
 *   <li>Setting item metadata (name, lore, amount, flags)</li>
 *   <li>Adding/removing enchantments</li>
 *   <li>A click listener</li>
 *   <li>An optional redirect to another menu after click</li>
 *   <li>An optional click sound</li>
 * </ul>
 *
 * All setter-like methods return {@code this} for fluent chaining.
 */
@SuppressWarnings("deprecation")
public class GuiButton implements GuiButtonInterface {

    /**
     * The current icon used by this button.
     */
    private ItemStack icon;

    /**
     * Optional sound to play when the button is clicked.
     */
    private Sound sound;

    /**
     * Volume for the click sound.
     */
    private float soundVolume = 1f;

    /**
     * Optional click listener invoked when the button is clicked.
     */
    private GuiClickInterface listener;

    /**
     * Optional redirect supplier returning a target menu to open after click.
     */
    private Supplier<GuiMenuInterface> redirectMenu;

    /**
     * Create a new GuiButton from an ItemStack.
     *
     * @param item the backing ItemStack
     */
    public GuiButton(ItemStack item) {
        this.icon = item;
        if (XaGui.getButtonClickSound() != null) {
            this.sound = XaGui.getButtonClickSound();
            this.soundVolume = XaGui.getButtonClickSoundVolume();
        }
    }

    /**
     * Create a new GuiButton with a Material (amount 1).
     *
     * @param material the material to use
     */
    public GuiButton(Material material) {
        this(new ItemStack(material));
    }

    /**
     * Get the material type of this button's icon.
     *
     * @return the Material of the icon
     */
    @Override
    public Material getIcon() {
        return icon.getType();
    }

    /**
     * Get this button's ItemStack.
     *
     * @return the ItemStack reference
     */
    @Override
    public ItemStack getItem() {
        return icon;
    }

    /**
     * Replace this button's ItemStack.
     *
     * @param item the new ItemStack
     */
    @Override
    public void setItem(ItemStack item) {
        this.icon = item;
    }

    /**
     * Set the click listener for this button.
     *
     * @param newListener the listener to invoke on click
     * @return this button for chaining
     */
    @Override
    public GuiButton withListener(GuiClickInterface newListener) {
        this.listener = newListener;
        return this;
    }

    /**
     * Set the display name of the button, supporting color codes with {@literal &}.
     *
     * @param name the new display name
     * @return this button for chaining
     */
    @Override
    public GuiButton setName(String name) {
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            if (XaGui.isUseKyoriText()) {
                PaperAdapter.setName(meta, name);
            } else {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            }
            icon.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Set the lore lines of the button, translating color codes with {@literal &}.
     *
     * @param newLore list of lore lines
     * @return this button for chaining
     */
    @Override
    public GuiButton setLoreList(List<String> newLore) {
        List<String> colored = new ArrayList<>(newLore.size());
        for (String s : newLore) {
            colored.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            if (XaGui.isUseKyoriText()) {
                PaperAdapter.setLoreList(meta, newLore);
            } else {
                meta.setLore(colored);
            }
            icon.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Set the lore lines from varargs, translating color codes with {@literal &}.
     *
     * @param args lore lines
     * @return this button for chaining
     */
    @Override
    public GuiButton setLore(String... args) {
        List<String> list = new ArrayList<>(args.length);
        list.addAll(Arrays.asList(args));
        return setLoreList(list);
    }

    /**
     * Set the lore lines from an array, translating color codes with {@literal &}.
     *
     * @param array lore lines
     * @return this button for chaining
     */
    @Override
    public GuiButton setLoreArray(String[] array) {
        List<String> list = new ArrayList<>(array.length);
        list.addAll(Arrays.asList(array));
        return setLoreList(list);
    }

    /**
     * Append a single lore line, translating color codes with {@literal &}.
     *
     * @param line the lore line to add
     * @return this button for chaining
     */
    @Override
    public GuiButton addLoreLine(String line) {
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Append multiple lore lines, translating color codes with {@literal &}.
     *
     * @param lines list of lore lines to add
     * @return this button for chaining
     */
    @Override
    public GuiButton addLoreList(List<String> lines) {
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            for (String s : lines) {
                lore.add(ChatColor.translateAlternateColorCodes('&', s));
            }
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Append multiple lore lines from varargs, translating color codes with {@literal &}.
     *
     * @param args lore lines
     * @return this button for chaining
     */
    @Override
    public GuiButton addLore(String... args) {
        List<String> list = new ArrayList<>(args.length);
        list.addAll(Arrays.asList(args));
        return addLoreList(list);
    }

    /**
     * Append multiple lore lines from an array, translating color codes with {@literal &}.
     *
     * @param array lore lines
     * @return this button for chaining
     */
    @Override
    public GuiButton addLoreArray(String[] array) {
        List<String> list = new ArrayList<>(array.length);
        list.addAll(Arrays.asList(array));
        return addLoreList(list);
    }

    /**
     * Clear all lore from the item.
     *
     * @return this button for chaining
     */
    @Override
    public GuiButton clearLore() {
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setLore(null);
            icon.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Add an item flag to the icon.
     *
     * @param flag the item flag to add
     * @return this button for chaining
     */
    @Override
    public GuiButton addItemFlag(ItemFlag flag) {
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flag);
            icon.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Remove an item flag from the icon.
     *
     * @param flag the item flag to remove
     * @return this button for chaining
     */
    @Override
    public GuiButton removeItemFlag(ItemFlag flag) {
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.removeItemFlags(flag);
            icon.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Set the stack amount of the icon.
     *
     * @param amount new amount
     * @return this button for chaining
     */
    @Override
    public GuiButton setAmount(int amount) {
        icon.setAmount(amount);
        return this;
    }

    /**
     * Add an unsafe enchantment at level 1.
     *
     * @param enchantment the enchantment to add
     * @return this button for chaining
     */
    @Override
    public GuiButton addEnchantment(Enchantment enchantment) {
        icon.addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    /**
     * Add an unsafe enchantment with a custom level.
     *
     * @param enchantment the enchantment to add
     * @param level       the level to apply
     * @return this button for chaining
     */
    @Override
    public GuiButton addEnchantment(Enchantment enchantment, int level) {
        icon.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    /**
     * Remove a specific enchantment from the icon.
     *
     * @param enchantment the enchantment to remove
     * @return this button for chaining
     */
    @Override
    public GuiButton removeEnchantment(Enchantment enchantment) {
        icon.removeEnchantment(enchantment);
        return this;
    }

    /**
     * Remove all enchantments from the icon.
     *
     * @return this button for chaining
     */
    @Override
    public GuiButton removeAllEnchantments() {
        Map<Enchantment, Integer> enchants = icon.getEnchantments();
        if (!enchants.isEmpty()) {
            for (Enchantment e : new ArrayList<>(enchants.keySet())) {
                icon.removeEnchantment(e);
            }
        }
        return this;
    }

    /**
     * Set the durability (damage) value of the icon.
     * Note: durability is deprecated for modern versions in favor of Damageable meta.
     *
     * @param durability the durability value
     * @return this button for chaining
     */
    @Override
    public GuiButton setDurability(short durability) {
        icon.setDurability(durability);
        return this;
    }

    /**
     * Get the configured click listener for this button, if any.
     *
     * @return the listener or null
     */
    @Override
    public GuiClickInterface getClickListener() {
        return listener;
    }

    /**
     * Get the sound configured to play on click, if any.
     *
     * @return the click sound or null
     */
    @Override
    public Sound getClickSound() {
        return sound;
    }

    /**
     * Get the volume configured for the click sound.
     *
     * @return the click sound volume
     */
    @Override
    public float getClickSoundVolume() {
        return soundVolume;
    }

    /**
     * Create a shallow copy of this button.
     * <p>
     * The copy contains a cloned ItemStack and copies over listener, redirect, and sound references.
     *
     * @return a cloned GuiButton
     */
    @Override
    public GuiButton clone() {
        GuiButton copy = new GuiButton(this.icon.clone());
        copy.listener = this.listener;
        copy.redirectMenu = this.redirectMenu;
        copy.sound = this.sound;
        return copy;
    }

    /**
     * Configure a redirect menu supplier to be opened after this button is clicked.
     *
     * @param menu supplier that returns a target GuiMenuInterface
     * @return this button for chaining
     */
    @Override
    public GuiButton withRedirect(Supplier<GuiMenuInterface> menu) {
        this.redirectMenu = menu;
        return this;
    }

    /**
     * Configure a sound to play when this button is clicked. Use null to disable.
     *
     * @param sound the sound to play, or null
     * @param volume the volume to play the sound at
     * @return this button for chaining
     */
    @Override
    public GuiButton withClickSound(Sound sound, float volume) {
        this.sound = sound;
        this.soundVolume = volume;
        return this;
    }

    /**
     * If a redirect is configured, open that menu for the given player.
     * Also plays the global redirect sound if configured via XaGui.
     *
     * @param p the player to redirect
     */
    @Override
    public void callRedirect(Player p) {
        if (redirectMenu != null) {
            GuiMenuInterface menu = redirectMenu.get();
            if (menu != null) {
                menu.open(p);
            }
            if (XaGui.getRedirectSound() != null) {
                p.playSound(p, XaGui.getRedirectSound(), 1f, 1f);
            }
        }
    }
}