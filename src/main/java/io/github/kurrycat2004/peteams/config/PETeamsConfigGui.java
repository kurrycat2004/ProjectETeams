package io.github.kurrycat2004.peteams.config;

import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.Tags;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.config.GuiConfig;

public class PETeamsConfigGui extends GuiConfig {
    public PETeamsConfigGui(GuiScreen parentScreen) {
        super(parentScreen, Tags.MODID, false, false,
                GuiConfig.getAbridgedConfigPath(PETeams.configFile.getAbsolutePath()),
                ConfigManager.getModConfigClasses(Tags.MODID)
        );
    }
}