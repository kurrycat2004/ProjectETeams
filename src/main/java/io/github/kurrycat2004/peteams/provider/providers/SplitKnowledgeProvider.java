package io.github.kurrycat2004.peteams.provider.providers;

import io.github.kurrycat2004.peteams.data.Team;
import io.github.kurrycat2004.peteams.provider.ProviderUtil;
import io.github.kurrycat2004.peteams.provider.interfaces.IKnowledgeHolder;
import io.github.kurrycat2004.peteams.provider.interfaces.ITeamKnowledgeHolder;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class SplitKnowledgeProvider implements IKnowledgeProvider, ICapabilitySerializable<NBTTagCompound> {
    private final IItemHandlerModifiable inputLocks = new ItemStackHandler(9);
    private final IKnowledgeHolder defaultImpl;
    private final ITeamKnowledgeHolder teamImpl;

    public SplitKnowledgeProvider(IKnowledgeHolder defaultImpl, ITeamKnowledgeHolder teamImpl) {
        this.defaultImpl = defaultImpl;
        this.defaultImpl.setInputAndLocks(this.inputLocks);
        this.teamImpl = teamImpl;
        this.teamImpl.setInputAndLocks(this.inputLocks);
    }

    @Override
    public boolean hasFullKnowledge() {
        if (this.teamImpl.isShareKnowledge()) return this.teamImpl.hasFullKnowledge();
        return this.defaultImpl.hasFullKnowledge();
    }

    @Override
    public void setFullKnowledge(boolean fullKnowledge) {
        if (this.teamImpl.isShareKnowledge()) this.teamImpl.setFullKnowledge(fullKnowledge);
        this.defaultImpl.setFullKnowledge(fullKnowledge);
    }

    @Override
    public void clearKnowledge() {
        if (this.teamImpl.isShareKnowledge()) this.teamImpl.clearKnowledge();
        this.defaultImpl.clearKnowledge();
    }

    @Override
    public boolean hasKnowledge(@NotNull ItemStack stack) {
        if (this.teamImpl.isShareKnowledge()) return this.teamImpl.hasKnowledge(stack);
        return this.defaultImpl.hasKnowledge(stack);
    }

    @Override
    public boolean addKnowledge(@NotNull ItemStack stack) {
        boolean added = false;
        if (this.teamImpl.isShareKnowledge()) added = this.teamImpl.addKnowledge(stack);
        return this.defaultImpl.addKnowledge(stack) || added;
    }

    @Override
    public boolean removeKnowledge(@NotNull ItemStack stack) {
        boolean removed = false;
        if (this.teamImpl.isShareKnowledge()) removed = this.teamImpl.removeKnowledge(stack);
        return this.defaultImpl.removeKnowledge(stack) || removed;
    }

    @NotNull
    @Override
    public List<ItemStack> getKnowledge() {
        if (this.teamImpl.isShareKnowledge()) return this.teamImpl.getKnowledge();
        return this.defaultImpl.getKnowledge();
    }

    @NotNull
    @Override
    public IItemHandler getInputAndLocks() {
        return inputLocks;
    }

    @Override
    public long getEmc() {
        if (this.teamImpl.isShareEmc()) return this.teamImpl.getEmc();
        return this.defaultImpl.getEmc();
    }

    @Override
    public void setEmc(long l) {
        // Only update one of (team, personal)
        if (this.teamImpl.isShareEmc()) this.teamImpl.setEmc(l);
        else this.defaultImpl.setEmc(l);
    }

    @Override
    public void sync(@NotNull EntityPlayerMP player) {
        boolean shareKnowledge = this.teamImpl.isShareKnowledge();
        ProviderUtil.sendKnowledgeSync(player, this, shareKnowledge);
    }

    public void syncMemberLeft(@NotNull EntityPlayerMP player) {
        ProviderUtil.sendKnowledgeSync(player, this.defaultImpl, true);
    }

    public void pullKnowledgeFromTeam() {
        Team team = this.teamImpl.getTeam();
        if (team == null || !team.isShareKnowledge()) return;
        this.defaultImpl.pullKnowledgeFrom(this.teamImpl);
    }

    public void sendKnowledgeSync(EntityPlayerMP player) {
        Team team = this.teamImpl.getTeam();
        if (team == null) return;
        ProviderUtil.sendKnowledgeSync(player, this, true);
    }

    public void sendEmcSync(EntityPlayerMP player) {
        Team team = this.teamImpl.getTeam();
        if (team == null) return;
        ProviderUtil.sendEmcSync(player, this);
    }

    public void syncMemberJoin() {
        Team team = this.teamImpl.getTeam();
        if (team == null) return;
        if (team.isShareKnowledge()) this.teamImpl.pullKnowledgeFrom(this.defaultImpl);
        team.markKnowledgeDirty(null);
        team.pushKnowledgeSyncAll();
    }

    public void pushKnowledgeToTeam() {
        Team team = this.teamImpl.getTeam();
        if (team == null || !team.isShareKnowledge()) return;
        this.teamImpl.pullKnowledgeFrom(this.defaultImpl);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return this.defaultImpl.serializeNBT();
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound properties) {
        this.defaultImpl.deserializeNBT(properties);
    }

    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == ProjectEAPI.KNOWLEDGE_CAPABILITY;
    }

    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        return capability == ProjectEAPI.KNOWLEDGE_CAPABILITY ? ProjectEAPI.KNOWLEDGE_CAPABILITY.cast(this) : null;
    }
}
