package io.github.kurrycat2004.peteams.provider;

import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.data.Team;
import io.github.kurrycat2004.peteams.data.TeamKnowledgeData;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.playerData.Transmutation;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OfflineKnowledgeProvider implements IKnowledgeProvider {
    private final String teamUUID;
    private static final IItemHandlerModifiable EMPTY_INPUT_LOCKS = new ItemStackHandler(9);

    public OfflineKnowledgeProvider(String teamUUID) {
        this.teamUUID = teamUUID;
    }

    public @NotNull Team getTeam() {
        return TeamKnowledgeData.getInstance().getTeam(teamUUID);
    }

    @Override
    public boolean hasFullKnowledge() {
        return this.getTeam().hasFullKnowledge();
    }

    @Override
    public void setFullKnowledge(boolean b) {
        this.getTeam().setFullKnowledge(null, b);
    }

    @Override
    public void clearKnowledge() {
        this.getTeam().clearKnowledge(null);
    }

    @Override
    public boolean hasKnowledge(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (hasFullKnowledge()) return true;

        for (ItemStack s : this.getTeam().getKnowledge()) {
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

        if (!knowsItem) team.addKnowledge(null, stack);
        if (isTome) team.setFullKnowledge(null, true);

        return true;
    }

    @Override
    public boolean removeKnowledge(@NotNull ItemStack stack) {
        Team team = this.getTeam();
        boolean result = false;
        if (stack.getItem() == ObjHandler.tome) {
            team.setFullKnowledge(null, false);
            result = true;
        }

        if (this.hasFullKnowledge()) return false;

        result |= team.removeKnowledge(null, stack);

        return result;
    }

    @Override
    public @NotNull List<ItemStack> getKnowledge() {
        if (this.hasFullKnowledge()) return Transmutation.getCachedTomeKnowledge();
        return this.getTeam().getKnowledge();
    }

    @Override
    public @NotNull IItemHandler getInputAndLocks() {
        return EMPTY_INPUT_LOCKS;
    }

    @Override
    public long getEmc() {
        long emc = this.getTeam().getEmc();
        System.out.println("OfflineKnowledgeProvider.getEmc: " + emc);
        return emc;
    }

    @Override
    public void setEmc(long l) {
        this.getTeam().setEmc(null, l);
    }

    @Override
    public void sync(@NotNull EntityPlayerMP entityPlayerMP) {
        TeamKnowledgeProvider.sendKnowledgeSyncTeam(this.getTeam(), entityPlayerMP, EMPTY_INPUT_LOCKS);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return TeamKnowledgeProvider.serializeNBTTeam(this.getTeam(), EMPTY_INPUT_LOCKS);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        PETeams.LOGGER.warn("Attempted to deserialize NBT on an offline knowledge provider, this should not happen");
    }
}
