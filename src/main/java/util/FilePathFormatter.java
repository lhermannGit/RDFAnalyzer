package util;

import java.io.File;

public class FilePathFormatter {

	public static void main(String[] args) {
		System.out.println(System.getProperty("user.home")+FilePathFormatter.setSeparators("/test/test/test"));
	}
	public static String setSeparators(String filePath){
		return filePath.replaceAll("//", File.separator);
	}
}
