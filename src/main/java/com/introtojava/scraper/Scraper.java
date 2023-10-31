package com.introtojava.scraper;

import java.io.BufferedInputStream;
import java.io.File;
import static java.io.File.separatorChar;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author ootka
 */
public class Scraper {

	public static void main(String[] args) throws IOException, URISyntaxException {
		// get site url from user
		Scanner input = new Scanner(System.in);
		System.out.print("Enter site url: ");
		String siteUrl = input.nextLine().trim().toLowerCase();

		// get html and create directory and file
		Document html = Jsoup.connect(siteUrl).get();

		// generate dir name
		Calendar cal = Calendar.getInstance();
		long timeInMillis = cal.getTimeInMillis();
		int hash = html.title().hashCode();
		String dirName = String.valueOf(timeInMillis + hash);

		File mainDir = new File(dirName);
		mainDir.mkdir();
		File index = new File(mainDir.getAbsolutePath() + separatorChar + "index.html");
		FileWriter writer = new FileWriter(index);
		writer.write(html.outerHtml());

		StringBuilder newHtml = new StringBuilder();

		Document doc = Jsoup.parse(index, "UTF-8", siteUrl);
		Elements els = doc.getAllElements();
		String attrType = "";
		for (Element el : els) {
			switch (el.normalName()) {
				case "link" -> {
					attrType = "href";
					String fileName = getFileName(el.attr(attrType));
					if (el.attr("rel").equals("stylesheet")) {
						if (el.attr(attrType).contains("fonts")) {
							continue;
						}
						File stylesDir = new File(mainDir, "styles");
						if (stylesDir.mkdir()) {
							System.out.println("Created directory for styles");
						}
						File cssFile = new File(stylesDir, fileName);
						writeFile(el.absUrl(attrType), cssFile);
						el.attr(attrType, "styles" + separatorChar + fileName);
					} else if (el.absUrl("rel").contains("icon")) {
						File icon = new File(mainDir, fileName);
						writeFile(el.absUrl(attrType), icon);
						el.attr(attrType, fileName);
					}
				}
				case "img" -> {
					attrType = "src";
					if (el.attr(attrType).isBlank()
					    || el.attr(attrType).contains("base64")) {
						continue;
					}
					StringBuilder srcsetString = new StringBuilder();
					String fileName = getFileName(el.attr(attrType));
					File imgDir = new File(mainDir, "images");
					if (imgDir.mkdir()) {
						System.out.println("Created directory for images");
					}
					File image = new File(imgDir, fileName);
					writeFile(el.absUrl(attrType), image);
					el.attr(attrType, "images" + separatorChar + fileName);
					// TODO: srcset job for <source> tag
					if (el.hasAttr("srcset")) {
						String[] srcset = el.attr("srcset").split(",");
						for (String set : srcset) {
							String screenWidth = "";
							if (set.trim().split(" ").length > 1) {
								screenWidth = set.trim().split(" ")[1];
							}
							String setURL = set.trim().split(" ")[0];
							fileName = getFileName(setURL);
							image = new File(imgDir, fileName);
							if (el.attr("srcset").contains("https")) {
								writeFile(setURL, image);
							} else {
								writeFile(siteUrl + separatorChar + setURL, image);
							}
							srcsetString.append("images").append(separatorChar).append(fileName).append(" ").append(screenWidth).append(",");
						}
						el.attr("srcset", srcsetString.toString());
					}
				}

				default -> {
					continue;
				}
			}

			newHtml.append(el.outerHtml());
		}

		System.out.println(newHtml.toString());

	}

	public static String getFileName(String url) {
		String[] urlParts = url.split("/");
		String fileName = urlParts[urlParts.length - 1];
		fileName = fileName.contains("?") ? fileName.substring(0, fileName.indexOf("?")) : fileName;
		return fileName;
	}

	public static void writeFile(String url, File file) throws IOException, URISyntaxException {
		try (
		    BufferedInputStream in = new BufferedInputStream(new URI(url).toURL().openStream()); FileOutputStream fileOutputStream = new FileOutputStream(file);) {
			byte dataBuffer[] = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
			}
		} catch (Exception e) {
			if (e instanceof IOException) {
				System.out.println("Error writing buffer: " + e.getMessage());
			} else if (e instanceof URISyntaxException) {
				System.out.println("Invalid URI: " + e.getMessage());
			} else {
				System.out.println("Unknown error: " + e.getMessage());
			}
		}
	}

}
