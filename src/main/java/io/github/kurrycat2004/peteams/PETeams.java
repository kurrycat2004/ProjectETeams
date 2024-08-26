package io.github.kurrycat2004.peteams;

import io.github.kurrycat2004.peteams.event.Event;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Tags.MODID,
        version = Tags.VERSION,
        name = Tags.MODNAME,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:forge@[14.23.5.2768,);" +
                       "required-after:projecte@[1.12.2-PE1.4.1,);" +
                       "required-after:ftblib@[5.4.0,);" +
                       "after:projectex;"
        //serverSideOnly = true
)
public class PETeams {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);
    public static boolean DEV_ENV = false;

    public static final String PROJECT_EX = "projectex";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        DEV_ENV = ((Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"));
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

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PETeams.LOGGER.info("init");

        PETeams.debugLog("registering server side event handler");
        MinecraftForge.EVENT_BUS.register(new Event());
    }
}
