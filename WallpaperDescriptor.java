package com.livewallpaper.api;

import java.nio.file.Path;

/**
 * Lớp chứa thông tin mô tả (metadata) của một wallpaper.
 * 
 * Bao gồm ID, đường dẫn thư mục frames, thông số kỹ thuật như FPS, số lượng frame,
 * kích thước mỗi frame và trạng thái lặp (loop).
 * 
 * Lưu ý:
 * - Không xử lý logic render, chỉ lưu thông tin.
 * - Được truyền vào khi đăng ký wallpaper qua API.
 */
public class WallpaperDescriptor {

    /** ID định danh của wallpaper (duy nhất) */
    public final String id;

    /** Thư mục chứa frames (ảnh) của wallpaper */
    public final Path framesFolder;

    /** FPS mong muốn khi phát (frame/giây) */
    public final int fps;

    /** Tổng số frame của wallpaper */
    public final int frames;

    /** Chiều rộng mỗi frame (pixel) */
    public final int width;

    /** Chiều cao mỗi frame (pixel) */
    public final int height;

    /** Có lặp lại hay không */
    public final boolean loop;

    /**
     * Tạo một WallpaperDescriptor mới.
     * 
     * @param id ID duy nhất của wallpaper
     * @param framesFolder Thư mục chứa frames
     * @param fps Số frame/giây
     * @param frames Tổng số frame
     * @param width Chiều rộng frame
     * @param height Chiều cao frame
     * @param loop Có lặp hay không
     */
    public WallpaperDescriptor(String id, Path framesFolder, int fps, int frames, int width, int height, boolean loop) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID wallpaper không được để trống");
        }
        if (framesFolder == null) {
            throw new IllegalArgumentException("Thư mục frames không được null");
        }
        if (fps <= 0) {
            throw new IllegalArgumentException("FPS phải > 0");
        }
        if (frames <= 0) {
            throw new IllegalArgumentException("Số lượng frame phải > 0");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Kích thước frame phải > 0");
        }

        this.id = id;
        this.framesFolder = framesFolder;
        this.fps = fps;
        this.frames = frames;
        this.width = width;
        this.height = height;
        this.loop = loop;
    }
}