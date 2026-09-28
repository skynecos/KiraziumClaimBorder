package com.kirazium.claimborder;

import fr.xyness.SCS.API.SimpleClaimSystemAPI;
import fr.xyness.SCS.API.SimpleClaimSystemAPI_Provider;
import org.bukkit.plugin.java.JavaPlugin;

public final class KiraziumClaimBorder extends JavaPlugin {

    private BorderManager borderManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

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
