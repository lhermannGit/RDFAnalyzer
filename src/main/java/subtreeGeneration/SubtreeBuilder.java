package subtreeGeneration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import util.Database;

public class SubtreeBuilder {

	private Database db;
	
	// subpattern of regular expression for nested int & parenthesis
	private String nes =
		"(" +					// start group 1 (for recursion)
			"(" +				// start group 2 (allow neighbours)
	         	"[0-9]+" +		// match the int
	         	"\\\\(" +		// match the literal "("
	            	"(|(?1))" +	// empty or start recursively at group 1 again
	          	"\\\\)" +		// match the literal ")"
	        ")*" +				// end group 2
		")";					// end group 1
	
	public SubtreeBuilder(Database db) {
		this.db = db;
		// empty table
		//db.anyQuery("TRUNCATE `subtree_path`;");
		//System.out.println("Emptied table 'subtree_path'.");
		System.out.println("Insert subtrees into the database...");
	}

	/*
	 * 1) queries the database for all relevant subtrees for the parameter path
	 * 2) builds new subtrees from the selected subtrees, adding the last connection in path
	 * 3) builds new subtrees with adjusted start level
	 */
	public void buildTrees(ArrayList<Integer> path) {
		/*
		 * below is an approach to make use of a named subroutine to shorten the whole query string
		 * instead of the long nes-string above only (?&nes) would have to be inserted into the regular expression
		 * 
		 * this error is thrown: "...two named subpatterns have the same name..."
		 * 
		 * syntax can be seen here: https://mariadb.com/kb/en/mariadb/pcre/
		 * scroll down to "Defining subpatterns for use by reference"
		 * 
		 * I don't see the problem, feel free to give it a go and ask me (Adrian)
		 * 
		 * set define_subpattern to true to activate it
		 */
		boolean defineSubpattern = false;
		String n;
		if (defineSubpattern) {
			n = "(?&nes)";
			nes = "(?(DEFINE)(?<nes>" + nes + "))" + n;
		} else
			n = nes;
		
		// creates the regular expression from the given path which allows nested elements in between
		// e.g. for {5, 1, 3} something like:
		// ^{anyNestedElementsHere}5({anyNestedElementsHere}1({anyNestedElementsHere}3(
		String re = "^" + nes;
		for (int i = 0; i < path.size() - 1; i++) {
			re += path.get(i) + "\\\\(";
			if (i != path.size() - 2)
				re += n;
		}
		
		System.out.println("\nInsert: " + path.toString() + ", Regexp: " + re);
		
		// get all relevant subtrees
		ResultSet res = db.getSubtrees(re, re + n, path.get(path.size() - 1));
		if (res == null)
			System.out.println("ERROR on SELECT");
		else
			try {
				// for each relevant subtree
				while (res.next()) {
					// increment EndLvl if necessary
					int endLvl = res.getInt("EndLvl");
					if (endLvl < path.size() - 1)
						endLvl++;
					
					// insert new subtree
					boolean qry = db.saveSubtree( 0, endLvl, res.getString("NewPath")); // TODO real Crawl & Bag
					if (!qry)
						System.out.println("ERROR on Insert: " + res.getString("NewPath"));
					else
						System.out.println("Selected: " + res.getString("Path") + " - Inserted: " + res.getString("NewPath"));
					
					// insert subtrees with 0 < StartLvl < EndLvl
					// TODO
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		
		// insert new subtree if path has only one element
		if (path.size() == 1) {
			String newPath = path.get(0) + "()";
			boolean qry = db.saveSubtree( 0, 0, newPath); // TODO real Crawl & Bag
			if (!qry)
				System.out.println("ERROR on Create: " + newPath);
			else
				System.out.println("Created: " + newPath);
		}
	}
}
