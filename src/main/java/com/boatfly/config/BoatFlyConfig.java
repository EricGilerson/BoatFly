package com.boatfly.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BoatFlyConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("BoatFly");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public Flight flight = new Flight();
    public Speed speed = new Speed();
    public Keybinds keybinds = new Keybinds();
    public Command command = new Command();

    public static class Flight {
        public boolean enabled = true;
    }

    public static class Speed {
        public boolean allowPlayerChange = true;
        public double defaultSpeed = 8.0;
        public double max = -1;
        public double min = 0.0;
    }

    public static class Keybinds {
        public boolean flightToggle = true;
        public boolean speedUp = true;
        public boolean speedDown = true;
    }

    public static class Command {
        public boolean boatspeedEnabled = true;
    }

    public static BoatFlyConfig load(Path configDir) {
        Path configFile = configDir.resolve("boatfly.json");

        if (!Files.exists(configFile)) {
            BoatFlyConfig defaults = new BoatFlyConfig();
            save(configDir, defaults);
            return defaults;
        }

        try {
            String json = Files.readString(configFile);
            BoatFlyConfig config = GSON.fromJson(json, BoatFlyConfig.class);
            if (config == null) {
                LOGGER.warn("BoatFly config was empty, using defaults");
                return new BoatFlyConfig();
            }
            return config;
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.warn("Failed to load BoatFly config, using defaults: {}", e.getMessage());
            return new BoatFlyConfig();
        }
    }

    public static void save(Path configDir, BoatFlyConfig config) {
        Path configFile = configDir.resolve("boatfly.json");
        try {
            Files.createDirectories(configDir);
            Files.writeString(configFile, GSON.toJson(config));
        } catch (IOException e) {
            LOGGER.error("Failed to save BoatFly config: {}", e.getMessage());
        }
    }
}
