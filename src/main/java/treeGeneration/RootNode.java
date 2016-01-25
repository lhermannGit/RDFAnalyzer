package treeGeneration;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RootNode implements Node {

	Logger LOG = LogManager.getLogger(this.getClass().getName());
	private int name;
	public int bagID;
	private List<ChildNode> children;
	// private int priority;

	public RootNode(int name, int bagID) {
		this.name = name;
		this.bagID = bagID;
		this.children = new ArrayList<ChildNode>();
		LOG.debug("added new Root " + name);
		// this.priority = 100;
	}

	public int getName() {
		return this.name;
	}

	// public int getPriority() {
	// return this.priority;
	// }

	public List<ChildNode> getChildren() {
		return this.children;
	}

	public void addChildNode(ChildNode childNode) {
		if (!this.children.contains(childNode)) {
			this.children.add(childNode);
			LOG.debug("added new Child " + childNode.getName() + " with predicate " + childNode.getPredicate()
					+ " to rootnode " + this.name);
		}
	}

	public void removeChildNode(ChildNode childNode) {
		this.children.remove(childNode);
	}

	// look if subject already exists as an object in this tree
	// if found: add newChildNode as a child
	public boolean addIfInside(ChildNode newChildNode, int rdfSubject, List<ChildNode> preventLoop) {
		boolean inserted = false;
		if (this.name == newChildNode.getName()) {
			// if newChildNode is added in this tree, parent ChildNode needs to
			// add itself to preventLoop
			for (ChildNode child : this.children) {
				if ((!inserted) & (child.addIfInside(newChildNode, rdfSubject, preventLoop))) {
					inserted = true;
				}
			}
		} else {
			// no need to prevent this kind of loops
			for (ChildNode child : this.children) {
				if ((!inserted) & (child.addIfInside(newChildNode, rdfSubject))) {
					inserted = true;
				}
			}
		}
		if (this.name == rdfSubject) {
			this.addChildNode(newChildNode);
			if (!inserted)
				inserted = true;
		}
		return inserted;
	}

}
