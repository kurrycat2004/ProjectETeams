package io.github.kurrycat2004.peteams.util;

import io.github.kurrycat2004.peteams.Tags;
import io.github.kurrycat2004.peteams.config.FTBPETeamsData;
import io.github.kurrycat2004.peteams.data.Team;
import io.github.kurrycat2004.peteams.data.TeamSavedData;
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
        Team commonTeam = TeamSavedData.getCommonTeam(playerUUID, (UUID) ownerUUID);
        if (commonTeam == null) return false;

        FTBPETeamsData teamData = commonTeam.getTeam().getData().get(Tags.MODID);
        return teamData.isShareEmc() && teamData.isShareKnowledge();
    }
}
