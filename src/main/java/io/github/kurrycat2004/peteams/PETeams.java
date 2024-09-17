package io.github.kurrycat2004.peteams;

import io.github.kurrycat2004.peteams.net.PETPacketHandler;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(
        modid = Tags.MODID,
        version = Tags.VERSION,
        name = Tags.MODNAME,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:forge@[14.23.5.2768,);" +
                       "required-after:projecte@[1.12.2-PE1.4.1,);" +
                       "required-after:ftblib@[5.4.0,);" +
                       "after:projectex;",
        guiFactory = "io.github.kurrycat2004.peteams.config.PETeamsGuiFactory"
)
public class PETeams {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);
    public static boolean DEV_ENV = false;

    public static final String PROJECT_EX = "projectex";

    public static File configFile;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        DEV_ENV = ((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"));

        PETPacketHandler.register();

        configFile = new File(event.getModConfigurationDirectory(), Tags.MODID + ".cfg");
    }

    public static void debugLog(String msg, Object... args) {
        if (DEV_ENV) LOGGER.info(msg, args);
        else LOGGER.debug(msg, args);
    }

    public static void logStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder("Stack trace:\n");
        for (int i = 2; i < stackTrace.length; i++) {
            sb.append("\tat ")
                    .append(stackTrace[i])
                    .append("\n");
        }
        debugLog(sb.toString());
    }
}
