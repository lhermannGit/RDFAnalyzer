package subtreeGeneration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import treeGeneration.RootNode;
import treeGeneration.ChildNode;
import treeGeneration.Node;

public class TreeTraversal {

	Logger LOG = LogManager.getLogger(this.getClass().getName());
	private Queue<Node> queueNodes;
	private Queue<ArrayList<Integer>> queuePath;
	private Queue<ArrayList<Integer>> queuePairs;
	private List<ChildNode> children;
	private ArrayList<Integer> currentPath;
	
	public TreeTraversal(RootNode rootNode){
		queueNodes = new LinkedList<Node>();
		queuePairs = new LinkedList<ArrayList<Integer>>();
		queuePath = new LinkedList<ArrayList<Integer>>();
		children = new LinkedList<ChildNode>();
		currentPath = new ArrayList<Integer>();
		
		queueNodes.clear();
	    queuePath.clear();
	    queuePairs.clear();
	    children.clear();
		queueNodes.add(rootNode);
		queuePath.clear();
	    currentPath.clear();
	}
	
	// Returns null if all paths are exhausted
	public ArrayList<Integer> getNextPath() {
		ArrayList<Integer> helper;
		if (queuePairs.isEmpty()){
			while(true){
				if (queueNodes.isEmpty()){
					LOG.debug("returning null as next path");
					return null;
				}
				children = queueNodes.remove().getChildren();
				if (!queuePath.isEmpty())
					currentPath = queuePath.remove();
				else
					currentPath = null;
				if (children.size()==0)
					continue;
				for (int n=0; n<children.size(); n++){
					queueNodes.add(children.get(n));
					if (currentPath == null){
						helper = new ArrayList<Integer>();
					}
					else{					
						helper = new ArrayList<Integer>(currentPath);
					}
					helper.add(n);
					queuePath.add(helper);
					queuePairs.add(helper);
				}
				break;
			}
		}
		
		ArrayList<Integer> tmp = new ArrayList<Integer>(queuePairs.remove());
		LOG.debug("next path: " + tmp);
		return tmp;
	}
}
