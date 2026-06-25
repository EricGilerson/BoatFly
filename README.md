# Boat Fly

A Fabric mod that lets you fly while in a boat and control boat speed. Works on both client and server — server admins can configure restrictions for all players.

## Features

- **Boat Flight** — Toggle flight while riding a boat (default key: B)
- **Speed Control** — Increase/decrease boat speed with keybinds (default: I to increase, O to decrease)
- **Speed Command** — Use `/boatspeed <value>` to set speed directly (in blocks per second)
- **Server Configuration** — Server admins can restrict features via a config file

## Controls

| Key | Action |
|-----|--------|
| B | Toggle boat flight |
| I | Increase boat speed |
| O | Decrease boat speed |

All keybinds can be changed in the Keybinds menu. The default boat speed is 8 blocks/s.

When you toggle flight on, speed resets to 8 blocks/s. Use the speed keys or `/boatspeed` to adjust from there.

## Installation

Install on the **client** for single-player use. Install on **both client and server** to enable server-side configuration. The same JAR works in both environments.

If the mod is only on the client and the server doesn't have it, all features work unrestricted (vanilla server compatibility).

### Requirements

- Minecraft 26.1.2+
- Fabric Loader 0.19.2+
- Fabric API
- Java 25+

## Server Configuration

When installed on a server, a config file is generated at `config/boatfly.json` on first boot:

```json
{
  "flight": {
    "enabled": true
  },
  "speed": {
    "allowPlayerChange": true,
    "defaultSpeed": 8.0,
    "max": -1,
    "min": 0.0
  },
  "keybinds": {
    "flightToggle": true,
    "speedUp": true,
    "speedDown": true
  },
  "command": {
    "boatspeedEnabled": true
  }
}
```

### Config Options

| Option | Default | Description |
|--------|---------|-------------|
| `flight.enabled` | `true` | Allow boat flight. Set `false` to disable for all players. |
| `speed.allowPlayerChange` | `true` | Allow players to change speed. Set `false` to lock speed at the default. |
| `speed.defaultSpeed` | `8.0` | Speed (blocks/s) applied when a player joins. |
| `speed.max` | `-1` | Maximum speed cap. `-1` means no cap (uncapped). |
| `speed.min` | `0.0` | Minimum speed floor. |
| `keybinds.flightToggle` | `true` | Allow the flight toggle keybind. |
| `keybinds.speedUp` | `true` | Allow the speed-up keybind. |
| `keybinds.speedDown` | `true` | Allow the speed-down keybind. |
| `command.boatspeedEnabled` | `true` | Allow the `/boatspeed` command. |

Changes require a server restart to take effect.

### Example: Fast Boats Only (No Flight)

```json
{
  "flight": { "enabled": false },
  "speed": { "allowPlayerChange": true, "defaultSpeed": 12.0, "max": 30.0, "min": 5.0 },
  "keybinds": { "flightToggle": false, "speedUp": true, "speedDown": true },
  "command": { "boatspeedEnabled": true }
}
```

This disables flight entirely while letting players change speed between 5 and 30 blocks/s, with a default of 12.

## Version History

- **7.0.0** — Server-side configuration, network sync, admin controls. Same JAR for client and server.
- **6.x** — Minecraft 26.1 support, client refactoring.
- **5.0.0+** — Speed in blocks per second (velocity system). `/boatspeed` command added.
- **< 5.0.0** — Legacy multiplier-based speed system.

## Links

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/boat-fly)

## Support

If you encounter any problems, open an issue on the repository.
