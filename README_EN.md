# BellCommand - Command Item Plugin

[中文版](README.md)

## Version Info
Current Version: 1.2.0-beta.1

This is a beta version with the following features and known issues:
- ✅ Complete command item system
- ✅ Multi-language support (Simplified Chinese, Traditional Chinese, English, Japanese, French)
- ✅ Bedrock player support (requires Floodgate)
- ✅ Auto-give and cleanup features
- ⚠️ Known performance issues, will be optimized in future versions
- ⚠️ Recommended to test thoroughly in a test environment before production use

## Introduction
BellCommand is a powerful Minecraft plugin that allows server administrators to create custom items that can execute commands. Players can trigger preset commands by left-clicking or right-clicking these items.

## Features
- Custom item appearance and name
- Different commands for left and right clicks
- Complete permission control system
- Command cooldown settings
- Multi-language support
- Debug mode
- Real-time configuration reload
- Automatic update checking

## Commands
- `/bc give <player> <itemID> [amount]` - Give command items
- `/bc list` - View available command items
- `/bc reload` - Reload plugin configuration

## Permissions
- `bellcommand.command` - Allow using basic commands
- `bellcommand.reload` - Allow configuration reload
- `bellcommand.give` - Allow giving items
- `bellcommand.list` - Allow viewing item list
- `bellcommand.use` - Allow using command items
- `bellcommand.item.<itemID>` - Allow using specific items

## Installation
1. Download the latest BellCommand.jar
2. Place the file in your server's plugins folder
3. Restart server or reload plugin
4. Edit configuration file
5. Use `/bc reload` to reload configuration

## Support & Feedback
- GitHub Issues: [Issue Tracker](https://github.com/ning-g-mo/BellCommand/issues)
- MineBBS: [Plugin Page](https://www.minebbs.com/)

## Special Thanks
- [cursor](https://www.cursor.com/)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [leafMC](https://github.com/Winds-Studio/Leaf) 