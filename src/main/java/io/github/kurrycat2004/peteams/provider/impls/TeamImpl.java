package io.github.kurrycat2004.peteams.provider.impls;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.data.Team;
import io.github.kurrycat2004.peteams.data.TeamSavedData;
import io.github.kurrycat2004.peteams.provider.ProviderUtil;
import io.github.kurrycat2004.peteams.provider.interfaces.ITeamKnowledgeHolder;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.playerData.Transmutation;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TeamImpl implements ITeamKnowledgeHolder {
    private final Supplier<UUID> playerUUIDSupplier;
    private IItemHandlerModifiable inputLocks;

    /**
     * Use this if the player object is not initialized yet (e.g. in an {@link net.minecraftforge.event.AttachCapabilitiesEvent}) <br>
     * Will be memoized
     *
     * @param playerUUIDSupplier Supplier for the player's UUID
     */
    public TeamImpl(@NotNull Supplier<UUID> playerUUIDSupplier) {
        this.playerUUIDSupplier = Suppliers.memoize(playerUUIDSupplier);
    }

    public TeamImpl(@NotNull UUID playerUUID) {
        this(() -> playerUUID);
    }

    public @Nullable Team getTeam() {
        return TeamSavedData.getTeamFromPlayerUUID(playerUUIDSupplier.get());
    }

    public boolean isShareEmc() {
        Team team = getTeam();
        return team != null && team.isShareEmc();
    }

    public boolean isShareKnowledge() {
        Team team = getTeam();
        return team != null && team.isShareKnowledge();
    }

    @Override
    public void fireChangedEvent() {}

    @Override
    public boolean hasFullKnowledge() {
        return Objects.requireNonNull(this.getTeam()).hasFullKnowledge();
    }

    @Override
    public void setFullKnowledge(boolean fullKnowledge) {
        Objects.requireNonNull(this.getTeam()).setFullKnowledge(playerUUIDSupplier.get(), fullKnowledge);
        fireChangedEvent();
    }

    @Override
    public void clearKnowledge() {
        Team team = Objects.requireNonNull(this.getTeam());
        PETeams.debugLog("Clearing knowledge for team {}", team.getTeam());
        team.clearKnowledge(playerUUIDSupplier.get());
        fireChangedEvent();
    }

    @Override
    public boolean hasKnowledge(@NotNull ItemStack stack) {
        Team team = Objects.requireNonNull(this.getTeam());
        if (team.hasFullKnowledge()) return true;
        return ProviderUtil.basicContainsStack(team.getKnowledge(), stack);
    }

    @Override
    public boolean addKnowledge(@NotNull ItemStack stack) {
        Team team = Objects.requireNonNull(this.getTeam());
        if (team.hasFullKnowledge()) return false;

        boolean knowsItem = this.hasKnowledge(stack);
        boolean isTome = stack.getItem() == ObjHandler.tome;
        if (knowsItem && isTome) return false;

        if (!knowsItem) {
            PETeams.debugLog("Adding knowledge {} for player {}", stack, playerUUIDSupplier.get());
            team.addKnowledge(playerUUIDSupplier.get(), stack);
        }

        if (isTome) team.setFullKnowledge(playerUUIDSupplier.get(), true);

        fireChangedEvent();
        return true;
    }

    @Override
    public boolean removeKnowledge(@NotNull ItemStack stack) {
        Team team = Objects.requireNonNull(this.getTeam());
        boolean changed = false;
        if (stack.getItem() == ObjHandler.tome && team.hasFullKnowledge()) {
            team.setFullKnowledgeRaw(false);
            changed = true;
        }

        if (team.hasFullKnowledge()) return false;

        boolean removed = team.getKnowledgeMut().removeIf(s -> ItemHelper.basicAreStacksEqual(stack, s));

        if (changed || removed) {
            team.markKnowledgeDirty(playerUUIDSupplier.get());
            this.fireChangedEvent();
        }
        return removed || changed;
    }

    @NotNull
    @Override
    public List<ItemStack> getKnowledge() {
        Team team = Objects.requireNonNull(this.getTeam());
        if (team.hasFullKnowledge()) return Transmutation.getCachedTomeKnowledge();
        return team.getKnowledge();
    }

    @NotNull
    @Override
    public IItemHandler getInputAndLocks() {
        return inputLocks;
    }

    @Override
    public long getEmc() {
        return Objects.requireNonNull(this.getTeam()).getEmc();
    }

    @Override
    public void setEmc(long l) {
        Objects.requireNonNull(this.getTeam()).setEmc(playerUUIDSupplier.get(), l);
    }

    @Override
    public void sync(@NotNull EntityPlayerMP player) {
        PETeams.debugLog("Warning: sync called on a TeamImpl, this will work as expected, but should not happen");
        ProviderUtil.sendKnowledgeSync(player, this, true);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        Team team = Objects.requireNonNull(this.getTeam());
        PETeams.debugLog("Warning: serializeNBT called on a TeamImpl, this will work as expected, but should not happen");
        return ProviderUtil.serializeHelper(team.getEmc(), team.getKnowledge(), this.inputLocks, team.hasFullKnowledge());
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
    public void setEmcRaw(long emc) {Objects.requireNonNull(this.getTeam()).setEmcRaw(emc);}

    @Override
    public @NotNull List<ItemStack> getKnowledgeMut() {return Objects.requireNonNull(this.getTeam()).getKnowledgeMut();}

    @Override
    public @NotNull IItemHandlerModifiable getInputAndLocksMut() {return this.inputLocks;}

    @Override
    public void setFullKnowledgeRaw(boolean fullKnowledge) {Objects.requireNonNull(this.getTeam()).setFullKnowledgeRaw(fullKnowledge);}
}
