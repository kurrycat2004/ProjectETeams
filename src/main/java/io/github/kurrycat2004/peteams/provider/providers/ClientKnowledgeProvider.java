package io.github.kurrycat2004.peteams.provider.providers;

import io.github.kurrycat2004.peteams.gui.GuiSync;
import io.github.kurrycat2004.peteams.provider.ProviderUtil;
import io.github.kurrycat2004.peteams.provider.impls.DefaultImpl;
import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class ClientKnowledgeProvider extends DefaultImpl implements ICapabilitySerializable<NBTTagCompound> {
    public ClientKnowledgeProvider(@NotNull EntityPlayer player) {
        super(player);
        this.inputLocks = new ItemStackHandler(9);
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound nbt) {
        boolean resetCache = nbt.getBoolean(ProviderUtil.TAG_RESET_CACHE);
        if (resetCache) this.clearKnowledge();

        super.deserializeNBT(nbt);

        if (nbt.hasKey(ProviderUtil.TAG_KNOWLEDGE)) GuiSync.updateClientTransmutation();
    }

    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == ProjectEAPI.KNOWLEDGE_CAPABILITY;
    }

    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        return capability == ProjectEAPI.KNOWLEDGE_CAPABILITY ? ProjectEAPI.KNOWLEDGE_CAPABILITY.cast(this) : null;
    }
}
