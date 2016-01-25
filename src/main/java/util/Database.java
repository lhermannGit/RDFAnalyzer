package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import treeGeneration.ChildNode;
import treeGeneration.Node;
import treeGeneration.RootNode;

/*
 * This class provide connection to MariaDB, store subtrees into DB and provide access to the DB data
 * Notes: - MariaDB should be set up and run [use port 3307, while port 3306 is occupied by MySQL DB]
 * 		  - "datamining" DB should be created in advance
 * 		  - in order to get access to DB create config.properties file
 * 		  - DB_URL = "jdbc:mysql://localhost:3307/datamining" - change it if needed
 *  	  - add to build Path MariaDBConnector: libs/mariadb-java-client-1.2.3.jar
 *  
 * @author Olga Zagovora <zagovora@uni-koblenz.de>
 */

public class Database {

	// JDBC driver name and database URL
	//static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static String DB_URL = "jdbc:mysql://localhost:3306/datamining";
	// database credentials
	static String dbName = "datamining";
	static String user = "root";
	static String password = "";//""root
	private Connection connection;
	private Statement stmt;
	private int bagid;
	private int crawlid=0;
	static String tableName="subtree_path";
	
	public Database(RootNode rootNode) {
		// initialize
		
		// commented because you need to have the config.properties file
		//MySQLConnectionInfo config = new MySQLConnectionInfo();
		//dbName=config.getDatabaseName();
		//user=config.getUser();
		//password=config.getPassword();
		// TODO Add DB_URL in config.properties

		// connect to the database 		
		try {
			connection = DriverManager.getConnection(DB_URL, user, password);
			stmt = connection.createStatement();

			// create two tables (one with RDF triples another with graph structure)
			this.createTables(tableName);

			if (rootNode!=null)
				{ bagid=rootNode.bagID;
				this.serializeTree(rootNode);
				}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// checks if a database table exists
	private boolean tableExists(String tableName) throws SQLException {
		String str="SELECT * FROM information_schema.tables "
				+ "WHERE table_schema = '"+dbName+"' AND table_name = '"+tableName+"' "
				+ "LIMIT 1;";
		ResultSet res = this.stmt.executeQuery(str);
		
		//get number of rows
		res.last();
		
		return res.getRow() > 0;
	}
	
	// creates necessary tables
	private void createTables(String tableName) throws SQLException {
		String sqlString;
		if  (!tableExists("Crawls")){
			sqlString="CREATE TABLE Crawls "
					+ " (CrawlID INT NOT NULL AUTO_INCREMENT,"
					+ " Name VARCHAR(255) NOT NULL,"
					+ " PRIMARY KEY (CrawlID) );";
			stmt.executeUpdate(sqlString);
			System.out.println("Table Crawls was created.");
		} else
			System.out.println("No need to create table Crawls (already exists).");
		
		if  ( !tableExists("Trees")){
			sqlString="CREATE TABLE Trees "
					+ " (Id INT NOT NULL AUTO_INCREMENT,"
					+ " TreeID INT NOT NULL,"
					+ " BagID INT NULL,"
					+ " Current INT NOT NULL,"
					+ " Predicate INT NULL,"
					+ " Child_No INT NULL,"
					+ " PRIMARY KEY (ID) );";
			stmt.executeUpdate(sqlString);
			System.out.println("Table Trees is created!");
			}
		else {System.out.println("You tried to create table Trees that already exist.");
		}
		
		if  (!tableExists(tableName)){
			sqlString="CREATE TABLE "+tableName+" "
					+ " (Id INT NOT NULL AUTO_INCREMENT,"
					+ " Crawl INT NULL,"
					+ " Bag INT NULL,"
					+ " StartLvl INT NOT NULL,"
					+ " EndLvl INT NOT NULL,"
					+ " Path VARCHAR(255) NOT NULL,"
					+ " PRIMARY KEY (Id) "
					//+ ", FOREIGN KEY (Crawl) REFERENCES Crawls (CrawlID) "
					+ ");";
			stmt.executeUpdate(sqlString);
			System.out.println("Table "+tableName+" is created!");
		} else
			System.out.println("No need to create table "+tableName+" (already exists).");
		
		
		
	}

	

	public void serializeTree(RootNode rootNode) {
		
		///get the tree according to the rootNode
		
		LinkedList<Node> queueNodes = new LinkedList<Node>();
		if (rootNode!=null)
		{
			queueNodes.add(rootNode);
			//int lvl=0;
			int treeID;
			
			try {
			//fond the max id in the Table Tree and increment it in order to store new Tree
			String query="SELECT MAX(TreeID) FROM Trees ;";
			ResultSet res = this.stmt.executeQuery(query);
			res.last();
			treeID=res.getInt(1)+1;
			
			//Node parentnode=null;
			Node currentnode;
			LinkedList <Node> visited= new LinkedList <Node>();
			
			while(!queueNodes.isEmpty()){
				currentnode = queueNodes.getFirst();
				
				List<ChildNode> children = queueNodes.remove().getChildren();
				
				if (!visited.contains(currentnode) )
				if (children.size()!=0)
					{for (int n=0; n<children.size(); n++){
						if (!visited.contains(children.get(n)) )
						{queueNodes.add(children.get(n)); }
						///store to DB
						try {
							
							String SqlString="INSERT INTO Trees (TreeID, BagID, Current, Predicate, Child_No) VALUES("+treeID+" ,"+
									bagid+","+currentnode.getName()+","+children.get(n).getPredicate()+","+children.get(n).getName()+");";
							this.stmt.executeUpdate(SqlString);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							System.out.println("Cannot save current_node,predicate,child_node into Table Trees!!");
							e.printStackTrace();
						}
					/*if (queueNodes.getFirst()==children.get(0))
						{parentnode=currentnode;
						lvl+=1;}
					else if ()
					{}
					*/
					}
				}
				visited.add(currentnode);
				
			}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	
		
	}

	
	
	
	// runs any query string on the database
	public ResultSet query(String Pattern) {
		ResultSet result = null;
		System.out.println(Pattern);
		try{
			result = this.stmt.executeQuery(Pattern);
		}
		catch(Exception x) {
			x.printStackTrace();
		}
		return result;
	}
	
	public ResultSet preparedQuery(String query){
		ResultSet result = null;
		
		return result;
	}

	// returns all relevant subtrees and already create its new subtree via REGEXP_REPLACE
	public ResultSet getSubtrees(String regexp1, String regexp2, int newVal) {
		return this.query("SELECT StartLvl, EndLvl, Path, REGEXP_REPLACE(Path, '" + regexp2 + "', '\\\\0" + newVal + "\\(\\)') AS NewPath FROM subtree_path WHERE StartLvl = 0 AND Path REGEXP '" + regexp1 + "';");
	}
	
	// inserts a subtree into the database
	public boolean saveSubtree( int startLvl, int endLvl, String path) {
		ResultSet rs = this.query("INSERT INTO subtree_path (Crawl, Bag, StartLvl, EndLvl, Path) VALUES (" + crawlid + ", " + bagid + ", " + startLvl + ", " + endLvl + ", '" + path + "');");
		return rs != null;
	}
	
	// closes the database connection
	public void close() {
		try {
			this.stmt.close();
			this.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
