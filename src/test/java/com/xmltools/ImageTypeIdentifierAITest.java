package com.xmltools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ImageTypeIdentifierAITest {

    @TempDir
    File tempDir;

    @Test
    public void testGetAIImageType_EPS() throws IOException {
        File epsFile = new File(tempDir, "test.xxx");
        assertTrue(epsFile.createNewFile());

        // Write EPS header to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(epsFile))) {
            writer.write("%!PS-Adobe-3.0 EPSF-3.0\n");
        }

        String result = ImageTypeIdentifier.getAIImageType(epsFile);
        assertEquals("eps", result, "File should be detected as EPS.");
    }

    @Test
    public void testCheckForCGM() throws IOException, URISyntaxException {
        // Locate the sample CGM file in the resources directory
        Path resourcePath = Paths.get(Objects.requireNonNull(getClass().getResource("/ICN-S1000DBIKE-AAA-DA53000-0-U8025-00535-A-04-1.CGM")).toURI());

        // Create a copy of the CGM file in the temporary directory
        Path cgmFilePath = tempDir.toPath().resolve("test.xxx");
        Files.copy(resourcePath, cgmFilePath, StandardCopyOption.REPLACE_EXISTING);

        // Verify the file content
        File cgmFile = cgmFilePath.toFile();
        String result = ImageTypeIdentifier.checkForCGM(cgmFile);
        assertEquals("cgm", result, "File should be detected as CGM.");
    }
}
