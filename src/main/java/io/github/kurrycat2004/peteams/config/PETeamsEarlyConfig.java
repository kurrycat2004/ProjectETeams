package io.github.kurrycat2004.peteams.config;

import io.github.kurrycat2004.peteams.Tags;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

public class PETeamsEarlyConfig {
    private static final Logger LOGGER = LogManager.getLogger(Tags.MODID);
    private static final String PROPERTIES_FILE_NAME = "peteams-early.properties";

    public static final boolean memoizeFtbLibTeamUIDGetter;

    static {
        Properties properties = loadEarlyConfig();
        memoizeFtbLibTeamUIDGetter = Boolean.parseBoolean(properties.getProperty("memoizeFtbLibTeamUIDGetter", "true"));
    }

    private static Properties loadEarlyConfig() {
        Path propertiesFile = Loader.instance().getConfigDir().toPath().resolve("peteams-early.properties");
        Properties properties = new Properties();
        if (!Files.exists(propertiesFile)) {
            try {
                Files.write(propertiesFile, Arrays.asList(
                        "# PETeams Early Config Properties",
                        "# Caches the result of FTB Lib's team UID getter.",
                        "# This has a pretty big impact, so do not turn it off unless it causes compatibility issues",
                        "memoizeFtbLibTeamUIDGetter=true"
                ));
            } catch (IOException e) {
                LOGGER.warn("Failed to create {}, using default config values", PROPERTIES_FILE_NAME, e);
            }
            return properties;
        }
        try {
            properties.load(Files.newInputStream(propertiesFile));
        } catch (Exception e) {
            LOGGER.warn("Failed to load {}, using default config values", PROPERTIES_FILE_NAME, e);
        }

        return properties;
    }
}
