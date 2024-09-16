package io.github.kurrycat2004.peteams.provider;

import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.data.Team;
import io.github.kurrycat2004.peteams.net.EmcSyncPKT;
import io.github.kurrycat2004.peteams.net.PETPacketHandler;
import io.github.kurrycat2004.peteams.provider.interfaces.IKnowledgeHolder;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.KnowledgeSyncPKT;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProviderUtil {
    public static final String TAG_EMC = "transmutationEmc";
    public static final String TAG_KNOWLEDGE = "knowledge";
    public static final String TAG_INPUT_LOCK = "inputlock";
    public static final String TAG_FULL_KNOWLEDGE = "fullknowledge";
    public static final String TAG_RESET_CACHE = "resetCache";

    public static @NotNull NBTTagCompound serializeTeamToMemberSyncPacket(@NotNull Team team, IItemHandlerModifiable inputLocks) {
        return ProviderUtil.serializeHelper(team.getEmc(), team.getKnowledge(), inputLocks, team.hasFullKnowledge());
    }

    public static @NotNull NBTTagCompound serializeHelper(long emc, @NotNull List<ItemStack> knowledge, IItemHandler inputLocks, boolean fullKnowledge) {
        NBTTagCompound properties = new NBTTagCompound();
        properties.setLong(TAG_EMC, emc);

        NBTTagList knowledgeWrite = new NBTTagList();
        for (ItemStack i : knowledge) {
            NBTTagCompound tag = i.writeToNBT(new NBTTagCompound());
            knowledgeWrite.appendTag(tag);
        }

        properties.setTag(TAG_KNOWLEDGE, knowledgeWrite);
        //noinspection DataFlowIssue
        properties.setTag(TAG_INPUT_LOCK, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(inputLocks, null));
        properties.setBoolean(TAG_FULL_KNOWLEDGE, fullKnowledge);
        return properties;
    }

    public static void pruneDuplicateKnowledge(List<ItemStack> knowledge) {
        //TODO: optimize compactItemListNoStacksize
        ItemHelper.removeEmptyTags(knowledge);
        ItemHelper.compactItemListNoStacksize(knowledge);
        for (ItemStack s : knowledge) {
            if (s.getCount() > 1) s.setCount(1);
        }
    }

    public static void pruneStaleKnowledge(@NotNull List<ItemStack> knowledge) {
        knowledge.removeIf(stack -> !EMCHelper.doesItemHaveEmc(stack));
    }

    public static boolean basicContainsStack(@NotNull List<ItemStack> list, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        for (ItemStack s : list) {
            if (ItemHelper.basicAreStacksEqual(s, stack)) return true;
        }
        return false;
    }

    public static void sendKnowledgeSync(@NotNull EntityPlayerMP player, IKnowledgeProvider provider, boolean resetCache) {
        PETeams.debugLog("Sending knowledge from IKnowledgeProvider to {} ({})", player.getName(), player.getUniqueID());
        NBTTagCompound nbt = ProviderUtil.serializeHelper(
                provider.getEmc(),
                provider.getKnowledge(),
                provider.getInputAndLocks(),
                provider.hasFullKnowledge()
        );
        nbt.setBoolean(TAG_RESET_CACHE, resetCache);
        PacketHandler.sendTo(new KnowledgeSyncPKT(nbt), player);
    }

    public static void sendEmcSync(@NotNull EntityPlayerMP player, IKnowledgeProvider provider) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong(TAG_EMC, provider.getEmc());
        PETPacketHandler.sendTo(new EmcSyncPKT(nbt), player);
    }

    public static void deserializeHelper(@NotNull IKnowledgeHolder holder, @NotNull NBTTagCompound nbt) {
        holder.setEmcRaw(nbt.getLong(TAG_EMC));

        if (nbt.hasKey(TAG_KNOWLEDGE)) {
            NBTTagList knowledgeTagList = nbt.getTagList(TAG_KNOWLEDGE, Constants.NBT.TAG_COMPOUND);
            List<ItemStack> knowledge = holder.getKnowledgeMut();
            for (int i = 0; i < knowledgeTagList.tagCount(); i++) {
                ItemStack item = new ItemStack(knowledgeTagList.getCompoundTagAt(i));
                if (!item.isEmpty()) knowledge.add(item);
            }

            ProviderUtil.pruneStaleKnowledge(knowledge);
            ProviderUtil.pruneDuplicateKnowledge(knowledge);
        }

        if (nbt.hasKey(TAG_INPUT_LOCK)) {
            NBTTagList inputLocksTagList = nbt.getTagList(TAG_INPUT_LOCK, Constants.NBT.TAG_COMPOUND);

            IItemHandlerModifiable inputLocks = holder.getInputAndLocksMut();
            for (int i = 0; i < inputLocks.getSlots(); i++) {
                inputLocks.setStackInSlot(i, ItemStack.EMPTY);
            }

            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(inputLocks, null, inputLocksTagList);
        }

        if (nbt.hasKey(TAG_FULL_KNOWLEDGE)) {
            holder.setFullKnowledgeRaw(nbt.getBoolean(TAG_FULL_KNOWLEDGE));
        }
    }
}
