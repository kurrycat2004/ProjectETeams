package io.github.kurrycat2004.peteams.data;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.Tags;
import io.github.kurrycat2004.peteams.config.FTBPETeamsData;
import io.github.kurrycat2004.peteams.provider.ProviderUtil;
import io.github.kurrycat2004.peteams.provider.providers.SplitKnowledgeProvider;
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
    public static final String TAG_UUID = "uuid";
    public static final String TAG_EMC = "emc";
    public static final String TAG_KNOWLEDGE = "knowledge";
    public static final String TAG_FULL_KNOWLEDGE = "fullknowledge";

    private String uuid;
    private long emc = 0;
    private final List<ItemStack> knowledge = new ArrayList<>();
    private final List<ItemStack> view = Collections.unmodifiableList(knowledge);
    private boolean fullKnowledge = false;

    /**
     * whether emc should be synced with members
     */
    private boolean dirtyEmc = false;
    /**
     * whether knowledge should be synced with members
     */
    private boolean dirtyKnowledge = false;
    /**
     * the player that caused dirty knowledge <br>
     * <code>null</code> if no player / multiple players caused it <br>
     * can be skipped in next sync packet if set <br>
     */
    @Nullable
    private UUID dirtyKnowledgePlayerUUID = null;

    public Team() {
        super("team");
    }

    public Team(String uuid) {
        this();
        this.uuid = uuid;
    }

    public @NotNull ForgeTeam getTeam() {
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

    public List<ItemStack> getKnowledgeMut() {
        return knowledge;
    }

    public void clearKnowledge(@Nullable UUID playerUUID) {
        knowledge.clear();
        markKnowledgeDirty(playerUUID);
    }

    public boolean removeKnowledge(@Nullable UUID playerUUID, ItemStack stack) {
        boolean removed = knowledge.removeIf(s -> ItemHelper.basicAreStacksEqual(stack, s));
        markKnowledgeDirty(playerUUID);
        return removed;
    }

    public void syncPending() {
        if (dirtyKnowledge) pushKnowledgeSyncAll();
        else if (dirtyEmc) pushEmcSyncAll();
    }

    public void pushEmcSyncAll() {
        this.getTeam().getOnlineMembers().forEach(player -> {
            SplitKnowledgeProvider capability = (SplitKnowledgeProvider) player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
            if (capability == null) return;

            capability.sendEmcSync(player);
        });
        this.dirtyEmc = false;
    }

    public void pushKnowledgeSyncAll() {
        this.getTeam().getOnlineMembers().forEach(player -> {
            if (player.getUniqueID().equals(dirtyKnowledgePlayerUUID)) return;
            SplitKnowledgeProvider capability = (SplitKnowledgeProvider) player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
            if (capability == null) return;

            capability.pullKnowledgeFromTeam();
            capability.sendKnowledgeSync(player);
        });
        this.dirtyEmc = false;
        this.dirtyKnowledge = false;
        this.dirtyKnowledgePlayerUUID = null;
    }

    private void pullKnowledgeFromMembers() {
        this.getTeam().getOnlineMembers().forEach(player -> {
            SplitKnowledgeProvider capability = (SplitKnowledgeProvider) player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY, null);
            if (capability == null) return;

            capability.pushKnowledgeToTeam();
        });
    }

    public void shareKnowledgeChangedSync() {
        pullKnowledgeFromMembers();
        markKnowledgeDirty(null);

        pushKnowledgeSyncAll();
    }

    public void pruneDuplicateKnowledge() {
        ProviderUtil.pruneDuplicateKnowledge(knowledge);
    }

    public void pruneStaleKnowledge() {
        ProviderUtil.pruneStaleKnowledge(knowledge);
    }

    public void addKnowledge(@Nullable UUID playerUUID, ItemStack stack) {
        knowledge.add(stack);
        markKnowledgeDirty(playerUUID);
    }

    public void addKnowledgeRaw(ItemStack stack) {
        knowledge.add(stack);
    }

    public String getUuid() {
        return uuid;
    }

    public void setEmc(@Nullable UUID playerUUID, long emc) {
        this.emc = emc;
        markEmcDirty(playerUUID);
    }

    public void setEmcRaw(long emc) {
        this.emc = emc;
    }

    public void setFullKnowledge(@Nullable UUID playerUUID, boolean fullKnowledge) {
        this.fullKnowledge = fullKnowledge;
        markKnowledgeDirty(playerUUID);
    }

    public void setFullKnowledgeRaw(boolean fullKnowledge) {
        this.fullKnowledge = fullKnowledge;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.uuid = nbt.getString(TAG_UUID);
        this.emc = nbt.getLong(TAG_EMC);

        NBTTagList list = nbt.getTagList(TAG_KNOWLEDGE, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack item = new ItemStack(list.getCompoundTagAt(i));
            if (!item.isEmpty()) knowledge.add(item);
        }
        pruneStaleKnowledge();
        pruneDuplicateKnowledge();
        fullKnowledge = nbt.getBoolean(TAG_FULL_KNOWLEDGE);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        if (uuid == null || uuid.isEmpty()) {
            PETeams.LOGGER.warn("Tried to save invalid team: missing uuid");
            return compound;
        }
        compound.setString(TAG_UUID, uuid);
        compound.setLong(TAG_EMC, emc);

        NBTTagList knowledgeWrite = new NBTTagList();
        for (ItemStack i : knowledge) {
            NBTTagCompound tag = i.writeToNBT(new NBTTagCompound());
            knowledgeWrite.appendTag(tag);
        }

        compound.setTag(TAG_KNOWLEDGE, knowledgeWrite);
        compound.setBoolean(TAG_FULL_KNOWLEDGE, fullKnowledge);
        return compound;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        TeamSavedData.getInstance().markDirty();
    }

    public void markEmcDirty(@Nullable UUID dirtyPlayerUUID) {
        dirtyEmc = true;
        markDirty();
    }

    public void markKnowledgeDirty(@Nullable UUID dirtyPlayerUUID) {
        if (!dirtyKnowledge && dirtyKnowledgePlayerUUID == null)
            dirtyKnowledgePlayerUUID = dirtyPlayerUUID;
        else if (dirtyKnowledgePlayerUUID != dirtyPlayerUUID)
            dirtyKnowledgePlayerUUID = null;

        dirtyKnowledge = true;
        markDirty();
    }

    public boolean isShareEmc() {
        FTBPETeamsData data = getTeam().getData().get(Tags.MODID);
        return data.isShareEmc();
    }

    public boolean isShareKnowledge() {
        FTBPETeamsData data = getTeam().getData().get(Tags.MODID);
        return data.isShareKnowledge();
    }
}
