package io.github.kurrycat2004.peteams.util;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MixinUtil {
    public static boolean redirectOwnerEquals(@NotNull UUID playerUUID, Object ownerUUID) {
        if (playerUUID.equals(ownerUUID)) return true;
        if (!Universe.loaded()) return false;
        ForgePlayer forgeOwner = Universe.get().getPlayer((UUID) ownerUUID);
        if (forgeOwner == null || !forgeOwner.hasTeam()) return false;
        return forgeOwner.team.getOnlineMembers().stream().anyMatch(member -> member.getUniqueID().equals(playerUUID));
    }
}
