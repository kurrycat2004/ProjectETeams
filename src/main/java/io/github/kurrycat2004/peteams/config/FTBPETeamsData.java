package io.github.kurrycat2004.peteams.config;

import com.feed_the_beast.ftblib.events.team.ForgeTeamConfigEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamCreatedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDataEvent;
import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.TeamData;
import io.github.kurrycat2004.peteams.Tags;
import io.github.kurrycat2004.peteams.data.Team;
import io.github.kurrycat2004.peteams.data.TeamSavedData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class FTBPETeamsData extends TeamData {
    public FTBPETeamsData(ForgeTeam t) {
        super(t);
    }

    public static @NotNull FTBPETeamsData getData(@NotNull ForgeTeam team) {
        return team.getData().get(Tags.MODID);
    }

    @SubscribeEvent
    public static void onRegisterTeamData(@NotNull ForgeTeamDataEvent event) {
        event.register(new FTBPETeamsData(event.getTeam()));
    }

    @SubscribeEvent
    public static void onTeamConfig(@NotNull ForgeTeamConfigEvent event) {
        getData(event.getTeam()).addConfig(event.getConfig());
    }

    // For whatever reason, the default value on ConfigGroup.addBool is purely decorative for the tooltip and doesn't actually set the value
    @SubscribeEvent
    public static void onTeamCreated(@NotNull ForgeTeamCreatedEvent event) {
        ForgeTeam team = event.getTeam();
        getData(team).setShareEmc(PETeamsConfig.DEFAULT_BEHAVIOUR.defaultShareEmc);
        getData(team).setShareKnowledge(PETeamsConfig.DEFAULT_BEHAVIOUR.defaultShareKnowledge);
    }

    private boolean shareEmc = false;
    private boolean shareKnowledge = false;

    private void addConfig(@NotNull ConfigGroup main) {
        ConfigGroup group = main.getGroup(Tags.MODID);
        group.setDisplayName(new TextComponentString(Tags.MODNAME));

        group.addBool("share_emc", () -> shareEmc,
                this::setShareEmc, PETeamsConfig.DEFAULT_BEHAVIOUR.defaultShareEmc);
        group.addBool("share_knowledge", () -> shareKnowledge,
                this::setShareKnowledge, PETeamsConfig.DEFAULT_BEHAVIOUR.defaultShareKnowledge);
    }

    private @NotNull Team getTeam() {
        return TeamSavedData.getTeam(this.team.getUID());
    }

    private void setShareEmc(boolean shareEmc) {
        boolean shouldSync = this.shareEmc != shareEmc;
        this.shareEmc = shareEmc;
        if (shouldSync) getTeam().pushEmcSyncAll();
    }

    private void setShareKnowledge(boolean shareKnowledge) {
        boolean shouldSync = this.shareKnowledge != shareKnowledge;
        this.shareKnowledge = shareKnowledge;
        if (shouldSync) getTeam().shareKnowledgeChangedSync();
    }

    @Override
    public @NotNull String getId() {
        return Tags.MODID;
    }

    @Override
    public @NotNull NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("ShareEMC", shareEmc);
        nbt.setBoolean("ShareKnowledge", shareKnowledge);
        return nbt;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound nbt) {
        shareEmc = nbt.getBoolean("ShareEMC");
        shareKnowledge = nbt.getBoolean("ShareKnowledge");
    }

    public boolean isShareEmc() {
        return shareEmc;
    }

    public boolean isShareKnowledge() {
        return shareKnowledge;
    }
}
