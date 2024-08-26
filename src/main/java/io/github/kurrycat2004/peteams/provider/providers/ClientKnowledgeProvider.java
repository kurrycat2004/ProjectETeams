package io.github.kurrycat2004.peteams.provider.providers;

import io.github.kurrycat2004.peteams.gui.GuiSync;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.nbt.NBTTagCompound;

public class ClientKnowledgeProvider extends IdentityKnowledgeProvider {
    public ClientKnowledgeProvider(IKnowledgeProvider provider) {
        super(provider);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        boolean resetCache = nbt.getBoolean("resetCache");
        if (resetCache) this.provider.clearKnowledge();

        this.provider.deserializeNBT(nbt);
        GuiSync.updateClientTransmutation();
    }
}
