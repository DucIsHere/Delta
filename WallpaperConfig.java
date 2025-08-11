package com.livewallpaper.api.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WallpaperConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config/livewallpapers/config.json");

    public String activeWallpaper = "elaina";
    public int fps = 60;
    public float opacity = 1.0f;
    public boolean loop = true;

    public static WallpaperConfig load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                return GSON.fromJson(Files.readString(CONFIG_PATH), WallpaperConfig.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new WallpaperConfig();
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}