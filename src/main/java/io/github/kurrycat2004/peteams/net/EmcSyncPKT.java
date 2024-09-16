package io.github.kurrycat2004.peteams.net;

import io.netty.buffer.ByteBuf;
import moze_intel.projecte.PECore;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EmcSyncPKT implements IMessage {
    private NBTTagCompound nbt;

    public EmcSyncPKT() {}

    public EmcSyncPKT(NBTTagCompound nbt) {
        this.nbt = nbt;
    }

    public void fromBytes(ByteBuf buf) {
        this.nbt = ByteBufUtils.readTag(buf);
    }

    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.nbt);
    }

    public static class Handler implements IMessageHandler<EmcSyncPKT, IMessage> {
        public Handler() {}

        public IMessage onMessage(final EmcSyncPKT message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> PECore.proxy.getClientTransmutationProps().deserializeNBT(message.nbt));
            return null;
        }
    }
}

