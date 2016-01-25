package input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MySQLConnectionInfo {

	String password = "";
	String username = "";
	String database = "";
	String server = "";
	InputStream inputStream;

	public MySQLConnectionInfo() {
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
				username = prop.getProperty("username");
				password = prop.getProperty("password");
				database = prop.getProperty("database");
				server = prop.getProperty("server");
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

	}

	public String getUser() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDatabaseName() {
		return database;
	}

	public String getServer() {
		return server;
	}
}
