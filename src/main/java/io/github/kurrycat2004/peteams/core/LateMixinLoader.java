package io.github.kurrycat2004.peteams.core;

import io.github.kurrycat2004.peteams.compat.LoadedMods;
import io.github.kurrycat2004.peteams.config.PETeamsEarlyConfig;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Arrays;
import java.util.List;

public class LateMixinLoader implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList(
                "mixins.mods.projectex.json",
                "mixins.mods.ftblib.json"
        );
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return switch (mixinConfig) {
            case "mixins.mods.projectex.json" -> LoadedMods.PROJECT_EX;
            case "mixins.mods.ftblib.json" -> LoadedMods.FTB_LIB && PETeamsEarlyConfig.memoizeFtbLibTeamUIDGetter;
            default -> false;
        };
    }
}
