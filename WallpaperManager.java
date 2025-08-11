package com.livewallpaper.api;

import com.livewallpaper.api.cache.WallpaperCache;
import com.livewallpaper.api.util.ImageUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý tất cả wallpaper đã đăng ký và xử lý logic phát/render.
 * 
 * - Giữ danh sách các WallpaperInstance đang hoạt động.
 * - Điều khiển play/pause/seek/loop cho từng instance.
 * - Render frame hiện tại ra màn hình.
 */
public class WallpaperManager implements LiveWallpaperAPI {

    /** Danh sách wallpaper đã đăng ký (theo ID) */
    private final Map<String, WallpaperInstance> wallpapers = new ConcurrentHashMap<>();

    /** Cache toàn cục cho frames (shared giữa các wallpaper) */
    private final WallpaperCache cache = new WallpaperCache(10); // giữ 10 frame gần nhất

    @Override
    public void registerWallpaper(WallpaperDescriptor desc) {
        if (isRegistered(desc.id)) {
            throw new IllegalArgumentException("Wallpaper ID '" + desc.id + "' đã được đăng ký.");
        }
        wallpapers.put(desc.id, new WallpaperInstance(desc, cache));
    }

    @Override
    public void unregisterWallpaper(String id) {
        WallpaperInstance inst = wallpapers.remove(id);
        if (inst != null) {
            inst.dispose();
        }
    }

    @Override
    public void registerWallpaper(String id, Path framesFolder, int fps, int frames, int width, int height, boolean loop) {
        registerWallpaper(new WallpaperDescriptor(id, framesFolder, fps, frames, width, height, loop));
    }

    @Override
    public boolean isRegistered(String id) {
        return wallpapers.containsKey(id);
    }

    @Override
    public void play(String id) {
        WallpaperInstance inst = wallpapers.get(id);
        if (inst != null) inst.play();
    }

    @Override
    public void pause(String id) {
        WallpaperInstance inst = wallpapers.get(id);
        if (inst != null) inst.pause();
    }

    @Override
    public void stop(String id) {
        WallpaperInstance inst = wallpapers.get(id);
        if (inst != null) inst.stop();
    }

    @Override
    public void seek(String id, double seconds) {
        WallpaperInstance inst = wallpapers.get(id);
        if (inst != null) inst.seek(seconds);
    }

    @Override
    public double getPosition(String id) {
        WallpaperInstance inst = wallpapers.get(id);
        return (inst != null) ? inst.getPosition() : 0;
    }

    @Override
    public boolean isPlaying(String id) {
        WallpaperInstance inst = wallpapers.get(id);
        return inst != null && inst.isPlaying();
    }

    @Override
    public void setLoop(String id, boolean loop) {
        WallpaperInstance inst = wallpapers.get(id);
        if (inst != null) inst.setLoop(loop);
    }

    @Override
    public boolean isLooping(String id) {
        WallpaperInstance inst = wallpapers.get(id);
        return inst != null && inst.isLoop();
    }

    @Override
    public void setFPS(String id, int fps) {
        WallpaperInstance inst = wallpapers.get(id);
        if (inst != null) inst.setFPS(fps);
    }

    @Override
    public int getFPS(String id) {
        WallpaperInstance inst = wallpapers.get(id);
        return (inst != null) ? inst.getFPS() : 0;
    }

    @Override
    public void setOpacity(String id, float opacity) {
        WallpaperInstance inst = wallpapers.get(id);
        if (inst != null) inst.setOpacity(opacity);
    }

    @Override
    public float getOpacity(String id) {
        WallpaperInstance inst = wallpapers.get(id);
        return (inst != null) ? inst.getOpacity() : 1.0f;
    }

    @Override
    public void render(String id, int x, int y, int width, int height) {
        WallpaperInstance inst = wallpapers.get(id);
        if (inst != null) {
            inst.update(); // cập nhật frame nếu cần
            BufferedImage frame = cache.getFrame(inst.getCurrentFramePath());

            if (frame == null) {
                // Nếu chưa cache → load từ file
                frame = ImageUtils.loadImage(inst.getCurrentFramePath());
                cache.putFrame(inst.getCurrentFramePath(), frame);
            }

            if (frame != null) {
                drawImage(frame, x, y, width, height, inst.getOpacity());
            }
        }
    }

    /** Vẽ BufferedImage ra màn hình bằng OpenGL */
    private void drawImage(BufferedImage img, int x, int y, int width, int height, float opacity) {
        int w = img.getWidth();
        int h = img.getHeight();

        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);

        ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4);
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int pixel = pixels[py * w + px];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
                buffer.put((byte) (pixel & 0xFF));         // B
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
            }
        }
        buffer.flip();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        GL11.glColor4f(1f, 1f, 1f, opacity);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(1, 0); GL11.glVertex2f(x + width, y);
        GL11.glTexCoord2f(1, 1); GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord2f(0, 1); GL11.glVertex2f(x, y + height);
        GL11.glEnd();

        GL11.glDeleteTextures(textureId);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }
}