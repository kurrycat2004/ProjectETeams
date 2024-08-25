package io.github.kurrycat2004.peteams.event;

import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;
import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.mixin.forge.AttachCapabilitiesEventMixin;
import io.github.kurrycat2004.peteams.provider.ClientKnowledgeProvider;
import io.github.kurrycat2004.peteams.provider.TeamKnowledgeProvider;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.impl.KnowledgeImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class Event {
    @SubscribeEvent
    public void onAttachCaps(AttachCapabilitiesEvent<Entity> event) {
        Object playerObj = event.getObject();
        if (!(playerObj instanceof EntityPlayer player)) return;

        Map<ResourceLocation, ICapabilityProvider> caps = ((AttachCapabilitiesEventMixin) event).getCaps();

        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            caps.put(KnowledgeImpl.Provider.NAME, new TeamKnowledgeProvider.Provider(player));
            PETeams.LOGGER.info("overwrote server-side knowledge capability provider");
        } else {
            ICapabilityProvider capabilityProvider = caps.get(KnowledgeImpl.Provider.NAME);
            IKnowledgeProvider oldProvider = capabilityProvider.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
            caps.put(KnowledgeImpl.Provider.NAME, new ClientKnowledgeProvider.Provider(oldProvider));
            PETeams.LOGGER.info("overwrote client-side knowledge capability provider");
        }
    }

    @SubscribeEvent
    public void onTeamPlayerJoined(ForgeTeamPlayerJoinedEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return;

        EntityPlayerMP player = event.getPlayer().getPlayer();
        IKnowledgeProvider capability = player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
        if (!(capability instanceof TeamKnowledgeProvider provider)) return;
        provider.syncKnowledgeWithTeam();
    }

    @SubscribeEvent
    public void onTeamPlayerLeft(ForgeTeamPlayerLeftEvent event) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return;

        EntityPlayerMP player = event.getPlayer().getPlayer();
        IKnowledgeProvider capability = player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
        if (!(capability instanceof TeamKnowledgeProvider provider)) return;
        provider.sendKnowledgeSyncSingle(player, true);
    }
}
