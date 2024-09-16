package io.github.kurrycat2004.peteams.net;

import io.github.kurrycat2004.peteams.Tags;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PETPacketHandler {
    private static final SimpleNetworkWrapper HANDLER = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MODID);

    public static void register() {
        int disc = 0;
        HANDLER.registerMessage(EmcSyncPKT.Handler.class, EmcSyncPKT.class, disc++, Side.CLIENT);
    }

    public static void sendTo(IMessage msg, EntityPlayerMP player) {
        if (!(player instanceof FakePlayer)) {
            HANDLER.sendTo(msg, player);
        }
    }
}
