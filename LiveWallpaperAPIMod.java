package com.livewallpaper.api.fabric;

import com.livewallpaper.api.WallpaperManager;
import net.fabricmc.api.ClientModInitializer;

public class LiveWallpaperAPIMod implements ClientModInitializer {

    /** Singleton WallpaperManager */
    public static final WallpaperManager MANAGER = new WallpaperManager();

    @Override
    public void onInitializeClient() {
        System.out.println("[LiveWallpaperAPI] Khởi động API Fabric thành công!");
    }
}