package io.github.kurrycat2004.peteams.provider;

import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class ClientKnowledgeProvider implements IKnowledgeProvider {
    private final IKnowledgeProvider knowledge;

    public ClientKnowledgeProvider(IKnowledgeProvider knowledge) {
        this.knowledge = knowledge;
    }

    @Override
    public boolean hasFullKnowledge() {
        return this.knowledge.hasFullKnowledge();
    }

    @Override
    public void setFullKnowledge(boolean b) {
        this.knowledge.setFullKnowledge(b);
    }

    @Override
    public void clearKnowledge() {
        this.knowledge.clearKnowledge();
    }

    @Override
    public boolean hasKnowledge(@NotNull ItemStack itemStack) {
        return this.knowledge.hasKnowledge(itemStack);
    }

    @Override
    public boolean addKnowledge(@NotNull ItemStack itemStack) {
        return this.knowledge.addKnowledge(itemStack);
    }

    @Override
    public boolean removeKnowledge(@NotNull ItemStack itemStack) {
        return this.knowledge.removeKnowledge(itemStack);
    }

    @Override
    public @NotNull List<ItemStack> getKnowledge() {
        return this.knowledge.getKnowledge();
    }

    @Override
    public @NotNull IItemHandler getInputAndLocks() {
        return this.knowledge.getInputAndLocks();
    }

    @Override
    public long getEmc() {
        return this.knowledge.getEmc();
    }

    @Override
    public void setEmc(long l) {
        this.knowledge.setEmc(l);
    }

    @Override
    public void sync(@NotNull EntityPlayerMP entityPlayerMP) {
        this.knowledge.sync(entityPlayerMP);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return this.knowledge.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        boolean resetCache = nbt.getBoolean("resetCache");
        if (resetCache) this.knowledge.clearKnowledge();

        this.knowledge.deserializeNBT(nbt);
        TeamKnowledgeProvider.updateClientTransmutation();
    }

    public static class Provider implements ICapabilitySerializable<NBTTagCompound> {
        private final ClientKnowledgeProvider knowledge;

        public Provider(IKnowledgeProvider oldProvider) {
            this.knowledge = new ClientKnowledgeProvider(oldProvider);
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
