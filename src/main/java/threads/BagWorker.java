package threads;

import java.util.ArrayList;
import java.util.List;


import input.DisjointSet;
import subtreeGeneration.SubtreeBuilder;
import subtreeGeneration.TreeTraversal;
import treeGeneration.RootNode;
import treeGeneration.Window;
import util.Database;

public class BagWorker implements Runnable{

	DisjointSet bag;
	int bagID;
	int crawlID;
	
	public BagWorker(DisjointSet bag, int bagID, int crawlID){
		this.bag = bag;
		this.bagID = bagID;
		this.crawlID = crawlID;
	}
	
	@Override
	public void run() {
		Window window = new Window();
		List<RootNode> rootNodes = window.buildTree(bag.getSet().toArray(), bagID);
		
		for (RootNode rootNode : rootNodes){
			TreeTraversal traversal = new TreeTraversal(rootNode);
			Database db = new Database(rootNode);
			SubtreeBuilder builder = new SubtreeBuilder(db);
			
			while (true) {
				ArrayList<Integer> path = traversal.getNextPath();
				if (path == null)
					break;
				builder.buildTrees(path);
			}
		}
	}

}
