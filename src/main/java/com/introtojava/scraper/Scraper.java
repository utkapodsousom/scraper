package com.introtojava.scraper;

import java.io.File;
import static java.io.File.separatorChar;
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
		
	}

	public static File createDirectory(String name) throws IOException {
		File dirPath = new File(System.getProperty("user.dir") + separatorChar + name);
		try {
			dirPath.mkdir();
			System.out.println("Created new directory: " + name);
		} catch (Exception e) {
				System.out.println("Couldn't create directory: " + e.getMessage());
		}

		return dirPath;
	}
}
