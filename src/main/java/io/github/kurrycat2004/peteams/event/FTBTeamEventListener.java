package io.github.kurrycat2004.peteams.event;

import com.feed_the_beast.ftblib.events.team.ForgeTeamCreatedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;
import io.github.kurrycat2004.peteams.Tags;
import io.github.kurrycat2004.peteams.provider.providers.SplitKnowledgeProvider;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class FTBTeamEventListener {
    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public static void onTeamPlayerJoined(ForgeTeamPlayerJoinedEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return;

        EntityPlayerMP player = event.getPlayer().getPlayer();
        IKnowledgeProvider capability = player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
        if (!(capability instanceof SplitKnowledgeProvider provider)) return;
        provider.syncMemberJoin();
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public static void onTeamPlayerLeft(ForgeTeamPlayerLeftEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return;

        EntityPlayerMP player = event.getPlayer().getPlayer();
        IKnowledgeProvider capability = player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
        if (!(capability instanceof SplitKnowledgeProvider provider)) return;
        provider.syncMemberLeft(player);
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public static void onTeamCreated(ForgeTeamCreatedEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return;

        // This should always be a list with only the owner but whatever
        for (EntityPlayer player : event.getTeam().getOnlineMembers()) {
            IKnowledgeProvider capability = player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
            if (!(capability instanceof SplitKnowledgeProvider provider)) continue;
            provider.syncMemberJoin();
        }
    }
}
