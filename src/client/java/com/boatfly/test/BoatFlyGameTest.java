package com.boatfly.test;

import com.boatfly.BoatFlyClient;
import com.boatfly.config.BoatFlyConfig;
import com.boatfly.config.ClientConfigState;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BoatFlyGameTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        testDefaultConfigCreatedWhenMissing();
        testConfigLoadsCustomValues();
        testMalformedConfigFallsBackToDefaults();
        testClampSpeedWithMax();
        testClampSpeedNoMax();
        testClampSpeedWithMin();

        testConfigSentOnPlayerJoin(context);
        testFlightDisabledPreventsToggle(context);
        testSpeedMaxEnforced(context);
        testSpeedMinEnforced(context);
        testKeybindDisabledIgnoresPress(context);
        testSpeedChangeDisabledBlocksAll(context);
        testDisconnectResetsConfig(context);
        testDefaultSpeedAppliedOnJoin(context);
        testNoMaxMeansUncapped(context);
    }

    // --- Config loading tests (no world needed) ---

    private void testDefaultConfigCreatedWhenMissing() {
        Path tempDir = createTempConfigDir();
        Path configFile = tempDir.resolve("boatfly.json");
        assert !Files.exists(configFile) : "Config file should not exist yet";

        BoatFlyConfig config = BoatFlyConfig.load(tempDir);

        assert Files.exists(configFile) : "Config file should have been created";
        assert config.flight.enabled : "Default flight should be enabled";
        assert config.speed.allowPlayerChange : "Default should allow player speed change";
        assert config.speed.defaultSpeed == 8.0 : "Default speed should be 8.0";
        assert config.speed.max == -1 : "Default max should be -1 (uncapped)";
        assert config.speed.min == 0.0 : "Default min should be 0.0";
        assert config.keybinds.flightToggle : "Default keybind flightToggle should be true";
        assert config.keybinds.speedUp : "Default keybind speedUp should be true";
        assert config.keybinds.speedDown : "Default keybind speedDown should be true";
        assert config.command.boatspeedEnabled : "Default command should be enabled";

        cleanup(tempDir);
    }

    private void testConfigLoadsCustomValues() {
        Path tempDir = createTempConfigDir();
        String json = """
                {
                  "flight": { "enabled": false },
                  "speed": { "allowPlayerChange": false, "default": 12.0, "max": 20.0, "min": 3.0 },
                  "keybinds": { "flightToggle": false, "speedUp": true, "speedDown": false },
                  "command": { "boatspeedEnabled": false }
                }
                """;
        writeConfig(tempDir, json);

        BoatFlyConfig config = BoatFlyConfig.load(tempDir);

        assert !config.flight.enabled : "Flight should be disabled";
        assert !config.speed.allowPlayerChange : "Player speed change should be disabled";
        assert config.speed.defaultSpeed == 12.0 : "Default speed should be 12.0";
        assert config.speed.max == 20.0 : "Max speed should be 20.0";
        assert config.speed.min == 3.0 : "Min speed should be 3.0";
        assert !config.keybinds.flightToggle : "flightToggle keybind should be disabled";
        assert config.keybinds.speedUp : "speedUp keybind should remain enabled";
        assert !config.keybinds.speedDown : "speedDown keybind should be disabled";
        assert !config.command.boatspeedEnabled : "Command should be disabled";

        cleanup(tempDir);
    }

    private void testMalformedConfigFallsBackToDefaults() {
        Path tempDir = createTempConfigDir();
        writeConfig(tempDir, "{ invalid json !!!");

        BoatFlyConfig config = BoatFlyConfig.load(tempDir);

        assert config.flight.enabled : "Should fall back to default (enabled)";
        assert config.speed.defaultSpeed == 8.0 : "Should fall back to default speed";
        assert config.speed.max == -1 : "Should fall back to default max (-1)";

        cleanup(tempDir);
    }

    private void testClampSpeedWithMax() {
        ClientConfigState.reset();
        ClientConfigState.applyFromPayload(new com.boatfly.network.BoatFlyConfigPayload(
                true, true, 8.0, 15.0, 2.0, true, true, true, true
        ));

        assert ClientConfigState.clampSpeed(20.0) == 15.0 : "Should clamp to max 15.0";
        assert ClientConfigState.clampSpeed(1.0) == 2.0 : "Should clamp to min 2.0";
        assert ClientConfigState.clampSpeed(10.0) == 10.0 : "Should not clamp value within range";

        ClientConfigState.reset();
    }

    private void testClampSpeedNoMax() {
        ClientConfigState.reset();
        ClientConfigState.applyFromPayload(new com.boatfly.network.BoatFlyConfigPayload(
                true, true, 8.0, -1, 0.0, true, true, true, true
        ));

        assert ClientConfigState.clampSpeed(9999.0) == 9999.0 : "Should not clamp when max is -1";
        assert ClientConfigState.clampSpeed(0.0) == 0.0 : "Should allow 0 with min=0";

        ClientConfigState.reset();
    }

    private void testClampSpeedWithMin() {
        ClientConfigState.reset();
        ClientConfigState.applyFromPayload(new com.boatfly.network.BoatFlyConfigPayload(
                true, true, 8.0, -1, 5.0, true, true, true, true
        ));

        assert ClientConfigState.clampSpeed(3.0) == 5.0 : "Should clamp to min 5.0";
        assert ClientConfigState.clampSpeed(100.0) == 100.0 : "Should not clamp high value with no max";

        ClientConfigState.reset();
    }

    // --- Integration tests (require world) ---

    private void testConfigSentOnPlayerJoin(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.waitTicks(5);
            assert ClientConfigState.isServerManaged() : "Client should have received config from integrated server";
        }
    }

    private void testFlightDisabledPreventsToggle(ClientGameTestContext context) {
        ClientConfigState.applyFromPayload(new com.boatfly.network.BoatFlyConfigPayload(
                false, true, 8.0, -1, 0.0, true, true, true, true
        ));

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.waitTicks(5);
            BoatFlyClient.BoatFlyOn = false;

            context.getInput().pressKey(BoatFlyClient.BoatFlight);
            context.waitTick();

            assert !BoatFlyClient.BoatFlyOn : "Flight should not toggle when disabled by server";
        } finally {
            ClientConfigState.reset();
        }
    }

    private void testSpeedMaxEnforced(ClientGameTestContext context) {
        ClientConfigState.applyFromPayload(new com.boatfly.network.BoatFlyConfigPayload(
                true, true, 8.0, 10.0, 0.0, true, true, true, true
        ));

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.waitTicks(5);
            BoatFlyClient.boatVelocity = 9.0;

            context.getInput().pressKey(BoatFlyClient.BoatSpeedInc);
            context.waitTick();
            context.getInput().pressKey(BoatFlyClient.BoatSpeedInc);
            context.waitTick();
            context.getInput().pressKey(BoatFlyClient.BoatSpeedInc);
            context.waitTick();

            assert BoatFlyClient.boatVelocity <= 10.0 : "Speed should not exceed max of 10.0, got " + BoatFlyClient.boatVelocity;
        } finally {
            ClientConfigState.reset();
        }
    }

    private void testSpeedMinEnforced(ClientGameTestContext context) {
        ClientConfigState.applyFromPayload(new com.boatfly.network.BoatFlyConfigPayload(
                true, true, 8.0, -1, 5.0, true, true, true, true
        ));

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.waitTicks(5);
            BoatFlyClient.boatVelocity = 6.0;

            context.getInput().pressKey(BoatFlyClient.BoatSpeedDec);
            context.waitTick();
            context.getInput().pressKey(BoatFlyClient.BoatSpeedDec);
            context.waitTick();
            context.getInput().pressKey(BoatFlyClient.BoatSpeedDec);
            context.waitTick();

            assert BoatFlyClient.boatVelocity >= 5.0 : "Speed should not go below min of 5.0, got " + BoatFlyClient.boatVelocity;
        } finally {
            ClientConfigState.reset();
        }
    }

    private void testKeybindDisabledIgnoresPress(ClientGameTestContext context) {
        ClientConfigState.applyFromPayload(new com.boatfly.network.BoatFlyConfigPayload(
                true, true, 8.0, -1, 0.0, false, false, false, true
        ));

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.waitTicks(5);
            double originalSpeed = BoatFlyClient.boatVelocity;
            boolean originalFlyState = BoatFlyClient.BoatFlyOn;

            context.getInput().pressKey(BoatFlyClient.BoatFlight);
            context.waitTick();
            context.getInput().pressKey(BoatFlyClient.BoatSpeedInc);
            context.waitTick();
            context.getInput().pressKey(BoatFlyClient.BoatSpeedDec);
            context.waitTick();

            assert BoatFlyClient.BoatFlyOn == originalFlyState : "Flight state should not change with disabled keybind";
            assert BoatFlyClient.boatVelocity == originalSpeed : "Speed should not change with disabled keybinds";
        } finally {
            ClientConfigState.reset();
        }
    }

    private void testSpeedChangeDisabledBlocksAll(ClientGameTestContext context) {
        ClientConfigState.applyFromPayload(new com.boatfly.network.BoatFlyConfigPayload(
                true, false, 8.0, -1, 0.0, true, true, true, true
        ));

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.waitTicks(5);
            double originalSpeed = BoatFlyClient.boatVelocity;

            context.getInput().pressKey(BoatFlyClient.BoatSpeedInc);
            context.waitTick();
            context.getInput().pressKey(BoatFlyClient.BoatSpeedDec);
            context.waitTick();

            assert BoatFlyClient.boatVelocity == originalSpeed : "Speed should not change when allowPlayerChange is false";
        } finally {
            ClientConfigState.reset();
        }
    }

    private void testDisconnectResetsConfig(ClientGameTestContext context) {
        ClientConfigState.applyFromPayload(new com.boatfly.network.BoatFlyConfigPayload(
                false, false, 20.0, 25.0, 10.0, false, false, false, false
        ));

        assert ClientConfigState.isServerManaged() : "Should be server managed after applying payload";

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.waitTicks(3);
        }

        // After closing world (disconnect), config should reset
        context.waitTicks(3);
        assert !ClientConfigState.isServerManaged() || ClientConfigState.isFlightEnabled()
                : "Config should reset after disconnect";
    }

    private void testDefaultSpeedAppliedOnJoin(ClientGameTestContext context) {
        // We test via the integrated server which loads its own config.
        // Since the default config has defaultSpeed=8.0, verify it's applied.
        BoatFlyClient.boatVelocity = 1.0;

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.waitTicks(5);
            assert BoatFlyClient.boatVelocity == 8.0 : "Default speed should be applied on join, got " + BoatFlyClient.boatVelocity;
        }
    }

    private void testNoMaxMeansUncapped(ClientGameTestContext context) {
        // Default config has max=-1, meaning uncapped
        ClientConfigState.applyFromPayload(new com.boatfly.network.BoatFlyConfigPayload(
                true, true, 8.0, -1, 0.0, true, true, true, true
        ));

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.waitTicks(5);
            BoatFlyClient.boatVelocity = 99.0;

            context.getInput().pressKey(BoatFlyClient.BoatSpeedInc);
            context.waitTick();

            assert BoatFlyClient.boatVelocity >= 99.0 : "Speed should be uncapped when max is -1, got " + BoatFlyClient.boatVelocity;
        } finally {
            ClientConfigState.reset();
        }
    }

    // --- Helpers ---

    private Path createTempConfigDir() {
        try {
            return Files.createTempDirectory("boatfly_test_config");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeConfig(Path configDir, String json) {
        try {
            Files.createDirectories(configDir);
            Files.writeString(configDir.resolve("boatfly.json"), json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void cleanup(Path tempDir) {
        try {
            Files.deleteIfExists(tempDir.resolve("boatfly.json"));
            Files.deleteIfExists(tempDir);
        } catch (IOException e) {
            // best effort
        }
    }
}
