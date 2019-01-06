package db.mysql;

public class MySQLUtils {

	private static final String HOSTNAME = "localhost";
	private static final String PORT_NUM = "3306";
	public static final String DB_NAME = "laiproject";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";
	public static final String URL = String.format(
			"jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true&serverTimezone=UTC", 
			HOSTNAME, PORT_NUM, DB_NAME, USERNAME, PASSWORD);
}
