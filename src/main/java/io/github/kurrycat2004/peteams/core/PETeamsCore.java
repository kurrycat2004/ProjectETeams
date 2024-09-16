package io.github.kurrycat2004.peteams.core;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.jetbrains.annotations.Nullable;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("PETeamsCore")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class PETeamsCore implements IFMLLoadingPlugin, IEarlyMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return Collections.emptyList();
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return false;
    }

    @Override
    public String[] getASMTransformerClass() {return new String[0];}

    @Override
    public String getModContainerClass() {return null;}

    @Override
    public @Nullable String getSetupClass() {return null;}

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {return null;}
}
