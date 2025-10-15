<div align="center">
  <img src="assets/easytpa.png" alt="EasyTPA Logo" width="200">
  <h1>EasyTPA</h1>
  <p><strong>Simple, powerful teleport requests for Minecraft servers</strong></p>
  
  [![Minecraft](https://img.shields.io/badge/Minecraft-1.19.4--1.21.4-brightgreen)](https://www.minecraft.net/)
  [![License](https://img.shields.io/badge/License-MIT-blue.svg)](license)
  [![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.java.com/)
</div>

## Features

- **Simple Commands** - `/tpa`, `/tpaccept`, `/tpdeny` and more
- **Interactive UI** - Clickable accept/deny buttons with hover text
- **Sound Effects** - Audio feedback for requests and teleportation
- **Customizable** - Fully configurable messages and settings
- **Performance** - Lightweight with minimal resource usage
- **Flexible** - Permission-based access with bypass options
- **Reliable** - Request timeout and cooldown systems
- **Multi-version** - Support for Minecraft 1.19.4 through 1.21.4

## Commands

| Command              | Description                     | Permission         |
| -------------------- | ------------------------------- | ------------------ |
| `/tpa <player>`      | Send a teleport request         | `easytpa.tpa`      |
| `/tpaccept [player]` | Accept a teleport request       | `easytpa.tpaccept` |
| `/tpdeny [player]`   | Deny a teleport request         | `easytpa.tpdeny`   |
| `/tptoggle`          | Toggle teleport requests on/off | `easytpa.toggle`   |
| `/easytpa reload`    | Reload the plugin configuration | `easytpa.admin`    |

## Permissions

### Basic Permissions

- `easytpa.tpa` - Send teleport requests (default: true)
- `easytpa.tpaccept` - Accept teleport requests (default: true)
- `easytpa.tpdeny` - Deny teleport requests (default: true)
- `easytpa.toggle` - Toggle teleport requests (default: true)

### Advanced Permissions

- `easytpa.admin` - Access admin commands (default: op)
- `easytpa.bypass` - Bypass disabled teleport requests (default: op)
- `easytpa.cooldown.bypass` - Bypass cooldown timers (default: op)

### Permission Groups

- `easytpa.*` - All EasyTPA permissions (default: op)

## Configuration

```yaml
# Time settings
settings:
  request-timeout: 60 # Time in seconds before a request expires
  cooldown: 30 # Time in seconds between teleport requests
  enable-sounds: true # Play sounds for teleport events

# All messages are customizable
messages:
  prefix: "&7[&6EasyTPA&7] &r"
  # ... and many more messages
```

## Building from Source

```bash
git clone https://github.com/Membercat-Studios/easytpa.git

# build a shaded jar with gradle
./gradlew shadowJar

# find the bundled jarfile in build/libs/
```

## Support

If you encounter issues or have suggestions, please [open an issue](https://github.com/Membercat-Studios/easytpa/issues) with:

- Server version
- Plugin version
- Java version
- Error messages (if any)
- Steps to reproduce

## License

This project is licensed under the MIT License - see the [LICENSE](license) file for details.

## Contributing

Contributions are welcome! Feel free to submit pull requests or open issues for bugs and feature requests.

---

Made with ❤️ by [maybeizen](https://maybeizen.space)
