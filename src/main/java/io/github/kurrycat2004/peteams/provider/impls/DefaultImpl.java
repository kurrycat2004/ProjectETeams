package io.github.kurrycat2004.peteams.provider.impls;

import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.provider.ProviderUtil;
import io.github.kurrycat2004.peteams.provider.interfaces.IKnowledgeHolder;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.playerData.Transmutation;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultImpl implements IKnowledgeHolder {
    private final EntityPlayer player;
    private IItemHandlerModifiable inputLocks = null;
    private final List<ItemStack> knowledge = new ArrayList<>();
    private final List<ItemStack> view = Collections.unmodifiableList(knowledge);
    private long emc = 0;
    private boolean fullKnowledge = false;

    public DefaultImpl(@NotNull EntityPlayer player) {
        this.player = player;
    }

    public void fireChangedEvent() {
        if (this.player.world.isRemote) return;
        MinecraftForge.EVENT_BUS.post(new PlayerKnowledgeChangeEvent(this.player));
    }

    @Override
    public boolean hasFullKnowledge() {
        return this.fullKnowledge;
    }

    @Override
    public void setFullKnowledge(boolean fullKnowledge) {
        if (this.fullKnowledge == fullKnowledge) return;
        this.fullKnowledge = fullKnowledge;
        fireChangedEvent();
    }

    @Override
    public void clearKnowledge() {
        PETeams.debugLog("Clearing knowledge for {}", this.player.getName());
        knowledge.clear();
        fullKnowledge = false;
        fireChangedEvent();
    }

    @Override
    public boolean hasKnowledge(@NotNull ItemStack stack) {
        if (fullKnowledge) return true;
        return ProviderUtil.basicContainsStack(this.view, stack);
    }

    @Override
    public boolean addKnowledge(@NotNull ItemStack stack) {
        if (fullKnowledge) return false;

        boolean knowsItem = this.hasKnowledge(stack);
        boolean isTome = stack.getItem() == ObjHandler.tome;
        if (knowsItem && isTome) return false;

        if (!knowsItem) {
            PETeams.debugLog("Adding knowledge {} for {} ({})", stack, this.player.getName(), this.player.getUniqueID());
            knowledge.add(stack);
        }

        if (isTome) fullKnowledge = true;

        fireChangedEvent();
        return true;
    }

    @Override
    public boolean removeKnowledge(@NotNull ItemStack stack) {
        boolean changed = false;
        if (stack.getItem() == ObjHandler.tome && fullKnowledge) {
            this.fullKnowledge = false;
            changed = true;
        }

        if (fullKnowledge) return false;

        boolean removed = this.knowledge.removeIf(s -> ItemHelper.basicAreStacksEqual(stack, s));

        if (changed || removed) this.fireChangedEvent();
        return removed || changed;
    }

    @NotNull
    @Override
    public List<ItemStack> getKnowledge() {
        if (fullKnowledge) return Transmutation.getCachedTomeKnowledge();
        return this.view;
    }

    @NotNull
    @Override
    public IItemHandler getInputAndLocks() {
        return inputLocks;
    }

    @Override
    public long getEmc() {
        return this.emc;
    }

    @Override
    public void setEmc(long l) {
        this.emc = l;
    }

    @Override
    public void sync(@NotNull EntityPlayerMP player) {
        ProviderUtil.sendSync(player, this, false);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return ProviderUtil.serializeHelper(this.emc, this.knowledge, this.inputLocks, this.fullKnowledge);
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound properties) {
        ProviderUtil.deserializeHelper(this, properties);
    }

    @Override
    public void setInputAndLocks(@NotNull IItemHandlerModifiable inputLocks) {
        this.inputLocks = inputLocks;
    }

    @Override
    public void setEmcRaw(long l) {this.emc = l;}

    @Override
    public @NotNull List<ItemStack> getKnowledgeMut() {return this.knowledge;}

    @Override
    public @NotNull IItemHandlerModifiable getInputAndLocksMut() {return this.inputLocks;}

    @Override
    public void setFullKnowledgeRaw(boolean fullKnowledge) {this.fullKnowledge = fullKnowledge;}
}