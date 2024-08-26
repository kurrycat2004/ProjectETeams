package io.github.kurrycat2004.peteams.provider.interfaces;

import io.github.kurrycat2004.peteams.provider.ProviderUtil;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IKnowledgeHolder extends IKnowledgeProvider {
    void setInputAndLocks(@NotNull IItemHandlerModifiable inputLocks);

    /**
     * Sets the EMC of the holder without updating anything else
     *
     * @param emc EMC to set
     */
    void setEmcRaw(long emc);

    /**
     * @return Immutable view of the knowledge list
     */
    @NotNull List<ItemStack> getKnowledge();

    /**
     * @return Mutable view of the knowledge list
     */
    @NotNull List<ItemStack> getKnowledgeMut();

    /**
     * @return Immutable view of the input and lock slots
     */
    @NotNull IItemHandlerModifiable getInputAndLocksMut();

    /**
     * Sets the full knowledge of the holder without updating anything else
     */
    void setFullKnowledgeRaw(boolean fullKnowledge);

    boolean hasFullKnowledge();

    void fireChangedEvent();

    default void pullKnowledgeFrom(@NotNull IKnowledgeHolder other) {
        List<ItemStack> knowledge = this.getKnowledgeMut();
        for (ItemStack stack : other.getKnowledge()) {
            if (!stack.isEmpty()) knowledge.add(stack);
        }
        ProviderUtil.pruneStaleKnowledge(knowledge);
        ProviderUtil.pruneDuplicateKnowledge(knowledge);
        if (other.hasFullKnowledge()) this.setFullKnowledgeRaw(true);

        this.fireChangedEvent();
    }
}
