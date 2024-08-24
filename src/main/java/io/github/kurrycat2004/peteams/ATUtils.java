package io.github.kurrycat2004.peteams;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import java.lang.reflect.Field;
import java.util.Map;

public class ATUtils {
    private static final Field capsField;

    static {
        Field field;
        try {
            field = AttachCapabilitiesEvent.class.getDeclaredField("caps");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            PETeams.LOGGER.error("Failed to get caps field, the mod will not work as expected", e);
            field = null;
        }
        capsField = field;
    }

    public static Map<ResourceLocation, ICapabilityProvider> caps(AttachCapabilitiesEvent<?> event) {
        if (capsField == null) return null;
        try {
            //noinspection unchecked
            return (Map<ResourceLocation, ICapabilityProvider>) capsField.get(event);
        } catch (IllegalAccessException e) {
            PETeams.LOGGER.error("Failed to get caps field, the mod will not work as expected", e);
            return null;
        }
    }
}
