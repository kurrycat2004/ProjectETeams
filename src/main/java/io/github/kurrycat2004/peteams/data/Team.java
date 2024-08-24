package io.github.kurrycat2004.peteams.data;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.provider.TeamKnowledgeProvider;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Team extends WorldSavedData {
    private String uuid;
    private long emc = 0;
    private final List<ItemStack> knowledge = new ArrayList<>();
    private final List<ItemStack> view = Collections.unmodifiableList(knowledge);
    private boolean fullKnowledge = false;

    public Team() {
        super("team");
    }

    public Team(String uuid) {
        this();
        this.uuid = uuid;
    }

    /*public void trySync() {
        if (cachedEmc != emc) {
            cachedEmc = emc;
            ForgeTeam forgeTeam = this.getTeam();
            if (forgeTeam == null) return;
            forgeTeam.getOnlineMembers().forEach(player -> {
                TeamKnowledgeProvider capability = (TeamKnowledgeProvider) player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
                if (capability == null) return;
                capability.sync(player);
            });
        }
    }*/

    public ForgeTeam getTeam() {
        return Universe.get().getTeam(uuid);
    }

    public long getEmc() {
        return emc;
    }

    public boolean hasFullKnowledge() {
        return fullKnowledge;
    }


    /**
     * @return an unmodifiable list of the knowledge of this team
     */
    public List<ItemStack> getKnowledge() {
        return view;
    }

    public void clearKnowledge(@Nullable UUID playerUUID) {
        knowledge.clear();
        markDirty();
        sync(playerUUID);
    }

    public boolean removeKnowledge(@Nullable UUID playerUUID, ItemStack stack) {
        boolean removed = knowledge.removeIf(s -> ItemHelper.basicAreStacksEqual(stack, s));
        markDirty();
        sync(playerUUID);
        return removed;
    }

    public void update(@Nullable UUID playerUUID) {
        markDirty();
        sync(playerUUID);
    }

    private void sync(@Nullable UUID playerUUID) {
        ForgeTeam forgeTeam = this.getTeam();
        if (forgeTeam == null) return;
        forgeTeam.getOnlineMembers().forEach(player -> {
            if (player.getUniqueID().equals(playerUUID)) return;
            TeamKnowledgeProvider capability = (TeamKnowledgeProvider) player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
            if (capability == null) return;

            capability.knowledgeSyncTeam(this);
            capability.sendKnowledgeSyncTeam(this, player);
        });
    }

    public void pruneDuplicateKnowledge() {
        TeamKnowledgeProvider.pruneDuplicateKnowledge(knowledge);
    }

    public void pruneStaleKnowledge() {
        TeamKnowledgeProvider.pruneStaleKnowledge(knowledge);
    }

    public void addKnowledge(@Nullable UUID playerUUID, ItemStack stack) {
        knowledge.add(stack);
        markDirty();
        sync(playerUUID);
    }

    public void addKnowledgeRaw(ItemStack stack) {
        knowledge.add(stack);
    }

    public String getUuid() {
        return uuid;
    }

    public void setEmc(@Nullable UUID playerUUID, long emc) {
        this.emc = emc;
        markDirty();
        sync(playerUUID);
    }

    public void setEmcRaw(long emc) {
        this.emc = emc;
    }

    public void setFullKnowledge(@Nullable UUID playerUUID, boolean fullKnowledge) {
        this.fullKnowledge = fullKnowledge;
        markDirty();
        sync(playerUUID);
    }

    public void setFullKnowledgeRaw(boolean fullKnowledge) {
        this.fullKnowledge = fullKnowledge;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.uuid = nbt.getString("uuid");
        this.emc = nbt.getLong("emc");

        NBTTagList list = nbt.getTagList("knowledge", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack item = new ItemStack(list.getCompoundTagAt(i));
            if (!item.isEmpty()) knowledge.add(item);
        }
        pruneStaleKnowledge();
        pruneDuplicateKnowledge();
        fullKnowledge = nbt.getBoolean("fullknowledge");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        if (uuid == null || uuid.isEmpty()) {
            PETeams.LOGGER.warn("Tried to save invalid team: missing uuid");
            return compound;
        }
        compound.setString("uuid", uuid);
        compound.setLong("emc", emc);

        NBTTagList knowledgeWrite = new NBTTagList();
        for (ItemStack i : knowledge) {
            NBTTagCompound tag = i.writeToNBT(new NBTTagCompound());
            knowledgeWrite.appendTag(tag);
        }

        compound.setTag("knowledge", knowledgeWrite);
        compound.setBoolean("fullknowledge", fullKnowledge);
        return compound;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        TeamKnowledgeData.getInstance().markDirty();
    }
}
