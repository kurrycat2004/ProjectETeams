package io.github.kurrycat2004.peteams.mixin.forge;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = AttachCapabilitiesEvent.class, remap = false)
public interface AttachCapabilitiesEventMixin {
    @Accessor(value = "caps", remap = false)
    @NotNull Map<ResourceLocation, ICapabilityProvider> getCaps();
}
