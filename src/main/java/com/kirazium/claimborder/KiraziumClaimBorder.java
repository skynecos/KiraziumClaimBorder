package com.kirazium.claimborder;

import fr.xyness.SCS.API.SimpleClaimSystemAPI;
import fr.xyness.SCS.API.SimpleClaimSystemAPI_Provider;
import org.bukkit.plugin.java.JavaPlugin;

public final class KiraziumClaimBorder extends JavaPlugin {

    private BorderManager borderManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        int configVersion = getConfig().getInt("config-version", 1);

        // v1.0.1 migration: existing v1.0.0 configs used a 4-block wall.
        if (configVersion < 2) {
            getConfig().set("wall.height", 16.0D);
            configVersion = 2;
        }

        // v1.0.2 migration: split one border material into own/empty/other colors.
        if (configVersion < 3) {
            final String previousMaterial = getConfig().getString("wall.material", "PURPLE_STAINED_GLASS");
            getConfig().set("wall.materials.own", previousMaterial);
            getConfig().set("wall.materials.empty", "WHITE_STAINED_GLASS");
            getConfig().set("wall.materials.other", "RED_STAINED_GLASS");
            getConfig().set("wall.material", null);
            configVersion = 3;
        }

        if (getConfig().getInt("config-version", 1) != configVersion) {
            getConfig().set("config-version", configVersion);
            saveConfig();
        }

        final SimpleClaimSystemAPI api;
        try {
            api = SimpleClaimSystemAPI_Provider.getAPI();
        } catch (IllegalStateException exception) {
            getLogger().severe("SimpleClaimSystem API is not initialized. Disabling KiraziumClaimBorder.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        borderManager = new BorderManager(this, api);
        getServer().getPluginManager().registerEvents(new ClaimSeeListener(borderManager), this);

        getLogger().info("KiraziumClaimBorder enabled.");
    }

    @Override
    public void onDisable() {
        if (borderManager != null) {
            borderManager.clearAll();
        }
    }
}
