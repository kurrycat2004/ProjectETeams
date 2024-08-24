package io.github.kurrycat2004.peteams;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.KnowledgeSyncPKT;
import moze_intel.projecte.playerData.Transmutation;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.fml.common.FMLCommonHandler;
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


//TODO: split this into two classes, one for single, one for team
public class TeamKnowledgeProvider implements IKnowledgeProvider {
    private final EntityPlayer player;
    private final List<ItemStack> knowledge = new ArrayList<>();
    private final List<ItemStack> view = Collections.unmodifiableList(knowledge);
    private final IItemHandlerModifiable inputLocks = new ItemStackHandler(9);
    private long emc = 0;
    private boolean fullKnowledge = false;

    @Nullable
    private Team getTeam() {
        return getTeam(this.player);
    }

    public static Team getTeam(EntityPlayer player) {
        ForgePlayer forgePlayer = getPlayer(player);
        if (forgePlayer == null) return null;
        if (forgePlayer.team == null || !forgePlayer.hasTeam()) return null;

        return TeamKnowledgeData.getInstance().getTeam(forgePlayer.team.getUIDCode());
    }

    public static ForgePlayer getPlayer(EntityPlayer player) {
        ForgePlayer forgePlayer = Universe.get().getPlayer(player.getUniqueID());
        if (forgePlayer == null) {
            PETeams.LOGGER.warn("Failed to get ForgePlayer for player {}", player.getName());
        }
        return forgePlayer;
    }

    @Nullable
    private ForgePlayer getPlayer() {
        return getPlayer(this.player);
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
        if (this.fullKnowledge == fullKnowledge) return;

        this.fullKnowledge = fullKnowledge;
        Team team = this.getTeam();
        if (team != null && team.hasFullKnowledge() != fullKnowledge) {
            team.setFullKnowledge(fullKnowledge);
        }
        fireChangedEvent();
    }

