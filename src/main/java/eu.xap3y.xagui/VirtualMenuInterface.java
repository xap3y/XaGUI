package eu.xap3y.thepit.api.iface;

import eu.xap3y.xagui.interfaces.GuiInterface;
import org.jetbrains.annotations.NotNull;

public interface VirtualMenuInterface<T> {

    GuiInterface build(@NotNull T o);
}
