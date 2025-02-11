package eu.xap3y.thepit.api.gui;

import eu.xap3y.thepit.ThePit;
import eu.xap3y.thepit.api.iface.VirtualMenuInterface;
import eu.xap3y.xagui.interfaces.GuiInterface;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class VirtualMenu<T> implements VirtualMenuInterface<T> {
    private final GuiInterface gui;

    public VirtualMenu(GuiInterface gui) {
        this.gui = gui;
    }

    public VirtualMenu(String name, int rows) {
        this.gui = ThePit.xagui.createMenu(name, rows);
    }

    public void open(@NotNull Player player) {
        getGui().open(player);
    }

    public void open(int page, @NotNull Player player) {
        getGui().open(page, player);
    }

    public @NotNull GuiInterface getGui() {
        return this.gui;
    }

    public @NotNull GuiInterface build() {
        return getGui();
    }

    @Override
    public abstract GuiInterface build(@NotNull T o);

    public void open(@NotNull Player player, @NotNull T o) {
        GuiInterface gui = build(o);
        gui.open(player);
    }
}

