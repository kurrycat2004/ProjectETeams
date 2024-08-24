package io.github.kurrycat2004.peteams.mixin;

import io.github.kurrycat2004.peteams.PETeams;
import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Arrays;
import java.util.List;

public class MixinLoader implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        //noinspection ArraysAsListWithZeroOrOneArgument
        return Arrays.asList("mixins.peteams.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return Loader.isModLoaded(PETeams.PROJECT_EX);
    }
}
