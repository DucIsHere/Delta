package com.livewallpaper.api.cache;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cache LRU cho frames của wallpaper.
 * 
 * Giữ một số lượng frame giới hạn trong bộ nhớ.
 * Khi đầy → xoá frame cũ nhất (ít dùng nhất).
 */
public class WallpaperCache {

    private final int capacity;
    private final Map<Path, BufferedImage> cache;

    public WallpaperCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<Path, BufferedImage>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Path, BufferedImage> eldest) {
                return size() > WallpaperCache.this.capacity;
            }
        };
    }

    public BufferedImage getFrame(Path path) {
        return cache.get(path);
    }

    public void putFrame(Path path, BufferedImage image) {
        cache.put(path, image);
    }

    public boolean hasFrame(Path path) {
        return cache.containsKey(path);
    }

    public void clear() {
        cache.clear();
    }
}