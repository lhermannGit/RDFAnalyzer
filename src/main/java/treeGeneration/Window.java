package treeGeneration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
//import java.io.BufferedReader;
//import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import input.DisjointSet;
import input.Reader;
import util.ArraySerializer;
import util.FilePathFormatter;

public class Window {

	Logger LOG = LogManager.getLogger(this.getClass().getName());
	public static final int WINDOW_SIZE = 100;

	private int size;
	private List<RootNode> rootNodes;
	private Iterator<RootNode> rootNodesIterator;



	/**
	 * Arguments: Path to input file
	 *
	 * @param args
	 * @throws IOException
	 */

	// public void createTrees(int[] rdfQuads) throws IOException {
	public static void main(String[] args) throws IOException {
		// input looks like [subject,predicate,object,count,......]

		int[] testArray = null;
		ArraySerializer arraySerializer = new ArraySerializer();

		String homeDir = System.getProperty("user.home");
		Reader reader = new Reader(new File(
				homeDir + FilePathFormatter.setSeparators("/research-lab/data/btc2014/crawls/06/data.nq-0.gz"))); // TODO
																													// path
		try {
			List<DisjointSet> sets = reader.read();
			DisjointSet disjointSet = sets.get(0);
			System.out.println(disjointSet.getSize());
			testArray = disjointSet.getSet().toArray();
			// System.out.println(disjointSet.getSet());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		arraySerializer.serializeArray(homeDir + FilePathFormatter.setSeparators("/research-lab/data/temp/array.ser"), // TODO
																														// path
				testArray);
		testArray = arraySerializer
				.deserializeArray(homeDir + FilePathFormatter.setSeparators("/research-lab/data/temp/array.ser")); // TODO
																													// path

		// old test arrays
		// testArray = new int[] {1,2,3,1, 1,4,5,1, 9,3,2,1,
		// 3,1,9,1, 1,9,6,1, 11,12,13,1,
		// 13,1,14,1, 13,2,15,1, 6,3,2,1,
		// 14,16,2,1, 2,3,11,1, 13,3,7,1};
		// testArray = new int[] {1,99,3,1, 11,98,5,1, 9,97,2,1,
		// 3,96,9,1, 1,95,6,1, 11,94,13,1,
		// 13,93,14,1,13,92,15,1, 6,91,2,1,
		// 14,90,2,1, 2,89,11,1, 13,87,7,1};
		// testArray = new int[] {1,99,3,1, 3,98,5,1, 5,97,6,1,
		// 3,96,8,1, 1,88,3,1};
		Window window = new Window();
		List<RootNode> rootNodes = window.buildTree(testArray, 0);
		System.out.println("Number of rootNodes: " + rootNodes.size());

		int rootNodeCounter = 0;
		for (RootNode rootNode : rootNodes) {

			// RootNode rootNode=rootNodes.get(20);
			// RootNode rootNode=rootNodes.get(0);
			rootNodeCounter++;
			String tempDir = homeDir + FilePathFormatter.setSeparators("/research-lab/data/temp/subtrees/");
			BufferedWriter predicateTreeTripleWriter = new BufferedWriter(
					new FileWriter(new File(tempDir + rootNodeCounter + "-predicate-trees.txt")));
			BufferedWriter tripleTreeWriter = new BufferedWriter(
					new FileWriter(new File(tempDir + rootNodeCounter + "-triple-trees.txt")));
			System.out.println("creating subtrees for tree " + rootNodeCounter + " of " + rootNodes.size());
			// TreePrinter.printTree(rootNode);
		}
	}

	public Window() {

	}

	public List<RootNode> buildTree(int[] inputArray, int bagID) {

		int rdfQuadsCount = 0;

		rootNodes = new ArrayList<RootNode>();
		List<ChildNode> preventLoop = new ArrayList<ChildNode>();
		int rdfSubject;
		int rdfObject;
		boolean inserted = false;
		RootNode newRootNode = null;
		ChildNode newChildNode = null;
		size = 0;
		while (rdfQuadsCount < inputArray.length) {
			// just for output/testing
			LOG.debug("----- line read: " + rdfQuadsCount / 4 + " -----");

			rdfSubject = inputArray[rdfQuadsCount];
			rdfObject = inputArray[rdfQuadsCount + 2];

			newChildNode = new ChildNode(rdfObject, inputArray[rdfQuadsCount + 1], inputArray[rdfQuadsCount + 3]);
			// look through all nodes in all trees if new subject already
			// exists as an object and if found, add newChildNode there
			for (RootNode rootnode : rootNodes) {
				if ((!inserted) & (rootnode.addIfInside(newChildNode, rdfSubject, preventLoop))) {
					inserted = true;
				}
			}

			// if we inserted newChildNode only inside a tree with a
			// rootNode.name == rdfObject,
			// we don't need/want to combine trees
			if (!inserted) {
				if (preventLoop.isEmpty()) {
					// if newChildNode was not inserted yet, create new RootNode
					// and
					// link newChildNode to it
					newRootNode = new RootNode(rdfSubject, bagID);
					rootNodes.add(newRootNode);
					newRootNode.addChildNode(newChildNode);
					size = size + 2;
					combineTrees(newChildNode, rdfSubject, preventLoop);
				}
			} else {
				size++;
				combineTrees(newChildNode, rdfSubject, preventLoop);
			}
			preventLoop.clear();
			rdfQuadsCount = rdfQuadsCount + 4;
			inserted = false;
		}
		// br.close();
		System.out.println("size = " + size);

		return this.rootNodes;
	}

	// combine a tree with added Nodes if needed
	public void combineTrees(ChildNode newChildNode, int rdfSubject, List<ChildNode> preventLoop) {
		RootNode root;
		rootNodesIterator = rootNodes.iterator();
		RootNode rootToDelete = null; // try to get rid of

		// look for rootNodes with the same name as rdfObject
		while (rootNodesIterator.hasNext()) {
			root = rootNodesIterator.next();
			if ((newChildNode.getName() == root.getName()) && !(newChildNode.getName() == rdfSubject)) {
				LOG.debug("[start combining trees]");
				// remove links if needed to prevent loops
				for (ChildNode child : preventLoop) {
					child.removeChildNode(newChildNode);
					LOG.debug("deleted link between ChildNode " + newChildNode.getName() + " and parent ChildNode "
							+ child.getName());
				}
				// link all children of old rootNode to newChildNode and
				// delete old rootNode
				for (ChildNode child : root.getChildren()) {
					newChildNode.addChildNode(child);
					LOG.debug("linked child " + child.getName() + " to node " + newChildNode.getName());
				}
				rootToDelete = root;
			}
		}
		// TODO: find a better way to delete root inside
		if (rootToDelete != null) {
			LOG.debug("deleted RootNode " + rootToDelete.getName());
			rootNodes.remove(rootToDelete);
			rootToDelete = null;
		}
	}
}
