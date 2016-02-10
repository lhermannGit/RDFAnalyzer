package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	
	
	public final static Database INSTANCE = new Database();
	Logger LOG = LogManager.getLogger(Database.class.getName());
	
	DBConnectionInfo dbConfig = null;
	
	private Connection connection;
	private Statement stmt;
	private int crawlid = 0;
	private int bagid;
	private class MyNode{
	    Node node;
	    int lvl;

	    private MyNode(Node node,int lvl) {
	        this.node = node;
	        this.lvl = lvl;
	    }
	}

	private Database(){
		dbConfig = new DBConnectionInfo();
		try {
			connection = DriverManager.getConnection(dbConfig.DB_URL, dbConfig.user, dbConfig.password);
			stmt = connection.createStatement();

			// create 3 tables (one with RDF triples,one for paths and one for crawl's data)
			this.createTables(dbConfig.subtrees);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void serialize(RootNode rootNode){
		
		if (rootNode != null) {
			LinkedList<MyNode> queueNodes = new LinkedList<MyNode>();
			int lvl=0;
			int treeID;
			int lastVisitedLvl=0;
			MyNode currentnode;
			LinkedList <Node> visited= new LinkedList <Node>();
			queueNodes.add(new MyNode(rootNode,lvl));
			bagid=rootNode.bagID;
			try {
				// fond the max id in the Table Tree and increment it in order
				// to store new Tree
				String query = "SELECT MAX(TreeID) FROM Trees ;";
				ResultSet res = this.stmt.executeQuery(query);
				res.last();
				treeID = res.getInt(1) + 1;
				
				//save first line, root node
				String SqlString="INSERT INTO Trees ( BagID, TreeID, Current, Lvl) VALUES( "+bagid+","+treeID+","+rootNode.getName()+","+lvl+");";
				this.stmt.executeUpdate(SqlString);
				

				while (!queueNodes.isEmpty()) {
					currentnode = queueNodes.getFirst();
					
					List<ChildNode> children = queueNodes.remove().node.getChildren();
					int child_n=0;
					
					if (currentnode.lvl!=lastVisitedLvl){
						lastVisitedLvl++;}
					
					if (!visited.contains(currentnode.node) )
						{if (children.size()!=0)
							{for (int n=0; n<children.size(); n++){
						
								if (!visited.contains(children.get(n)) )
									{queueNodes.add(new MyNode(children.get(n),currentnode.lvl+1));}
								///store to DB
								try {
									lvl=lastVisitedLvl+1;
									SqlString="INSERT INTO Trees ( BagID, TreeID, Parent, Predicate, Current, Child_No, Lvl) VALUES("+
											bagid+","+treeID+","+currentnode.node.getName()+","+children.get(n).getPredicate()+","+children.get(n).getName()+","+child_n+","+lvl+");";
									this.stmt.executeUpdate(SqlString);
									child_n++;
									} 
								catch (SQLException e) {
									System.out.println("Cannot save current_node,predicate,child_node into Table Trees!!");
									e.printStackTrace();
									}	
								}
							}
						visited.add(currentnode.node);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}


	// checks if a database table exists
	private boolean tableExists(String tableName) throws SQLException {
		String str = "SELECT * FROM information_schema.tables " + "WHERE table_schema = '" + dbConfig.dbName
				+ "' AND table_name = '" + tableName + "' " + "LIMIT 1;";
		ResultSet res = this.stmt.executeQuery(str);

		// get number of rows
		res.last();

		return res.getRow() > 0;
	}

	// creates necessary tables
	private void createTables(String tableName) throws SQLException {
		String sqlString;
		if (!tableExists("Crawls")) {
			sqlString = "CREATE TABLE Crawls " + " (CrawlID INT NOT NULL AUTO_INCREMENT,"
					+ " Name VARCHAR(255) NOT NULL," + " PRIMARY KEY (CrawlID) );";
			stmt.executeUpdate(sqlString);
			LOG.info("Table Crawls was created.");
		} else
			LOG.info("No need to create table Crawls (already exists).");

		if (!tableExists("Trees")) {
			sqlString="CREATE TABLE Trees (Id INT NOT NULL AUTO_INCREMENT,"
					+ " TreeID INT NULL, BagID INT NULL,"
					+ " Parent INT NULL, Predicate INT NULL, Current INT NOT NULL,"
					+ " Child_No INT NULL, Lvl INT NOT NULL,"
					+ " PRIMARY KEY (ID) );";
			stmt.executeUpdate(sqlString);
			LOG.info("Table Trees is created!");
		} else {
			LOG.info("You tried to create table Trees that already exist.");
		}

		if (!tableExists(tableName)) {
			sqlString = "CREATE TABLE " + tableName + " " + " (Id INT NOT NULL AUTO_INCREMENT," + " Crawl INT NULL,"
					+ " Bag INT NULL," + " StartLvl INT NOT NULL," + " EndLvl INT NOT NULL,"
					+ " Path VARCHAR(255) NOT NULL," + " PRIMARY KEY (Id) "
					// + ", FOREIGN KEY (Crawl) REFERENCES Crawls (CrawlID) "
					+ ");";
			stmt.executeUpdate(sqlString);
			LOG.info("Table " + tableName + " is created!");
		} else
			LOG.info("No need to create table " + tableName + " (already exists).");

	}

	// runs any query string on the database
	public ResultSet query(String Pattern) {
		ResultSet result = null;
//		System.out.println(Pattern);
		try {
			result = this.stmt.executeQuery(Pattern);
		} catch (Exception x) {
			x.printStackTrace();
		}
		return result;
	}

	public ResultSet preparedQuery(String query) {
		ResultSet result = null;

		return result;
	}

	// returns all relevant subtrees and already create its new subtree via
	// REGEXP_REPLACE
	public ResultSet getSubtrees(String regexp1, String regexp2, int newVal) {
		return this.query("SELECT StartLvl, EndLvl, Path, REGEXP_REPLACE(Path, '" + regexp2 + "', '\\\\0" + newVal
				+ "\\(\\)') AS NewPath FROM subtree_path WHERE StartLvl = 0 AND Path REGEXP '" + regexp1 + "';");
	}

	// inserts a subtree into the database
	public boolean saveSubtree(int startLvl, int endLvl, String path, int bagID) {
		ResultSet rs = this.query("INSERT INTO subtree_path (Crawl, Bag, StartLvl, EndLvl, Path) VALUES (" + crawlid
				+ ", " + bagID + ", " + startLvl + ", " + endLvl + ", '" + path + "');");
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
