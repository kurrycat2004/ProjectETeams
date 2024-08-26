package io.github.kurrycat2004.peteams.provider;

import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.data.Team;
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
    public static @NotNull NBTTagCompound serializeTeamToMemberSyncPacket(@NotNull Team team, IItemHandlerModifiable inputLocks) {
        return ProviderUtil.serializeHelper(team.getEmc(), team.getKnowledge(), inputLocks, team.hasFullKnowledge());
    }

    public static @NotNull NBTTagCompound serializeHelper(long emc, @NotNull List<ItemStack> knowledge, IItemHandler inputLocks, boolean fullKnowledge) {
        NBTTagCompound properties = new NBTTagCompound();
        properties.setLong("transmutationEmc", emc);

        NBTTagList knowledgeWrite = new NBTTagList();
        for (ItemStack i : knowledge) {
            NBTTagCompound tag = i.writeToNBT(new NBTTagCompound());
            knowledgeWrite.appendTag(tag);
        }

        properties.setTag("knowledge", knowledgeWrite);
        //noinspection DataFlowIssue
        properties.setTag("inputlock", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(inputLocks, null));
        properties.setBoolean("fullknowledge", fullKnowledge);
        return properties;
    }

    public static void pruneDuplicateKnowledge(List<ItemStack> knowledge) {
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

    public static void sendSync(@NotNull EntityPlayerMP player, IKnowledgeProvider provider, boolean resetCache) {
        PETeams.debugLog("Sending knowledge from IKnowledgeProvider to {} ({})", player.getName(), player.getUniqueID());
        NBTTagCompound nbt = ProviderUtil.serializeHelper(
                provider.getEmc(),
                provider.getKnowledge(),
                provider.getInputAndLocks(),
                provider.hasFullKnowledge()
        );
        nbt.setBoolean("resetCache", resetCache);
        PacketHandler.sendTo(new KnowledgeSyncPKT(nbt), player);
    }

    public static void deserializeHelper(@NotNull IKnowledgeHolder holder, @NotNull NBTTagCompound nbt) {
        holder.setEmcRaw(nbt.getLong("transmutationEmc"));
        List<ItemStack> knowledge = holder.getKnowledgeMut();

        NBTTagList list = nbt.getTagList("knowledge", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack item = new ItemStack(list.getCompoundTagAt(i));
            if (!item.isEmpty()) knowledge.add(item);
        }

        ProviderUtil.pruneStaleKnowledge(knowledge);
        ProviderUtil.pruneDuplicateKnowledge(knowledge);

        IItemHandlerModifiable inputLocks = holder.getInputAndLocksMut();
        for (int i = 0; i < inputLocks.getSlots(); i++) {
            inputLocks.setStackInSlot(i, ItemStack.EMPTY);
        }

        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(inputLocks, null, nbt.getTagList("inputlock", Constants.NBT.TAG_COMPOUND));
        holder.setFullKnowledgeRaw(nbt.getBoolean("fullknowledge"));
    }
}
