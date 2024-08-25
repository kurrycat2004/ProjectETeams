package io.github.kurrycat2004.peteams.util;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MixinUtil {
    public static boolean redirectOwnerEquals(@NotNull UUID playerUUID, Object ownerUUID) {
        // we can't really check if the player is on the same team in the client thread,
        // but in case somehow someone who is not in the same team managed to access the container,
        // the server will handle it
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return true;

        if (playerUUID.equals(ownerUUID)) return true;
        if (!Universe.loaded()) return false;
        ForgePlayer forgeOwner = Universe.get().getPlayer((UUID) ownerUUID);
        if (forgeOwner == null || !forgeOwner.hasTeam()) return false;
        return forgeOwner.team.getOnlineMembers().stream().anyMatch(member -> member.getUniqueID().equals(playerUUID));
    }
}
