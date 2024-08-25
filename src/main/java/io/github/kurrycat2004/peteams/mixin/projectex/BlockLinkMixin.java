package io.github.kurrycat2004.peteams.mixin.projectex;

import com.latmod.mods.projectex.block.BlockLink;
import io.github.kurrycat2004.peteams.util.MixinUtil;
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
        return MixinUtil.redirectOwnerEquals(playerUUID, ownerUUID);
    }
}
