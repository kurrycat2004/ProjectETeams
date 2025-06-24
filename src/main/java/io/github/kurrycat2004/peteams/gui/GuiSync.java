package io.github.kurrycat2004.peteams.gui;

import com.latmod.mods.projectex.gui.ContainerTableBase;
import com.latmod.mods.projectex.gui.GuiTableBase;
import io.github.kurrycat2004.peteams.compat.LoadedMods;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class GuiSync {
    public static void updateClientTransmutation() {
        if (!FMLCommonHandler.instance().getEffectiveSide().isClient()) return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;
        if (player.openContainer instanceof TransmutationContainer container) {
            container.transmutationInventory.updateClientTargets();
        }
        if (LoadedMods.PROJECT_EX) {
            if (player.openContainer instanceof ContainerTableBase container) {
                if (Minecraft.getMinecraft().currentScreen instanceof GuiTableBase gui) {
                    gui.updateValidItemList();
                } else {
                    container.knowledgeUpdate.updateKnowledge();
                }
            }
        }
    }
}
