package eu.xap3y.xagui;

import eu.xap3y.xagui.commands.XaGuiCommandInjector;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class XaGuiPlugin extends JavaPlugin {

    @Getter
    private static XaGui xaGui;

    @Override
    public void onEnable() {
        getLogger().info("XaGui v" + XaGui.getVERSION() + " Standalone Library loaded!");

        xaGui = new XaGui(this);

        XaGuiCommandInjector.injectCommand();
    }

    public static void registerAsXaGui(XaGui p0) {
        xaGui = p0;
    }
}