package io.github.kurrycat2004.peteams.mixin.projectex;

import com.latmod.mods.projectex.integration.OfflineKnowledgeProvider;
import io.github.kurrycat2004.peteams.PETeams;
import io.github.kurrycat2004.peteams.provider.impls.TeamImpl;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(value = OfflineKnowledgeProvider.class, remap = false)
public abstract class OfflineKnowledgeProviderMixin {
    @Unique
    public TeamImpl peteams$teamImpl;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void redirectOfflineKnowledgeProvider(UUID id, CallbackInfo ci) {
        this.peteams$teamImpl = new TeamImpl(id);
    }

    @Inject(method = "hasFullKnowledge", at = @At("HEAD"), cancellable = true)
    private void injectHasFullKnowledge(CallbackInfoReturnable<Boolean> cir) {
        if (peteams$teamImpl.isShareKnowledge()) cir.setReturnValue(peteams$teamImpl.hasFullKnowledge());
    }

    @Inject(method = "setFullKnowledge", at = @At("HEAD"))
    private void injectSetFullKnowledge(boolean b, CallbackInfo ci) {
        if (peteams$teamImpl.isShareKnowledge()) peteams$teamImpl.setFullKnowledge(b);
    }

    @Inject(method = "clearKnowledge", at = @At("HEAD"))
    private void injectClearKnowledge(CallbackInfo ci) {
        if (peteams$teamImpl.isShareKnowledge()) peteams$teamImpl.clearKnowledge();
    }

    @Inject(method = "hasKnowledge", at = @At("HEAD"), cancellable = true)
    private void injectHasKnowledge(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (peteams$teamImpl.isShareKnowledge()) cir.setReturnValue(peteams$teamImpl.hasKnowledge(stack));
    }

    @Shadow
    public abstract boolean addKnowledge(ItemStack stack);

    @Inject(method = "addKnowledge", at = @At("HEAD"), cancellable = true)
    private void injectAddKnowledge(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        boolean added = false;
        if (peteams$teamImpl.isShareKnowledge()) added = peteams$teamImpl.addKnowledge(stack);
        cir.setReturnValue(addKnowledge(stack) || added);
    }

    @Shadow
    public abstract boolean removeKnowledge(ItemStack stack);

    @Inject(method = "removeKnowledge", at = @At("HEAD"), cancellable = true)
    private void injectRemoveKnowledge(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        boolean removed = false;
        if (peteams$teamImpl.isShareKnowledge()) removed = peteams$teamImpl.removeKnowledge(stack);
        cir.setReturnValue(removeKnowledge(stack) || removed);
    }

    @Inject(method = "getKnowledge", at = @At("HEAD"), cancellable = true)
    private void injectGetKnowledge(CallbackInfoReturnable<List<ItemStack>> cir) {
        if (peteams$teamImpl.isShareKnowledge()) cir.setReturnValue(peteams$teamImpl.getKnowledge());
    }

    @Inject(method = "getEmc", at = @At("HEAD"), cancellable = true)
    private void injectGetEmc(CallbackInfoReturnable<Long> cir) {
        PETeams.debugLog("OfflineKnowledgeProviderMixin#getEmc");
        if (peteams$teamImpl.isShareEmc()) cir.setReturnValue(peteams$teamImpl.getEmc());
    }

    @Inject(method = "setEmc", at = @At("HEAD"), cancellable = true)
    private void injectSetEmc(long l, CallbackInfo ci) {
        if (peteams$teamImpl.isShareEmc()) {
            peteams$teamImpl.setEmc(l);
            ci.cancel();
        }
    }
}
