package com.livewallpaper.api;

import java.nio.file.Path;

/**
 * API công khai cho hệ thống Live Wallpaper.
 *
 * Nguyên tắc:
 * - Không phụ thuộc loader (Fabric/Forge/Quilt).
 * - Gọn, chỉ cung cấp các hàm cần thiết; phần triển khai chi tiết nằm ở WallpaperManager.
 *
 * Ví dụ dùng (pseudo):
 *   LiveWallpaperAPI api = LiveWallpaperApiHolder.get(); // Lấy instance từ implementor
 *   api.registerWallpaper(new WallpaperDescriptor("elaina", Path.of("config/..."), 60, 3600, 1920, 1080, true));
 *   api.play("elaina");
 *   api.render("elaina", 0, 0, screenWidth, screenHeight);
 */
public interface LiveWallpaperAPI {

    /**
     * Đăng ký một wallpaper mới vào hệ thống.
     * Triển khai nên kiểm tra sự tồn tại của frames/resources, có thể trì hoãn kiểm tra nặng tới khi play().
     *
     * @param desc Thông tin và đường dẫn wallpaper.
     * @throws IllegalArgumentException nếu desc null hoặc sai.
     */
    void registerWallpaper(WallpaperDescriptor desc);

    /**
     * Gỡ đăng ký một wallpaper và giải phóng tài nguyên liên quan.
     * Sau khi gọi, wallpaper id này sẽ không còn khả dụng.
     *
     * @param id id wallpaper
     */
    void unregisterWallpaper(String id);

    /**
     * Bắt đầu phát một wallpaper đã đăng ký.
     * Nếu wallpaper đang phát thì bỏ qua hoặc reset lại tuỳ triển khai.
     *
     * @param id id wallpaper
     */
    void play(String id);

    /**
     * Tạm dừng phát wallpaper (giữ nguyên vị trí hiện tại).
     *
     * @param id id wallpaper
     */
    void pause(String id);

    /**
     * Dừng phát và quay lại vị trí đầu tiên (frame 0).
     *
     * @param id id wallpaper
     */
    void stop(String id);

    /**
     * Tua tới một thời điểm cụ thể (giây) trong wallpaper.
     * Nếu thời gian yêu cầu vượt phạm vi, sẽ bị giới hạn.
     *
     * @param id id wallpaper
     * @param seconds vị trí tính bằng giây
     */
    void seek(String id, double seconds);

    /**
     * Lấy vị trí phát hiện tại (tính bằng giây).
     *
     * @param id id wallpaper
     * @return giây (>= 0)
     */
    double getPosition(String id);

    /**
     * Kiểm tra wallpaper có đang phát hay không.
     *
     * @param id id wallpaper
     * @return true nếu đang phát
     */
    boolean isPlaying(String id);

    /**
     * Bật/tắt chế độ lặp.
     *
     * @param id id wallpaper
     * @param loop true nếu bật lặp
     */
    void setLoop(String id, boolean loop);

    /**
     * Lấy trạng thái chế độ lặp.
     *
     * @param id id wallpaper
     * @return true nếu đang bật lặp
     */
    boolean isLooping(String id);

    /**
     * Đặt FPS khi phát. Triển khai sẽ cập nhật timer nội bộ theo FPS mới.
     * Giá trị hợp lệ > 0, có thể giới hạn hoặc từ chối giá trị quá lớn.
     *
     * @param id id wallpaper
     * @param fps số frame/giây (ví dụ 30 hoặc 60)
     */
    void setFPS(String id, int fps);

    /**
     * Lấy FPS hiện tại của wallpaper.
     *
     * @param id id wallpaper
     * @return fps
     */
    int getFPS(String id);

    /**
     * Đặt độ mờ khi render (0.0f = trong suốt hoàn toàn, 1.0f = đục hoàn toàn).
     *
     * @param id id wallpaper
     * @param opacity 0..1
     */
    void setOpacity(String id, float opacity);

    /**
     * Lấy độ mờ hiện tại.
     *
     * @param id id wallpaper
     * @return opacity 0..1
     */
    float getOpacity(String id);

    /**
     * Render frame hiện tại của wallpaper vào vùng được chỉ định.
     *
     * Lưu ý:
     * - Hàm này nên được gọi ở render/main thread.
     * - Nếu wallpaper không phát, vẫn render frame cuối hoặc ảnh mặc định.
     *
     * @param id id wallpaper
     * @param x toạ độ trái (pixel)
     * @param y toạ độ trên (pixel)
     * @param width chiều rộng (pixel)
     * @param height chiều cao (pixel)
     */
    void render(String id, int x, int y, int width, int height);

    /**
     * Tiện ích: đăng ký wallpaper từ thư mục frames (có thể trong config hoặc assets).
     * Triển khai sẽ tạo WallpaperDescriptor từ thông tin này.
     *
     * @param id id wallpaper
     * @param framesFolder đường dẫn tới thư mục frames
     * @param fps fps
     * @param frames tổng số frame (nếu không biết, triển khai có thể tự dò)
     * @param width chiều rộng frame
     * @param height chiều cao frame
     * @param loop có lặp hay không
     */
    void registerWallpaper(String id, Path framesFolder, int fps, int frames, int width, int height, boolean loop);

    /**
     * Kiểm tra wallpaper có được đăng ký hay không.
     *
     * @param id id wallpaper
     * @return true nếu đã đăng ký
     */
    boolean isRegistered(String id);
}