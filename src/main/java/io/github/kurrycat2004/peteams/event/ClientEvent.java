package io.github.kurrycat2004.peteams.event;

import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;
import io.github.kurrycat2004.peteams.data.Team;
import io.github.kurrycat2004.peteams.provider.TeamKnowledgeProvider;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientEvent {
    @SubscribeEvent
    public void onTeamPlayerJoined(ForgeTeamPlayerJoinedEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) return;
        EntityPlayer localPlayer = Minecraft.getMinecraft().player;
        EntityPlayer eventPlayer = event.getPlayer().getPlayer();
        // Only run event handler for onTeamPlayerJoined(localPlayer)
        if (localPlayer == null || !eventPlayer.getUniqueID().equals(localPlayer.getUniqueID())) return;


        /*EntityPlayerMP player = event.getPlayer().getPlayer();
        IKnowledgeProvider capability = player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
        if (!(capability instanceof TeamKnowledgeProvider provider)) return;
        provider.sendKnowledgeSyncTeam(player);
        Team team = TeamKnowledgeProvider.getTeam(player);
        if (team != null) team.update();*/
    }

    @SubscribeEvent
    public void onTeamPlayerLeft(ForgeTeamPlayerLeftEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) return;

        /*EntityPlayerMP player = event.getPlayer().getPlayer();
        IKnowledgeProvider capability = player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
        if (!(capability instanceof TeamKnowledgeProvider provider)) return;
        provider.sendKnowledgeSyncSingle(player);*/
    }
}
