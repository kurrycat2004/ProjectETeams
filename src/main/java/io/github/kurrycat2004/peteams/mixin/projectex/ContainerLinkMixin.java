package io.github.kurrycat2004.peteams.mixin.projectex;

import com.latmod.mods.projectex.gui.ContainerLink;
import io.github.kurrycat2004.peteams.util.MixinUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(value = ContainerLink.class)
public class ContainerLinkMixin {
    @Redirect(method = "enchantItem(Lnet/minecraft/entity/player/EntityPlayer;I)Z",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/UUID;equals(Ljava/lang/Object;)Z"))
    private boolean redirectOwnerEquals(@NotNull UUID playerUUID, Object ownerUUID) {
        return MixinUtil.redirectOwnerEquals(playerUUID, ownerUUID);
    }
}
