package io.github.kurrycat2004.peteams.mixin.projectex;

import com.latmod.mods.projectex.block.BlockLink;
import com.latmod.mods.projectex.tile.TileLink;
import io.github.kurrycat2004.peteams.data.Team;
import io.github.kurrycat2004.peteams.data.TeamSavedData;
import io.github.kurrycat2004.peteams.util.MixinUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

@Mixin(value = BlockLink.class)
public class BlockLinkMixin {
    @Redirect(method = "onBlockActivated",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/UUID;equals(Ljava/lang/Object;)Z"))
    private boolean redirectOwnerEquals(@NotNull UUID playerUUID, Object ownerUUID) {
        return MixinUtil.redirectOwnerEquals(playerUUID, ownerUUID);
    }

    @Inject(method = "onBlockActivated",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;sendStatusMessage(Lnet/minecraft/util/text/ITextComponent;Z)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectStatusMessage(World world, BlockPos pos, IBlockState state, @NotNull EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir,
                                     TileEntity tileEntity) {
        Team commonTeam = TeamSavedData.getCommonTeam(player.getUniqueID(), ((TileLink) tileEntity).owner);

        String statusTranslationKey =
                commonTeam == null ?
                "peteams.block_link.gui_fail.different_team" :
                "peteams.block_link.gui_fail.share_disabled";

        player.sendStatusMessage(
                new TextComponentTranslation(statusTranslationKey)
                        .setStyle(new Style().setColor(TextFormatting.RED)),
                false);
    }
}
