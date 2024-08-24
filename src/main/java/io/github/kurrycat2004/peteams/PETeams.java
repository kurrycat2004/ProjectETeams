package io.github.kurrycat2004.peteams;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Tags.MODID,
        version = Tags.VERSION,
        name = Tags.MODNAME,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:forge@[14.23.5.2768,);required-after:projecte@[1.12.2-PE1.4.1,);required-after:ftblib@[5.4.0,);"
        //serverSideOnly = true
)
public class PETeams {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PETeams.LOGGER.info("init");

        PETeams.LOGGER.info("registering server side event busses");
        MinecraftForge.EVENT_BUS.register(new Event());
    }
}
