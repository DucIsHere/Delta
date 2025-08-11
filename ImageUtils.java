package com.livewallpaper.api.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tiện ích load ảnh từ file.
 * 
 * Hỗ trợ PNG/JPEG/WebP nếu có plugin ImageIO WebP.
 */
public class ImageUtils {

    /**
     * Load ảnh từ đường dẫn.
     * 
     * @param path Đường dẫn ảnh
     * @return BufferedImage hoặc null nếu lỗi
     */
    public static BufferedImage loadImage(Path path) {
        if (path == null || !Files.exists(path)) return null;
        try {
            return ImageIO.read(path.toFile());
        } catch (IOException e) {
            System.err.println("Không thể load ảnh: " + path + " (" + e.getMessage() + ")");
            return null;
        }
    }
}