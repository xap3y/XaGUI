package eu.xap3y.xagui.interfaces;

import eu.xap3y.xagui.interfaces.listeners.*;
import eu.xap3y.xagui.models.GuiPageSwitchModel;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public interface GuiMenuInterface {
    void setOnOpen(GuiOpenInterface openAction);

    void setOnClose(GuiCloseInterface closeAction);

    void setOnClick(GuiClickInterface onClick);

    void setOnClickOwn(GuiOwnClickInterface onClick);

    void setOnPageSwitch(GuiPageSwitchInterface onPageSwitch);

    void setName(String newName);
    String getName();
    String getRawName();
    int getSize();
    int getPages();
    int getCurrentPageIndex();
    int getCurrentPage();

    void setSlot(int slot, GuiButtonInterface button);
    void setAllPageSlot(int slot, GuiButtonInterface button);
    void setAllPageSlot(int slot, ItemStack item);
    void setAllPageSlot(int slot, Material item);
    void setSlot(int slot, ItemStack item);
    void setSlot(int slot, Material item);
    void setSlot(int page, int slot, GuiButtonInterface button);
    void setSlot(int page, int slot, ItemStack button);
    void setSlot(int page, int slot, Material button);

    void updateSlot(int slot, ItemStack item);
    void updateSlot(int slot, Material item);
    void updateSlot(int page, int slot, ItemStack item);
    void updateSlot(int page, int slot, Material item);

    GuiButtonInterface getSlot(int slot);
    GuiButtonInterface getSlot(int page, int slot);

    void clearSlot(int slot);
    void clearSlot(int page, int slot);
    void clearAllSlots();
    void clearAllSlots(int page);

    JavaPlugin getOwner();

    void unlockButton(int slot);
    void unlockButton(int page, int slot);
    void lockButton(int slot);
    void lockButton(int page, int slot);
    boolean isButtonLocked(int slot);

    void open(Player player);
    void open(int page, Player player);
    void switchPage(int pageIndex, Player player);
    int getMaxPages();

    void stickSlot(int slot);
    void unStickSlot(int slot);
    void close(Player player);

    void fillSlots(ItemStack item, int... slots);
    void fillSlots(ItemStack item, Integer[] slots);
    void fillSlots(GuiButtonInterface item, int... slots);

    void fillSlots(int page, ItemStack item, int... slots);
    void fillSlots(int page, GuiButtonInterface item, int... slots);

    void addCloseButton();
    void addCloseButtonAllPages();
    void addCloseButton(int page, ItemStack button);
    void addCloseButton(int page, GuiButtonInterface button);

    void addPaginator();
    void setNextPageButton(ItemStack item);
    void setPreviousPageButton(ItemStack item);
    void setPageSwitchSound(Sound sound);

    void fillBorder();
    void fillBorder(int page, ItemStack item);
    void fillBorder(ItemStack item);
    void fillBorder(Material material);

    void setSelfInventoryAccess(boolean value);
    boolean getSelfInventoryAccess();

    void allowSelfInventoryClickTypes(ClickType... types);
    ClickType[] getAllowedSelfInventoryClickTypes();

    void allowClickTypes(ClickType... types);
    void blacklistClickTypes(ClickType... types);
    ClickType[] getAllowedClickTypes();
    ClickType[] getBlacklistedClickTypes();

    void callback();
    void setCallback(Runnable callback);

    void setTotalPages(int pages);
}
