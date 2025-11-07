package eu.xap3y.xagui.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;

@Data
@AllArgsConstructor
public class GuiPageSwitchModel {

    private final Player player;
    private final int targetPage;
    private final int oldPage;

}
