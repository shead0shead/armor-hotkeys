# Armor Hotkeys Mod

A Minecraft Fabric mod that adds convenient hotkeys for quickly managing your armor and elytra. Perfect for players who frequently switch between combat and exploration setups.

<p align="center">
  <img src="https://github.com/user-attachments/assets/15dc17d7-8f2d-49c1-be8d-78e9cbc12f22" alt="Armor Hotkeys Demo" style="width: 300%; max-width: 2000px; border-radius: 8px;" />
</p>

## Features

### Quick Armor Management
- **Equip All Armor** (`Z`) - Automatically equip all available armor pieces from your inventory
- **Unequip All** (`B`) - Remove all armor and store it in your inventory
- **Swap Chest** (`X`) - Toggle between chestplate and elytra in the chest slot

### Elytra Specialized Functions
- **Elytra Only** (`C`) - Equip elytra while removing all other armor
- **Elytra + Armor** (`V`) - Equip elytra with helmet, leggings, and boots

### Smart Inventory Management
- **Memory System**: Remembers where your armor came from for seamless re-equipping
- **Direct Swapping**: Efficient item exchange without temporary slot usage
- **Inventory Protection**: Prevents item loss and handles full inventory gracefully

## Key Bindings

<p align="center">
  <img src="https://github.com/user-attachments/assets/e34868b3-03b9-4334-a282-cd0e6f1e4a23" alt="Key Bindings Screenshot" style="width: 100%; max-width: 800px; border-radius: 8px;" />
</p>

| Key | Function | Description |
|-----|----------|-------------|
| `Z` | Equip All | Equips all available armor pieces |
| `X` | Swap Chest | Switches between chestplate and elytra |
| `C` | Elytra Only | Equips elytra (removes other armor) |
| `V` | Elytra + Armor | Equips elytra with other armor pieces |
| `B` | Unequip All | Removes all armor |

## Installation

### Prerequisites
- **Minecraft Version**: 1.21.1
- **Fabric Loader**: ≥ 0.18.4
- **Fabric API**: ≥ 0.140.2

### Installation Steps
1. **Install Fabric Loader**
   - Download and install [Fabric Loader 0.18.4](https://fabricmc.net/use/) for Minecraft 1.21.1

2. **Install Fabric API**
   - Download [Fabric API 0.140.2+1.21.1](https://www.curseforge.com/minecraft/mc-mods/fabric-api) for Minecraft 1.21.1

3. **Install Armor Hotkeys**
   - Download the latest `SheadsArmorHotkeys-1.0.1.jar` from [Releases](https://github.com/shead0shead/armor-hotkeys/releases)
   - Place it in your `.minecraft/mods` folder

## How to Use

### Basic Usage
1. **Equipping Armor**: Press `Z` to automatically equip all armor pieces from your inventory
2. **Switching to Elytra**: Press `X` to swap your chestplate for elytra (and vice versa)
3. **Quick Elytra Setup**: Press `C` for just elytra, or `V` for elytra with armor
4. **Removing Armor**: Press `B` to unequip all armor pieces

### Advanced Features
- **Inventory Memory**: The mod remembers where each armor piece was stored
- **Smart Swapping**: Uses direct exchanges to minimize cursor item movement
- **Error Handling**: Shows messages when items are missing or inventory is full

## Configuration

Key bindings can be changed in Minecraft's standard Controls menu:
1. Open Minecraft Settings
2. Go to **Controls**
3. Scroll to **"Shead's Armor Hotkeys"** category
4. Rebind keys as desired

## Compatibility

- **Minecraft Version**: 1.21.1 (Fabric)
- **Mod Type**: Client-side only - no server installation required
- **Mod Compatibility**: Compatible with most inventory and armor mods

## Requirements

- **Minecraft**: 1.21.1
- **Fabric Loader**: ≥ 0.18.4
- **Fabric API**: ≥ 0.140.2

## FAQ

### How do I change the key bindings?
Open Minecraft's Controls settings, scroll to the "Shead's Armor Hotkeys" category, and rebind the keys as desired.

### Is the mod client-side or server-side?
The mod is client-side only. You don't need to install it on the server. However, you must have Fabric Loader and Fabric API installed on your client.

### Why isn't the mod responding to my key presses?
1. Make sure you are pressing the correct keys (default: Z, X, C, V, B).
2. Check if you have rebound the keys in the controls settings.
3. Ensure you are not in an inventory or other GUI that might be capturing the input.
4. Verify that you have the required Fabric API installed.

### Why isn't my armor equipping?
1. Make sure you have the appropriate armor in your inventory.
2. Check if your inventory is full. The mod needs empty slots to move items around.
3. Ensure you're not in creative mode as inventory management works differently there.

### Does the mod work with other inventory management mods?
The mod uses standard Minecraft inventory APIs and should be compatible with most mods. However, if you experience conflicts with other inventory-modifying mods, try adjusting load order or report the issue.

### Can I use this mod on servers?
Yes! This is a client-side mod, so it works on any server (including vanilla servers) as long as you have it installed on your client. No server-side installation is required.

### What happens if my inventory is full when trying to unequip armor?
The mod will show an error message in chat: "No inventory space available". You'll need to free up at least one slot before unequipping armor.

### Does the mod remember where my armor was stored?
Yes! The mod has a memory system that tracks where each armor piece came from. When you re-equip, it will try to use the same slots.

## Reporting Issues & Suggestions

Found a bug or have a feature request? Please report it on our [GitHub Issues](https://github.com/shead0shead/armor-hotkeys/issues) page with the following information:
- Minecraft version
- Mod version
- Steps to reproduce the issue
- Any relevant error logs

## License

### MIT License

Copyright © 2024 Egor Konovalov (Shead)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.