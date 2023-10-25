package com.introtojava.scraper;

import java.io.BufferedInputStream;
import java.io.File;
import static java.io.File.separatorChar;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
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

	public static void main(String[] args) throws IOException {
		// get site url from user
		Scanner input = new Scanner(System.in);
		System.out.print("Enter site url: ");
		String siteUrl = input.nextLine().trim().toLowerCase();

		// get html and create directory and file
		Document html = Jsoup.connect(siteUrl).get();

		//generate dir name
		Calendar cal = Calendar.getInstance();
		long timeInMillis = cal.getTimeInMillis();
		int hash = html.title().hashCode();
		String dirName = String.valueOf(timeInMillis + hash);

		File mainDir = new File(dirName);
		mainDir.mkdir();
		File index = new File(mainDir.getAbsolutePath() + separatorChar + "index.html");
		FileWriter writer = new FileWriter(index);
		writer.write(html.outerHtml());

		StringBuilder sb = new StringBuilder();

		Document doc = Jsoup.parse(index, "UTF-8", siteUrl);
		Elements els = doc.getAllElements();
		String attrType = "";
		for (Element el : els) {
			switch (el.normalName()) {
				case "link" -> {
					attrType = "href";
					String fileName = getFileName(el, attrType);
					if (el.attr("rel").equals("stylesheet")) {
						if (el.attr(attrType).contains("fonts")) {
							continue;
						}
						File stylesDir = new File(mainDir, "styles");
						if (stylesDir.mkdir()) {
							System.out.println("Created directory for styles");
						}
						File cssFile = new File(stylesDir, fileName);
						writeFile(el, cssFile, attrType);
						el.attr(attrType, "styles" + separatorChar + fileName);
					} else if (el.absUrl("rel").contains("icon")) {
						File icon = new File(mainDir, fileName);
						writeFile(el, icon, attrType);
						el.attr(attrType, fileName);
					}
				}
				case "img" -> {
					attrType = "src";	
					String fileName = getFileName(el, attrType);
					if (el.attr(attrType).contains("base64")) {
						continue;
					}
					File imgDir = new File(mainDir, "images");
					if (imgDir.mkdir()) {
						System.out.println("Created directory for images");
					}
					File image = new File(imgDir, fileName);
					writeFile(el, image, attrType);
					el.attr(attrType, "images" +separatorChar + fileName);
				}
				default -> {
					continue;
				}
			}

			sb.append(el.outerHtml());
		}

		System.out.println(sb.toString());

	}

	public static String getFileName(Element el, String attr) {
		String[] urlParts = el.attr(attr).split("/");
		String fileName = urlParts[urlParts.length - 1];
		fileName = fileName.contains("?") ? fileName.substring(0, fileName.indexOf("?")) : fileName;
		return fileName;
	}

	public static void writeFile(Element el, File file, String attr) throws IOException {
		try (
			BufferedInputStream in = new BufferedInputStream(new URL(el.absUrl(attr)).openStream()); FileOutputStream fileOutputStream = new FileOutputStream(file);) {
			byte dataBuffer[] = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
			}
		} catch (IOException e) {
			System.out.println("Error writing buffer: " + e.getMessage());
		}
	}

}
