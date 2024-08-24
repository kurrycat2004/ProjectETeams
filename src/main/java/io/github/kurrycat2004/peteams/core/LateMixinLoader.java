package io.github.kurrycat2004.peteams.core;

import io.github.kurrycat2004.peteams.PETeams;
import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Arrays;
import java.util.List;

public class LateMixinLoader implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        //noinspection ArraysAsListWithZeroOrOneArgument
        return Arrays.asList("mixins.mods.projectex.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return switch (mixinConfig) {
            case "mixins.mods.projectex.json" -> Loader.isModLoaded(PETeams.PROJECT_EX);
            default -> false;
        };
    }
}