    @Override
    public void clearKnowledge() {
        Team team = this.getTeam();
        knowledge.clear();
        fullKnowledge = false;
        if (team != null) {
            team.clearKnowledge();
            team.setFullKnowledge(false);
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
        if (this.hasFullKnowledge()) return false;
        boolean knowsItem = this.hasKnowledge(stack);
        boolean isTome = stack.getItem() == ObjHandler.tome;
        if (knowsItem && isTome) return false;

        Team team = this.getTeam();

        if (!knowsItem) {
            this.knowledge.add(stack);
            if (team != null) team.addKnowledge(stack);
        }

        if (isTome) {
            this.fullKnowledge = true;
            if (team != null) team.setFullKnowledge(true);
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
            if (team != null) team.setFullKnowledge(false);
            result = true;
        }

        if (this.hasFullKnowledge()) return false;

        boolean removed = this.knowledge.removeIf(s -> ItemHelper.basicAreStacksEqual(stack, s));
        if (team != null) removed = team.removeKnowledge(stack);

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
        if (team != null) team.setEmc(l);
        else this.emc = l;
    }

    @Override
    public void sync(@NotNull EntityPlayerMP player) {
        Team team = this.getTeam();
        if (team == null) this.sendKnowledgeSyncSingle(player);
        else this.sendKnowledgeSyncTeam(player);
    }

    public void sendKnowledgeSyncSingle(EntityPlayerMP player) {
        PETeams.LOGGER.debug("Syncing knowledge for single player {} ({})", player.getName(), player.getUniqueID());
        KnowledgeSyncPKT packet = new KnowledgeSyncPKT(this.serializeNBTSingle());
        PacketHandler.sendTo(packet, player);
    }

    public void sendKnowledgeSyncTeam(EntityPlayerMP player) {
        Team team = this.getTeam();
        if (team == null) return;
        PETeams.LOGGER.debug("Syncing knowledge for team {}", team.getUuid());
        KnowledgeSyncPKT packet = new KnowledgeSyncPKT(this.serializeNBTTeam(team));
        PacketHandler.sendTo(packet, player);
        /*team.getTeam().getOnlineMembers().forEach(member -> {
            if (member != null) PacketHandler.sendTo(packet, member);
        });*/
    }

    /*private void sendKnowledgeSync(EntityPlayerMP player) {
        Team team = getTeam(player);
        KnowledgeSyncPKT packet = new KnowledgeSyncPKT(this.serializeNBT());
        if (team == null) {
            PacketHandler.sendTo(packet, player);
        } else {
            team.getTeam().getOnlineMembers().forEach(member -> {
                if (member != null) PacketHandler.sendTo(packet, member);
            });
        }
    }*/

    @Override
    public NBTTagCompound serializeNBT() {
        Team team = this.getTeam();
        if (team == null) return serializeNBTSingle();
        return serializeNBTTeam(team);
    }

    public NBTTagCompound serializeNBTTeam(@NotNull Team team) {
        return serializeHelper(team.getEmc(), team.getKnowledge(), this.inputLocks, team.hasFullKnowledge());
    }

    public NBTTagCompound serializeNBTSingle() {
        return serializeHelper(this.emc, this.knowledge, this.inputLocks, this.fullKnowledge);
    }

    private NBTTagCompound serializeHelper(long emc, List<ItemStack> knowledge, IItemHandlerModifiable inputLocks, boolean fullKnowledge) {
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
    public void deserializeNBT(NBTTagCompound properties) {
        Team team = this.getTeam();
        long emc = properties.getLong("transmutationEmc");

        if (team != null) team.setEmcRaw(emc);
        else this.emc = emc;

        NBTTagList list = properties.getTagList("knowledge", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack item = new ItemStack(list.getCompoundTagAt(i));
            if (!item.isEmpty()) {
                if (team != null) team.addKnowledgeRaw(item);
                knowledge.add(item);
            }
        }

        pruneStaleKnowledge(knowledge);
        pruneDuplicateKnowledge(knowledge);
        if (team != null) {
            team.pruneStaleKnowledge();
            team.pruneDuplicateKnowledge();
        }

        for (int i = 0; i < inputLocks.getSlots(); i++) {
            inputLocks.setStackInSlot(i, ItemStack.EMPTY);
        }

        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(inputLocks, null, properties.getTagList("inputlock", Constants.NBT.TAG_COMPOUND));
        boolean fullKnowledge = properties.getBoolean("fullknowledge");
        if (team != null) team.setFullKnowledgeRaw(fullKnowledge);
        this.fullKnowledge = fullKnowledge;

        // update transmutation GUI
        updateClientTransmutation();
    }

    public static void updateClientTransmutation() {
        //if (!FMLCommonHandler.instance().getEffectiveSide().isClient()) return;
        System.out.println(FMLCommonHandler.instance().getEffectiveSide());

        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;
        if (!(player.openContainer instanceof TransmutationContainer container)) return;
        container.transmutationInventory.updateClientTargets();
    }

    public static void pruneDuplicateKnowledge(List<ItemStack> knowledge) {
        ItemHelper.removeEmptyTags(knowledge);
        ItemHelper.compactItemListNoStacksize(knowledge);
        for (ItemStack s : knowledge) {
            if (s.getCount() > 1) s.setCount(1);
        }
    }

    public static void pruneStaleKnowledge(List<ItemStack> knowledge) {
        knowledge.removeIf(stack -> !EMCHelper.doesItemHaveEmc(stack));
    }

    /*
    object Provider: ICapabilitySerializable<NBTTagCompound> {
        override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
            return capability == ProjectEAPI.KNOWLEDGE_CAPABILITY
        }

        override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
            return if(this.hasCapability(capability, facing)) ProjectEAPI.KNOWLEDGE_CAPABILITY.cast(KnowledgeImplWrapper) else null
        }

        override fun serializeNBT(): NBTTagCompound = KnowledgeImplWrapper.serializeNBT()

        override fun deserializeNBT(nbt: NBTTagCompound?) {
            KnowledgeImplWrapper.deserializeNBT(nbt)
        }

    }
    * */

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
