package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConnectionInfo {
	String DB_URL;

	String dbName;
	String user;
	String password;
	String subtrees;
	InputStream inputStream;
	
	public DBConnectionInfo() {
		Properties prop = new Properties();

		try {
			inputStream = new FileInputStream(new File("config.properties"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (inputStream != null) {
			try {
				prop.load(inputStream);
				DB_URL = prop.getProperty("DB_URL");
				user = prop.getProperty("user");
				password = prop.getProperty("password");
				dbName = prop.getProperty("dbName");
				subtrees = prop.getProperty("subtrees");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				throw new FileNotFoundException("property file not found in the classpath");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public String getDB_URL() {
		return DB_URL;
	}

	public String getDbName() {
		return dbName;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

}
