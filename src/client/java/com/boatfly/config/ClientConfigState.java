package com.boatfly.config;

import com.boatfly.network.BoatFlyConfigPayload;

public class ClientConfigState {
    private static boolean serverManaged = false;
    private static boolean flightEnabled = true;
    private static boolean allowPlayerSpeedChange = true;
    private static double defaultSpeed = 8.0;
    private static double maxSpeed = -1;
    private static double minSpeed = 0.0;
    private static boolean keybindFlightToggle = true;
    private static boolean keybindSpeedUp = true;
    private static boolean keybindSpeedDown = true;
    private static boolean commandEnabled = true;

    public static void applyFromPayload(BoatFlyConfigPayload payload) {
        serverManaged = true;
        flightEnabled = payload.flightEnabled();
        allowPlayerSpeedChange = payload.allowPlayerSpeedChange();
        defaultSpeed = payload.defaultSpeed();
        maxSpeed = payload.maxSpeed();
        minSpeed = payload.minSpeed();
        keybindFlightToggle = payload.keybindFlightToggle();
        keybindSpeedUp = payload.keybindSpeedUp();
        keybindSpeedDown = payload.keybindSpeedDown();
        commandEnabled = payload.commandEnabled();
    }

    public static void reset() {
        serverManaged = false;
        flightEnabled = true;
        allowPlayerSpeedChange = true;
        defaultSpeed = 8.0;
        maxSpeed = -1;
        minSpeed = 0.0;
        keybindFlightToggle = true;
        keybindSpeedUp = true;
        keybindSpeedDown = true;
        commandEnabled = true;
    }

    public static double clampSpeed(double speed) {
        speed = Math.max(minSpeed, speed);
        if (maxSpeed > 0) {
            speed = Math.min(maxSpeed, speed);
        }
        return speed;
    }

    public static boolean isServerManaged() { return serverManaged; }
    public static boolean isFlightEnabled() { return flightEnabled; }
    public static boolean isAllowPlayerSpeedChange() { return allowPlayerSpeedChange; }
    public static double getDefaultSpeed() { return defaultSpeed; }
    public static double getMaxSpeed() { return maxSpeed; }
    public static double getMinSpeed() { return minSpeed; }
    public static boolean isKeybindFlightToggle() { return keybindFlightToggle; }
    public static boolean isKeybindSpeedUp() { return keybindSpeedUp; }
    public static boolean isKeybindSpeedDown() { return keybindSpeedDown; }
    public static boolean isCommandEnabled() { return commandEnabled; }
}
