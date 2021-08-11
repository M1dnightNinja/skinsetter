package me.m1dnightninja.skinsetter.fabric;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.MidnightCoreModInitializer;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.event.PlayerDisconnectEvent;
import me.m1dnightninja.midnightcore.fabric.event.PlayerJoinedEvent;
import me.m1dnightninja.skinsetter.common.SkinSetterImpl;
import me.m1dnightninja.skinsetter.common.core.SkinManagerImpl;
import me.m1dnightninja.skinsetter.common.util.SkinUtil;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.io.File;
import java.nio.file.Paths;

public class SkinSetter implements MidnightCoreModInitializer {

    @Override
    public void onInitialize() { }

    @Override
    public void onAPICreated(MidnightCore midnightCore, MidnightCoreAPI midnightCoreAPI) {

        File configFolder = Paths.get("config", "SkinSetter").toFile();

        if(!configFolder.exists() && !configFolder.mkdirs()) {
            MidnightCoreAPI.getLogger().warn("Unable to create config file for SkinSetter!");
        }

        if(!MidnightCoreAPI.getInstance().areAllModulesLoaded("midnightcore:skin", "midnightcore:lang", "midnightcore:player_data")) {

            throw new IllegalStateException("Unable to enable SkinSetter, one or more required MidnightCore modules are missing!");
        }

        ConfigSection sec = new JsonConfigProvider().loadFromStream(getClass().getResourceAsStream("/assets/skinsetter/lang/en_us.json"));
        ConfigSection cfg = new JsonConfigProvider().loadFromStream(getClass().getResourceAsStream("/assets/skinsetter/config.json"));

        new SkinSetterImpl(configFolder, sec, cfg, new SkinManagerImpl());

        SkinUtil util = new SkinUtil();

        CommandRegistrationCallback.EVENT.register(((commandDispatcher, b) -> new SkinCommand(util).register(commandDispatcher)));
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> util.saveSkins());

        Event.register(PlayerJoinedEvent.class, this, event -> util.applyLoginSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUUID())));
        Event.register(PlayerDisconnectEvent.class, this, 10, event -> util.savePersistentSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUUID())));

    }
}
