package com.xmltools;

import me.tongfei.progressbar.ProgressBar;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.util.Iterator;

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
		String epsPattern = "%!PS-Adobe-\\d\\.\\d EPSF-\\d\\.\\d";

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String firstLine = reader.readLine();
			if (firstLine != null && firstLine.matches(epsPattern)) {
				return "eps";
			} else {
				System.err.println("No eps header found for file: " + file.getName());
				return checkForCGM(file);
			}
		} catch (IOException e) {
			System.err.println("Failed to read file: " + file.getName());
		}
		return null;
	}

	private static String checkForCGM(File file) {
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			CGM cgm = new CGM();
			cgm.read(in);
			return "cgm";
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + file.getName());
		} catch (IOException e) {
			System.err.println("Unidentified file type: " + file.getName());
		}
		return null;
	}

}
