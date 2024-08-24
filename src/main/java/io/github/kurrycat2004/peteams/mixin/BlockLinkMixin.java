package io.github.kurrycat2004.peteams.mixin;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.latmod.mods.projectex.block.BlockLink;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(value = BlockLink.class)
public class BlockLinkMixin {
    @Redirect(method = "onBlockActivated",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/UUID;equals(Ljava/lang/Object;)Z"))
    private boolean redirectOwnerEquals(@NotNull UUID playerUUID, Object ownerUUID) {
        if (playerUUID.equals(ownerUUID)) return true;
        if (!Universe.loaded()) return false;
        ForgePlayer forgeOwner = Universe.get().getPlayer((UUID) ownerUUID);
        if (forgeOwner == null || !forgeOwner.hasTeam()) return false;
        return forgeOwner.team.getOnlineMembers().stream().anyMatch(member -> member.getUniqueID().equals(playerUUID));
    }
}
