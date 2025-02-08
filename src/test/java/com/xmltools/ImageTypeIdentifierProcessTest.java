package com.xmltools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ImageTypeIdentifierProcessTest {

    @TempDir
    File tempDir;

    @Test
    public void testProcessFolder() throws IOException {
        // Create a temporary folder with test files
        File testFolder = new File(tempDir, "testFolder");
        assertTrue(testFolder.mkdir());

        // Create test files
        File jpegFile = new File(testFolder, "image1.xxx");
        BufferedImage bufferedImage1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(bufferedImage1, "jpg", jpegFile);

        File pngFile = new File(testFolder, "image2.xxx");
        BufferedImage bufferedImage2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(bufferedImage2, "png", pngFile);

        // Process the folder
        ImageTypeIdentifier.processFolder(testFolder);

        // Check that files have been renamed correctly
        assertTrue(new File(testFolder, "image1.jpg").exists());
        assertTrue(new File(testFolder, "image2.png").exists());
    }

    @Test
    public void testProcessFolder_InvalidFolder() {
        // Provide an invalid folder path
        File invalidFolder = new File("nonexistentFolder");
        ImageTypeIdentifier.processFolder(invalidFolder);

        // There should be no exceptions and no processing
    }
}
