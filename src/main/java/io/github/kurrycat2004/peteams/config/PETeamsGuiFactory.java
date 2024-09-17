package io.github.kurrycat2004.peteams.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

public class PETeamsGuiFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) {}

    @Override
    public boolean hasConfigGui() {
        return Minecraft.getMinecraft().getIntegratedServer() != null;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new PETeamsConfigGui(parentScreen);
    }
}
