# EasyTPA

A simple yet powerful Minecraft teleport request plugin that allows players to request teleportation to other players with interactive features and customizable settings.

## Features

- 🎯 Simple `/tpa`, `/tpaccept`, and `/tpdeny` commands
- 👆 Interactive clickable accept/deny buttons
- 💬 Hover text for better user experience
- 🔊 Sound effects for requests and teleportation
- ⏲️ Configurable cooldown system
- ⚡ Fast and lightweight
- 🎨 Fully customizable messages
- ⏱️ Request timeout system
- 🔒 Permission-based access
- 🔄 Multi-version support (1.19.4 - 1.21.4)

## Version Compatibility

The plugin is compatible with the following Minecraft versions:

- 1.21.x (Latest)
- 1.20.x
- 1.19.4

Each version has its own JAR file in the releases section. Make sure to download the correct version for your server.

## Commands

- `/tpa <player>` - Send a teleport request to a player
- `/tpaccept [player]` - Accept a teleport request
- `/tpdeny [player]` - Deny a teleport request

## Permissions

- `easytpa.tpa` - Allows sending teleport requests
- `easytpa.tpaccept` - Allows accepting teleport requests
- `easytpa.tpdeny` - Allows denying teleport requests

## Configuration

The plugin is highly configurable through the `config.yml` file:

```yaml
# Time settings
settings:
  request-timeout: 60 # Time in seconds before a request expires
  cooldown: 30 # Time in seconds between teleport requests
  enable-sounds: true # Play sounds for teleport events

# All messages are customizable
messages:
  prefix: "&7[&6EasyTPA&7] "
  # ... and many more messages
```

## Installation

1. Download the correct version for your server from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/EasyTPA/config.yml` (optional)

## Building from Source

1. Clone the repository
2. Switch to the branch matching your Minecraft version (e.g., `mc-1.19.4`, `mc-1.20`, `mc-1.21`)
3. Build using Gradle:

```bash
./gradlew build
```

4. Find the built JAR in `build/libs/`

## Dependencies

- Spigot/Paper 1.19.4 - 1.21.4
- Java 17 or higher (for 1.19.4+)

## Support

If you encounter any issues or have suggestions, please open an issue on the GitHub repository. When reporting issues, please include:

- Server version
- Plugin version
- Error messages (if any)
- Steps to reproduce the issue

## License

This project is licensed under the MIT License - see the [LICENSE](license) file for details.

## Contributing

Contributions are welcome! Feel free to submit pull requests or open issues for any bugs or feature requests.

---

Made with ❤️ by maybeizen
