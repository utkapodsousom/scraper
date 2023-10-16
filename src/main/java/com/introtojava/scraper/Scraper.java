package com.introtojava.scraper;

import java.io.File;
import static java.io.File.separatorChar;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author ootka
 */
public class Scraper {

	public static void main(String[] args) throws IOException {
		// get site url from user
		Scanner input = new Scanner(System.in);
		System.out.print("Enter site url: ");
		String siteUrl = input.nextLine();

		// get html and create directory and file
		Document html = Jsoup.connect(siteUrl.trim().toLowerCase()).get();

		//generate dir name
		Calendar cal = Calendar.getInstance();
		long timeInMillis = cal.getTimeInMillis();
		int hash = html.title().hashCode();
		String dirName = String.valueOf(timeInMillis + hash);

		String currentDir = createDirectory(dirName).getAbsolutePath();
		File index = new File(currentDir + separatorChar + "index.html");
		try (FileWriter writer = new FileWriter(index)) {
			writer.write(html.outerHtml());
		}

	}

	public static File createDirectory(String name) throws IOException {
		File dirPath = new File(System.getProperty("user.dir") + separatorChar + name);
		try {
			if (dirPath.mkdir()) {
				System.out.println("Created new directory: " + name);
			} else {
				System.out.println("Directory already exists");
			}
		} catch (Exception e) {
			System.out.println("Couldn't create directory: " + e.getMessage());
		}

		return dirPath;
	}

	public static File createFile(String path, String name) throws IOException {
		File filePath = new File(path + separatorChar + name);
		try {
			if (filePath.createNewFile()) {
				System.out.println("Created new file: " + name);
			} else {
				System.out.println("File already exists");
			}
		} catch (IOException e) {
			System.out.println("Couldn't create file: " + e.getMessage());
		}

		return filePath;
	}
}
