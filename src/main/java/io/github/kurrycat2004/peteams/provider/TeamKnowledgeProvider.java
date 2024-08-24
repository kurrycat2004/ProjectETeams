package io.github.kurrycat2004.peteams.provider;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.data.Team;
import io.github.kurrycat2004.peteams.data.TeamKnowledgeData;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.KnowledgeSyncPKT;
import moze_intel.projecte.playerData.Transmutation;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


//TODO: split this into two classes, one for single, one for team
public class TeamKnowledgeProvider implements IKnowledgeProvider {
    private final EntityPlayer player;
    private final List<ItemStack> knowledge = new ArrayList<>();
    private final List<ItemStack> view = Collections.unmodifiableList(knowledge);
    private final IItemHandlerModifiable inputLocks = new ItemStackHandler(9);
    private long emc = 0;
    private boolean fullKnowledge = false;

    private @Nullable Team getTeam() {
        return getTeam(this.player);
    }

    public static @Nullable Team getTeam(EntityPlayer player) {
        ForgePlayer forgePlayer = getPlayer(player);
        if (forgePlayer == null) return null;
        if (forgePlayer.team == null || !forgePlayer.hasTeam()) return null;

        return TeamKnowledgeData.getInstance().getTeam(forgePlayer.team.getUIDCode());
    }

    public static @Nullable ForgePlayer getPlayer(EntityPlayer player) {
        if (!Universe.loaded()) return null;
        ForgePlayer forgePlayer = Universe.get().getPlayer(player.getUniqueID());
        if (forgePlayer == null) {
            PETeams.LOGGER.warn("Failed to get ForgePlayer for player {}", player.getName());
        }
        return forgePlayer;
    }

    private @NotNull UUID getUUID() {
        return this.player.getUniqueID();
    }

    private TeamKnowledgeProvider(EntityPlayer player) {
        this.player = player;
    }

    private void fireChangedEvent() {
        if (this.player == null || this.player.world.isRemote) return;
        MinecraftForge.EVENT_BUS.post(new PlayerKnowledgeChangeEvent(this.player));
    }

    @Override
    public boolean hasFullKnowledge() {
        Team team = this.getTeam();
        if (team == null) return this.fullKnowledge;
        return team.hasFullKnowledge();
    }

    @Override
    public void setFullKnowledge(boolean fullKnowledge) {
        if (this.hasFullKnowledge() == fullKnowledge) return;

        this.fullKnowledge = fullKnowledge;
        Team team = this.getTeam();
        if (team != null) team.setFullKnowledge(getUUID(), fullKnowledge);
        fireChangedEvent();
    }

    @Override
    public void clearKnowledge() {
        PETeams.debugLog("Clearing knowledge for {}", this.player.getName());
        Team team = this.getTeam();
        knowledge.clear();
        fullKnowledge = false;
        if (team != null) {
            PETeams.debugLog("Clearing knowledge for team {}", team.getUuid());
            team.clearKnowledge(getUUID());
            team.setFullKnowledge(getUUID(), false);
        }
        fireChangedEvent();
    }

    private List<ItemStack> getKnowledgeView() {
        Team team = this.getTeam();
        if (team == null) return this.view;
        return team.getKnowledge();
    }

