package shead.armorhotkeys.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import shead.armorhotkeys.ArmorHotkeysMod;

@Config(name = ArmorHotkeysMod.MOD_ID)
public class ArmorHotkeysConfig implements ConfigData {

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.NoTooltip
    public boolean enableSuccessSound = true;

    @ConfigEntry.Gui.NoTooltip
    public boolean enableErrorSound = true;

    @ConfigEntry.Gui.NoTooltip
    public boolean showNotifications = true;

    @ConfigEntry.Gui.NoTooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int soundVolume = 80;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.NoTooltip
    public boolean enableMemorySystem = true;

    @ConfigEntry.Gui.NoTooltip
    public boolean requireEmptyCursor = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.RequiresRestart
    public boolean enableEquipAllKey = true;

    @ConfigEntry.Gui.RequiresRestart
    public boolean enableUnequipAllKey = true;

    @ConfigEntry.Gui.RequiresRestart
    public boolean enableSwapChestKey = true;

    @ConfigEntry.Gui.RequiresRestart
    public boolean enableElytraOnlyKey = true;

    @ConfigEntry.Gui.RequiresRestart
    public boolean enableElytraArmorKey = true;

    public float getSoundVolumeFloat() {
        return soundVolume / 100.0f;
    }
}