package com.livewallpaper.api;

import com.livewallpaper.api.cache.WallpaperCache;
import com.livewallpaper.api.util.ImageUtils;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * Đại diện cho một wallpaper đang hoạt động (một instance duy nhất).
 * 
 * - Giữ thông tin từ WallpaperDescriptor.
 * - Tính toán frame hiện tại dựa vào FPS và thời gian trôi qua.
 * - Hỗ trợ play/pause/stop/seek.
 * - Prefetch frame kế tiếp để giảm giật khi render.
 */
public class WallpaperInstance {

    private final WallpaperDescriptor desc;
    private final WallpaperCache cache;

    private boolean playing = false;
    private boolean loop;

    private float opacity = 1.0f;

    private int fps;
    private double frameDuration; // ms/frame
    private int totalFrames;

    private int currentFrameIndex = 0;
    private long lastFrameTime = 0;

    public WallpaperInstance(WallpaperDescriptor desc, WallpaperCache cache) {
        this.desc = desc;
        this.cache = cache;
        this.fps = desc.fps;
        this.frameDuration = 1000.0 / fps;
        this.totalFrames = desc.frames;
        this.loop = desc.loop;
    }

    /** Bắt đầu phát */
    public void play() {
        playing = true;
        lastFrameTime = System.currentTimeMillis();
    }

    /** Tạm dừng phát */
    public void pause() {
        playing = false;
    }

    /** Dừng phát và về frame 0 */
    public void stop() {
        playing = false;
        currentFrameIndex = 0;
    }

    /** Tua tới một thời điểm (giây) */
    public void seek(double seconds) {
        int targetFrame = (int) (seconds * fps);
        if (targetFrame < 0) targetFrame = 0;
        if (targetFrame >= totalFrames) targetFrame = totalFrames - 1;
        currentFrameIndex = targetFrame;
    }

    /** Lấy vị trí hiện tại (giây) */
    public double getPosition() {
        return currentFrameIndex / (double) fps;
    }

    /** Cập nhật frame nếu cần */
    public void update() {
        if (!playing) return;

        long now = System.currentTimeMillis();
        if (now - lastFrameTime >= frameDuration) {
            currentFrameIndex++;
            lastFrameTime = now;

            if (currentFrameIndex >= totalFrames) {
                if (loop) {
                    currentFrameIndex = 0;
                } else {
                    currentFrameIndex = totalFrames - 1;
                    playing = false;
                }
            }

            // Prefetch frame tiếp theo để tránh lag
            int nextFrame = (currentFrameIndex + 1) % totalFrames;
            Path nextPath = getFramePath(nextFrame);
            if (!cache.hasFrame(nextPath)) {
                BufferedImage img = ImageUtils.loadImage(nextPath);
                if (img != null) {
                    cache.putFrame(nextPath, img);
                }
            }
        }
    }

    /** Lấy đường dẫn frame hiện tại */
    public Path getCurrentFramePath() {
        return getFramePath(currentFrameIndex);
    }

    /** Tạo đường dẫn frame dựa trên index */
    private Path getFramePath(int index) {
        // Frame file dạng frame_0001.png → frame_NNNN.png
        String filename = String.format("frame_%04d.png", index);
        return desc.framesFolder.resolve(filename);
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setFPS(int fps) {
        if (fps > 0) {
            this.fps = fps;
            this.frameDuration = 1000.0 / fps;
        }
    }

    public int getFPS() {
        return fps;
    }

    public void setOpacity(float opacity) {
        if (opacity < 0f) opacity = 0f;
        if (opacity > 1f) opacity = 1f;
        this.opacity = opacity;
    }

    public float getOpacity() {
        return opacity;
    }

    /** Giải phóng tài nguyên (nếu cần) */
    public void dispose() {
        // Có thể clear cache liên quan nếu muốn
    }
}