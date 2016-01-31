package threads;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import input.DisjointSet;
import subtreeGeneration.SubtreeBuilder;
import subtreeGeneration.TreeTraversal;
import treeGeneration.RootNode;
import treeGeneration.Window;
import util.Database;

public class BagWorker implements Runnable{

	Logger LOG = LogManager.getLogger(this.getClass().getName());
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
		LOG.info("processing bag: " + bagID + " in crawl: " + crawlID + " with a bag size of: " + bag.getSize());
		Window window = new Window();
		List<RootNode> rootNodes = window.buildTree(bag.getSet().toArray(), bagID);
		
		for (RootNode rootNode : rootNodes){
			TreeTraversal traversal = new TreeTraversal(rootNode);
			Database.INSTANCE.serialize(rootNode);
			SubtreeBuilder builder = new SubtreeBuilder(rootNode.bagID);
			
			while (true) {
				ArrayList<Integer> path = traversal.getNextPath();
				if (path == null)
					break;
				builder.buildTrees(path);
			}
		}
		
		LOG.info("finished processing bag: " + bagID + " in crawl: " + crawlID + ". Bag had " + rootNodes.size() + " Trees generated");
	}

}
