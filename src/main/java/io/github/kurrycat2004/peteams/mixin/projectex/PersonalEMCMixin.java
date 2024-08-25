package io.github.kurrycat2004.peteams.mixin.projectex;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.latmod.mods.projectex.integration.PersonalEMC;
import io.github.kurrycat2004.peteams.data.TeamKnowledgeData;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = PersonalEMC.class)
public class PersonalEMCMixin {
    @Inject(method = "get(Lnet/minecraft/world/World;Ljava/util/UUID;)Lmoze_intel/projecte/api/capabilities/IKnowledgeProvider;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/management/PlayerList;getPlayerByUUID(Ljava/util/UUID;)Lnet/minecraft/entity/player/EntityPlayerMP;"),
            cancellable = true)
    private static void injectOfflineKnowledge(World world, UUID playerUUID, CallbackInfoReturnable<IKnowledgeProvider> cir) {
        if (!Universe.loaded()) return;
        ForgePlayer forgePlayer = Universe.get().getPlayer(playerUUID);
        if (forgePlayer == null || !forgePlayer.hasTeam()) return;
        cir.setReturnValue(
                TeamKnowledgeData.getInstance().getTeam(forgePlayer.team.getUIDCode()).getOfflineProvider()
        );
    }
}
