package com.boatfly;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import java.lang.Math;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;


import static net.minecraft.server.command.CommandManager.argument;

public class BoatFlyClient implements ClientModInitializer {
	public static KeyBinding BoatFlight;
	public static boolean BoatFlyOn;
	public static KeyBinding BoatSpeedInc;
	public static KeyBinding BoatSpeedDec;
	public static double BoatSpeed = 1;
	public static double boatVelocity = 8;
	private static double lastPlayerX;
	private static double lastPlayerZ;
	private static long lastCheckTime;
	private static double playerSpeed;
	private static Vec3d boat;
	private static final MinecraftClient client = MinecraftClient.getInstance();


	// The KeyBinding declaration and registration are commonly executed here statically

	@Override
	public void onInitializeClient() {
		BoatFlight = KeyBindingHelper.registerKeyBinding(new KeyBinding("Boat Fly",  InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "Boat Fly"));
		BoatFlyOn = false;
		BoatSpeedInc = KeyBindingHelper.registerKeyBinding(new KeyBinding("Increase Boat Speed",  InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "Boat Fly"));
		BoatSpeedDec = KeyBindingHelper.registerKeyBinding(new KeyBinding("Decrease Boat Speed",  InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "Boat Fly"));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.isWindowFocused()) { // Ensure the window is focused before rendering
				onRenderTick(client);
			}
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("boatspeed")
					.then(argument("value", FloatArgumentType.floatArg())
							.executes(context -> {
								final double value = Math.round((FloatArgumentType.getFloat(context, "value")) * 1000.0) / 1000.0;
								changeSpeed(MinecraftClient.getInstance(), (float) value, false);
								return (int) value;
							})));
		});
		/*ClientTickEvents.END_CLIENT_TICK.register(client -> {
			updatePlayerSpeed();
		});*/
	}
	private void onRenderTick(MinecraftClient client) {
		if (client.player != null) {
			if (BoatFlight.wasPressed()) {
				BoatFlyOn = !BoatFlyOn;
				System.out.println(BoatFlyOn);
				BoatSpeed = 1;
				if (BoatFlyOn) {
					client.player.sendMessage(Text.of("Boat Fly is now On and Boat Speed is Set to 8 blocks/s"), true);
					changeSpeed(client, 8, true);
				} else {
					client.player.sendMessage(Text.of("Boat Fly is now Off"), true);
				}
			}
			if (BoatSpeedDec.wasPressed()) {
				if(boatVelocity != 0) {
					changeSpeed(client, boatVelocity + -1, false);
				}

			}
			if (BoatSpeedInc.wasPressed()) {
				changeSpeed(client, boatVelocity + 1, false);
			}
			if (client.options.jumpKey.isPressed()) {
				if (BoatFlyClient.BoatFlyOn) {
					if (!client.player.hasVehicle())
						return;
					Entity vehicle = client.player.getVehicle();
					assert vehicle != null;
					Vec3d velocity = vehicle.getVelocity();
					double motionY = client.options.jumpKey.isPressed() ? 0.3 : 0;
					vehicle.setVelocity(new Vec3d(velocity.x, motionY, velocity.z));
				}

			}
			if (client.options.forwardKey.isPressed()) {
				if (BoatFlyClient.BoatSpeed != 1) {
					if (!client.player.hasVehicle())
						return;
					Entity vehicle = client.player.getVehicle();
					assert vehicle != null;
					Vec3d velocity = vehicle.getVelocity();
					boat = new Vec3d(velocity.x * BoatSpeed, velocity.y, velocity.z * BoatSpeed);
					vehicle.setVelocity(boat);
					assert BoatFlyClient.client.player != null;
				}
			}

		}


	}
	public void changeSpeed(MinecraftClient client, double speed, boolean fly){

		BoatSpeed = multiplier(speed);
		double scale = Math.pow(10, 5);
		boatVelocity = Math.round(speed * scale) / scale;
		double BoatSpeedRound = Math.round(BoatSpeed * scale) / scale;
		System.out.println(BoatSpeedRound);
		String StringBoatSpeed = Double.toString(speed);
		assert client.player != null;
		if(!fly) {
			//BoatFlyOn = false;
			client.player.sendMessage(Text.of("Your Boat Speed is now " + StringBoatSpeed + " blocks/s"), true);
		}
	}
	/*private void updatePlayerSpeed() {
		if(client.player != null) {
			Vec3d playerPos = client.player.getPos();
			double playerX = playerPos.x;
			double playerZ = playerPos.z;
			long currentTime = System.currentTimeMillis();
			long timeElapsed = currentTime - lastCheckTime;

			if (timeElapsed > 1000) { // Update speed every 100 milliseconds
				double deltaX = playerX - lastPlayerX;
				double deltaZ = playerZ - lastPlayerZ;
				double distance = MathHelper.sqrt((float) (deltaX * deltaX + deltaZ * deltaZ));
				playerSpeed = distance / (timeElapsed / 1000.0); // Blocks per second
				lastPlayerX = playerX;
				lastPlayerZ = playerZ;
				lastCheckTime = currentTime;

				// Send player speed to chat
				client.player.sendMessage(Text.of(String.format("Speed: %.2f blocks/s", playerSpeed)), true);
			}
		}
	}*/
	private double multiplier(double velocity){
		double finalMultiplier;
		finalMultiplier = Math.pow((-5.33893*Math.pow(Math.log(velocity-8+11.9072),-3.31832)+1.26253),0.470998);
		return finalMultiplier;
	}
}