    @Override
    public boolean hasKnowledge(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (hasFullKnowledge()) return true;

        for (ItemStack s : this.getKnowledgeView()) {
            if (ItemHelper.basicAreStacksEqual(s, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addKnowledge(@NotNull ItemStack stack) {
        PETeams.debugLog("Adding knowledge {} for {} ({})", stack, this.player.getName(), this.player.getUniqueID());
        if (this.hasFullKnowledge()) return false;
        boolean knowsItem = this.hasKnowledge(stack);
        boolean isTome = stack.getItem() == ObjHandler.tome;
        if (knowsItem && isTome) return false;

        Team team = this.getTeam();

        if (!knowsItem) {
            this.knowledge.add(stack.copy());
            if (team != null) team.addKnowledge(getUUID(), stack);
        }

        if (isTome) {
            this.fullKnowledge = true;
            if (team != null) team.setFullKnowledge(getUUID(), true);
        }

        this.fireChangedEvent();
        return true;
    }

    @Override
    public boolean removeKnowledge(@NotNull ItemStack stack) {
        Team team = this.getTeam();
        boolean result = false;
        if (stack.getItem() == ObjHandler.tome) {
            this.fullKnowledge = false;
            if (team != null) team.setFullKnowledge(getUUID(), false);
            result = true;
        }

        if (this.hasFullKnowledge()) return false;

        boolean removed = this.knowledge.removeIf(s -> ItemHelper.basicAreStacksEqual(stack, s));
        if (team != null) removed = team.removeKnowledge(getUUID(), stack);

        result |= removed;

        if (result) this.fireChangedEvent();
        return result;
    }

    @NotNull
    @Override
    public List<ItemStack> getKnowledge() {
        if (this.hasFullKnowledge()) return Transmutation.getCachedTomeKnowledge();

        Team team = this.getTeam();
        if (team == null) return this.view;
        return team.getKnowledge();
    }

    @NotNull
    @Override
    public IItemHandler getInputAndLocks() {
        return inputLocks;
    }

    @Override
    public long getEmc() {
        Team team = this.getTeam();
        if (team == null) return this.emc;
        return team.getEmc();
    }

    @Override
    public void setEmc(long l) {
        Team team = this.getTeam();
        // prevent emc duping by only setting one of team/personal emc
        if (team != null) team.setEmc(getUUID(), l);
        else this.emc = l;
    }

    @Override
    public void sync(@NotNull EntityPlayerMP player) {
        Team team = this.getTeam();
        if (team == null) this.sendKnowledgeSyncSingle(player, false);
        else this.sendKnowledgeSyncTeam(team, player);
    }

    public void sendKnowledgeSyncSingle(@NotNull EntityPlayerMP player, boolean resetCache) {
        PETeams.debugLog("Sending single knowledge to single player {} ({})", player.getName(), player.getUniqueID());
        NBTTagCompound nbt = this.serializeNBTSingle(resetCache);
        PETeams.debugLog("NBT: {}", nbt);
        KnowledgeSyncPKT packet = new KnowledgeSyncPKT(nbt);
        PacketHandler.sendTo(packet, player);
    }

    public void knowledgeSyncTeam(@NotNull Team team) {
        PETeams.debugLog("Syncing single knowledge of {} ({}) with team {}", this.player.getName(), this.player.getUniqueID(), team.getUuid());
        for (ItemStack stack : team.getKnowledge()) {
            if (!stack.isEmpty()) this.knowledge.add(stack);
        }
        pruneStaleKnowledge(knowledge);
        pruneDuplicateKnowledge(knowledge);
        if (team.hasFullKnowledge()) this.fullKnowledge = true;

        this.fireChangedEvent();
    }

    public void sendKnowledgeSyncTeam(@NotNull Team team, EntityPlayerMP player) {
        PETeams.debugLog("Sending knowledge from team {} to {} ({})", team.getUuid(), player.getName(), player.getUniqueID());
        KnowledgeSyncPKT packet = new KnowledgeSyncPKT(this.serializeNBTTeam(team));
        PacketHandler.sendTo(packet, player);
    }

    public void syncKnowledgeWithTeam() {
        Team team = this.getTeam();
        if (team == null) return;

        PETeams.debugLog("Syncing knowledge of {} ({}) with team {}", this.player.getName(), this.player.getUniqueID(), team.getUuid());
        for (ItemStack stack : this.knowledge) {
            if (!stack.isEmpty()) team.addKnowledgeRaw(stack);
        }
        team.pruneStaleKnowledge();
        team.pruneDuplicateKnowledge();
        if (this.fullKnowledge) team.setFullKnowledgeRaw(true);

        team.update(null);
    }

    public NBTTagCompound serializeNBTTeam(@NotNull Team team) {
        NBTTagCompound result = serializeHelper(team.getEmc(), team.getKnowledge(), this.inputLocks, team.hasFullKnowledge());
        result.setBoolean("resetCache", true);
        return result;
    }

    public NBTTagCompound serializeNBTSingle(boolean resetCache) {
        NBTTagCompound result = serializeHelper(this.emc, this.knowledge, this.inputLocks, this.fullKnowledge);
        result.setBoolean("resetCache", resetCache);
        return result;
    }

    private @NotNull NBTTagCompound serializeHelper(long emc, @NotNull List<ItemStack> knowledge, IItemHandlerModifiable inputLocks, boolean fullKnowledge) {
        NBTTagCompound properties = new NBTTagCompound();
        properties.setLong("transmutationEmc", emc);

        NBTTagList knowledgeWrite = new NBTTagList();
        for (ItemStack i : knowledge) {
            NBTTagCompound tag = i.writeToNBT(new NBTTagCompound());
            knowledgeWrite.appendTag(tag);
        }

        properties.setTag("knowledge", knowledgeWrite);
        //noinspection DataFlowIssue
        properties.setTag("inputlock", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(inputLocks, null));
        properties.setBoolean("fullknowledge", fullKnowledge);
        return properties;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return serializeHelper(this.emc, this.knowledge, this.inputLocks, this.fullKnowledge);
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound properties) {
        PETeams.debugLog("Deserialize team knowledge: {}", properties);
        this.emc = properties.getLong("transmutationEmc");

        NBTTagList list = properties.getTagList("knowledge", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack item = new ItemStack(list.getCompoundTagAt(i));
            if (!item.isEmpty()) knowledge.add(item);
        }

        pruneStaleKnowledge(knowledge);
        pruneDuplicateKnowledge(knowledge);

        for (int i = 0; i < inputLocks.getSlots(); i++) {
            inputLocks.setStackInSlot(i, ItemStack.EMPTY);
        }

        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(inputLocks, null, properties.getTagList("inputlock", Constants.NBT.TAG_COMPOUND));
        this.fullKnowledge = properties.getBoolean("fullknowledge");
    }

    public static void pruneDuplicateKnowledge(List<ItemStack> knowledge) {
        ItemHelper.removeEmptyTags(knowledge);
        ItemHelper.compactItemListNoStacksize(knowledge);
        for (ItemStack s : knowledge) {
            if (s.getCount() > 1) s.setCount(1);
        }
    }

    public static void pruneStaleKnowledge(@NotNull List<ItemStack> knowledge) {
        knowledge.removeIf(stack -> !EMCHelper.doesItemHaveEmc(stack));
    }

    public static class Provider implements ICapabilitySerializable<NBTTagCompound> {
        private final TeamKnowledgeProvider knowledge;

        public Provider(EntityPlayer player) {
            this.knowledge = new TeamKnowledgeProvider(player);
        }

        public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
            return capability == ProjectEAPI.KNOWLEDGE_CAPABILITY;
        }

        public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
            return capability == ProjectEAPI.KNOWLEDGE_CAPABILITY ? ProjectEAPI.KNOWLEDGE_CAPABILITY.cast(this.knowledge) : null;
        }

        public NBTTagCompound serializeNBT() {
            return this.knowledge.serializeNBT();
        }

        public void deserializeNBT(NBTTagCompound nbt) {
            this.knowledge.deserializeNBT(nbt);
        }
    }
}
