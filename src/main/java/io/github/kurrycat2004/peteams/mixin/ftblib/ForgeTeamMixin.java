package io.github.kurrycat2004.peteams.mixin.ftblib;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.TeamType;
import com.feed_the_beast.ftblib.lib.data.Universe;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ForgeTeam.class, remap = false)
public class ForgeTeamMixin {
    @Unique
    private String peteams$uuidString;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void peteams$onInit(Universe u, short id, String n, TeamType t, CallbackInfo ci) {
        this.peteams$uuidString = String.format("%04X", id);
    }

    /**
     * @author kurrycat
     * @reason Memoize the result of getUIDCode, String.format is slooow
     */
    @Overwrite
    public final String getUIDCode() {
        return peteams$uuidString;
    }
}
