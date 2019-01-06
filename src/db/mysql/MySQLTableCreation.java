package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySQLTableCreation {
	public static void main(String[] args) {
		try {
			System.out.printf("Connecting to MySQL database %s\n", MySQLUtils.DB_NAME);
//			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			Connection conn = DriverManager.getConnection(MySQLUtils.URL);
			if (conn == null) {
				System.out.println("Connection Failed.");
			} else {
				System.out.println("Connection Successful.");
				Statement stmt = conn.createStatement();

				// clean up 
				stmt.executeUpdate("DROP TABLE IF EXISTS categories, history, items, users");
				
				// create new tables
				String items = "CREATE TABLE items ("
								+ "	item_id VARCHAR(255) NOT NULL,"
								+ " name VARCHAR(255),"
								+ " rating FLOAT,"
								+ " address VARCHAR(255),"
								+ " image_url VARCHAR(255),"
								+ " url VARCHAR(255),"
								+ " distance FLOAT,"
								+ " PRIMARY KEY(item_id)"
								+ ")";
				String users = "CREATE TABLE users ("
								+ "	user_id VARCHAR(255) NOT NULL,"
								+ " password VARCHAR(255) NOT NULL,"
								+ " first_name VARCHAR(255),"
								+ " last_name VARCHAR(255), "
								+ " PRIMARY KEY(user_id)"
								+ ")";
				String categories = "CREATE TABLE categories ("
								+ "	item_id VARCHAR(255) NOT NULL,"
								+ " category VARCHAR(255) NOT NULL,"
								+ " PRIMARY KEY(item_id, category),"
								+ " FOREIGN KEY(item_id) REFERENCES items(item_id)"
								+ ")";
				String history = "CREATE TABLE history ("
								+ "	user_id VARCHAR(255) NOT NULL,"
								+ " item_id VARCHAR(255) NOT NULL,"
								+ " last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
								+ " PRIMARY KEY(user_id, item_id),"
								+ " FOREIGN KEY(user_id) REFERENCES users(user_id),"
								+ " FOREIGN KEY(item_id) REFERENCES items(item_id)"
								+ ")";
				
				stmt.executeUpdate(items);
				stmt.executeUpdate(users);
				stmt.executeUpdate(categories);
				stmt.executeUpdate(history);
				
				// insert test user...
				stmt.executeUpdate("INSERT INTO users VALUES('1111','3229c1097c00d497a0fd282d586be050','John','Smith')");
				
				
				
				System.out.println("Done.");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
