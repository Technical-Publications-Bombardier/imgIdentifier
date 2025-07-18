package com.xmltools;

import me.tongfei.progressbar.ProgressBar;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jcgm.core.CGM;

public class ImageTypeIdentifier {
		public static void main(String[] args) {
				if (args.length != 1) {
						System.err.println("Usage: java ImageTypeIdentifier <folder_path>");
						return;
				}

				String folderPath = args[0];
				File folder = new File(folderPath);
				processFolder(folder);
		}

		public static void processFolder(File folder) {
				File[] files = folder.listFiles();
				if (files == null) {
						System.err.println("Invalid folder path.");
						return;
				}
try(ProgressBar bar = new ProgressBar(String.format("Processing folder: '%s'", folder.getName()) , files.length)){
    for (File file : files) {
        bar.step();
        if (file.isDirectory()) {
            processFolder(file);
        } else if (file.isFile() && file.getName().endsWith(".xxx")) {
            String newExtension = getImageType(file);
            if (newExtension != null) {
                String newFileName = file.getName().replace(".xxx", "." + newExtension);
                File newFile = new File(file.getParent(), newFileName);

                if (file.renameTo(newFile)) {
                    System.out.println("Renamed: " + file.getName() + " -> " + newFileName);
                } else {
                    System.err.println("Failed to rename: " + file.getName());
                }
            }
        }
    }
}

		}

		public static String getImageType(File file) {
				try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
						Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
						if (readers.hasNext()) {
								ImageReader reader = readers.next();
								String formatName = reader.getFormatName().toLowerCase();

								reader.setInput(iis);
                            return switch (formatName) {
                                case "jpeg", "jpg" -> "jpg";
                                case "gif" -> "gif";
                                case "bmp" -> "bmp";
                                case "png" -> "png";
                                default -> {
                                    System.err.println("Unknown image type: [" + file.getName() + "] Type name :" + formatName);
                                    yield null;
                                }
                            };
						} else {
							var imageType = getAIImageType(file);
							if (imageType != null) {
								return imageType;
							} else {
								System.err.println("No suitable ImageReader found for: " + file.getName());
								return null;
							}
						}
				} catch (IOException e) {
						System.err.printf("Error reading the file: '%s'%n",file.getName());
					//noinspection CallToPrintStackTrace
					e.printStackTrace();
						return null;
				}
		}


	public static String getAIImageType(File file) {
		String epsPattern = "%!?PS-Adobe-[\\d.]+\\s+EPSF-[\\d.]+";

		// First check: binary EPS header (magic bytes)
		try (InputStream input = new FileInputStream(file)) {
			byte[] magic = new byte[4];
			if (input.read(magic) == 4) {
				// Match binary EPS header C5 D0 D3 C6
				if ((magic[0] & 0xFF) == 0xC5 &&
						(magic[1] & 0xFF) == 0xD0 &&
						(magic[2] & 0xFF) == 0xD3 &&
						(magic[3] & 0xFF) == 0xC6) {
					return "eps";
				}
			}
		} catch (IOException e) {
			System.err.println("Failed to read file bytes: " + file.getName());
		}

		// Second check: ASCII EPS header via regex
		Pattern pattern = Pattern.compile(epsPattern);
		try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
			String firstLine = reader.readLine();
			firstLine += reader.readLine();  // Append second line
			firstLine = firstLine.replaceAll("[^\\x20-\\x7E]", "");  // Sanitize the input by removing non-printable characters
			Matcher matcher = pattern.matcher(firstLine);
			if (matcher.find()) {
				return "eps";
			} else {
				return checkForCGM(file);  // fallback
			}
		} catch (IOException e) {
			System.err.println("Failed to read file text: " + file.getName());
		}

		return null;
	}

	static String checkForCGM(File file) {
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			// Check if the file is empty
			if (in.available() == 0) {
				System.err.println("Empty file: " + file.getName());
				return null;
			}
			CGM cgm = new CGM();
			cgm.read(in);
			return "cgm";
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + file.getName());
		} catch (Exception ignoredException) {
			System.err.println("Unidentified file type: " + file.getName());
		}
		return null;
	}

}
