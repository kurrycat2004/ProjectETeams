package io.github.kurrycat2004.peteams.config;

import io.github.kurrycat2004.peteams.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Tags.MODID)
@Mod.EventBusSubscriber(modid = Tags.MODID)
public class PETeamsConfig {
    @Config.LangKey("peteams.config.default_behaviour.title")
    @Config.Comment("These options require a server restart on dedicated servers\n" +
                    "Only newly created teams will have these values as defaults. Existing teams will not be affected.")
    public static final DefaultBehaviourCategory DEFAULT_BEHAVIOUR = new DefaultBehaviourCategory();

    public static class DefaultBehaviourCategory {
        @Config.LangKey("peteams.config.default_behaviour.default_share_emc")
        public boolean defaultShareEmc = false;

        @Config.LangKey("peteams.config.default_behaviour.default_share_knowledge")
        public boolean defaultShareKnowledge = false;
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (!event.getModID().equals(Tags.MODID)) return;
        ConfigManager.sync(Tags.MODID, Config.Type.INSTANCE);
    }
}
