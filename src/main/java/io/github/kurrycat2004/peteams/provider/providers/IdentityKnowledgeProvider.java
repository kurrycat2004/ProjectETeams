package io.github.kurrycat2004.peteams.provider.providers;

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

public class IdentityKnowledgeProvider implements IKnowledgeProvider, ICapabilitySerializable<NBTTagCompound> {
    protected final IKnowledgeProvider provider;

    public IdentityKnowledgeProvider(IKnowledgeProvider provider) {
        this.provider = provider;
    }

    public boolean hasFullKnowledge() {return this.provider.hasFullKnowledge();}

    public void setFullKnowledge(boolean b) {this.provider.setFullKnowledge(b);}

    public void clearKnowledge() {this.provider.clearKnowledge();}

    public boolean hasKnowledge(@NotNull ItemStack itemStack) {return this.provider.hasKnowledge(itemStack);}

    public boolean addKnowledge(@NotNull ItemStack itemStack) {return this.provider.addKnowledge(itemStack);}

    public boolean removeKnowledge(@NotNull ItemStack itemStack) {return this.provider.removeKnowledge(itemStack);}

    public @NotNull List<ItemStack> getKnowledge() {return this.provider.getKnowledge();}

    public @NotNull IItemHandler getInputAndLocks() {return this.provider.getInputAndLocks();}

    public long getEmc() {return this.provider.getEmc();}

    public void setEmc(long l) {this.provider.setEmc(l);}

    public void sync(@NotNull EntityPlayerMP entityPlayerMP) {this.provider.sync(entityPlayerMP);}

    public NBTTagCompound serializeNBT() {return this.provider.serializeNBT();}

    public void deserializeNBT(NBTTagCompound nbt) {this.provider.deserializeNBT(nbt);}

    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == ProjectEAPI.KNOWLEDGE_CAPABILITY;
    }

    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        return capability == ProjectEAPI.KNOWLEDGE_CAPABILITY ? ProjectEAPI.KNOWLEDGE_CAPABILITY.cast(this) : null;
    }
}

