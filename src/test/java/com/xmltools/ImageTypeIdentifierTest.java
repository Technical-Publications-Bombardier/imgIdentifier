package com.xmltools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

public class ImageTypeIdentifierTest {

    @TempDir
    File tempDir;

    @Test
    public void testGetImageType_JPEG() throws IOException {
        File jpgFile = new File(tempDir, "test.xxx");
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(bufferedImage, "jpg", jpgFile);

        String result = ImageTypeIdentifier.getImageType(jpgFile);
        assertEquals("jpg", result, "File should be detected as JPEG.");
    }

    @Test
    public void testGetImageType_PNG() throws IOException {
        File pngFile = new File(tempDir, "test.xxx");
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(bufferedImage, "png", pngFile);

        String result = ImageTypeIdentifier.getImageType(pngFile);
        assertEquals("png", result, "File should be detected as PNG.");
    }

    @Test
    public void testGetImageType_Unknown() throws IOException {
        File unknownFile = new File(tempDir, "test.xxx");
        Files.deleteIfExists(unknownFile.toPath());
        assertTrue(unknownFile.createNewFile());

        // Add random data to the unknown file
        byte[] randomData = new byte[1024]; // 1 KB of random data
        new SecureRandom().nextBytes(randomData);
        Files.write(unknownFile.toPath(), randomData);

        String result = ImageTypeIdentifier.getImageType(unknownFile);
        assertNull(result, "File type should be unknown.");
    }
}
