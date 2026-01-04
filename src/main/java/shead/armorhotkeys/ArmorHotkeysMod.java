package shead.armorhotkeys;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shead.armorhotkeys.config.ArmorHotkeysConfig;

public class ArmorHotkeysMod implements ModInitializer {
    public static final String MOD_ID = "sheadarmorhotkeys";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ArmorHotkeysConfig config;

    @Override
    public void onInitialize() {
        AutoConfig.register(ArmorHotkeysConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ArmorHotkeysConfig.class).getConfig();

        LOGGER.info("Shead`s Armor Hotkeys initialized!");
    }
}