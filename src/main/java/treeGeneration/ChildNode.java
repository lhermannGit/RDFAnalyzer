package treeGeneration;

import java.util.ArrayList;
import java.util.List;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ChildNode implements Node {

	Logger LOG = LogManager.getLogger(this.getClass().getName());
	private int name;
	private int predicate;
	private int rdfCount;
	private List<ChildNode> children;

	
	public ChildNode(int name, int predicate, int count) {
		this.name = name;
		this.predicate = predicate;
		this.rdfCount = count;
		this.children = new ArrayList<ChildNode>();
	}

	public int getName() {
		return this.name;
	}

	public int getPredicate() {
		return this.predicate;
	}

	public int getRdfCount(){
		return this.rdfCount;
	}
	public void addChildNode(ChildNode childNode) {
		if (!this.children.contains(childNode)){
			this.children.add(childNode);
			LOG.debug("added new Child " + childNode.getName() + " with predicate "
					+ childNode.getPredicate() + " to ChildNode " + this.name);
		}
	}

	public List<ChildNode> getChildren() {
		return this.children;
	}
	
	public void removeChildNode(ChildNode childNode){
		this.children.remove(childNode);
	}

	// look if subject already exists as an object in children
	// if found: create and add new ChildNode
	public boolean addIfInside(ChildNode newChildNode, int rdfSubject){
		boolean inserted = false;
		for (ChildNode child : this.children) {
			if ((!inserted) & (child.addIfInside(newChildNode, rdfSubject))){
				inserted = true;
			}
		}
		if ((this.name == rdfSubject) && !(this == newChildNode)){
			this.addChildNode(newChildNode);
			inserted = true;
		}
		return inserted;
	}

	// look if subject already exists as an object in children
	// if found:add newChildNode as a child and add 'this' to preventLoop
	public boolean addIfInside(ChildNode newChildNode, int rdfSubject, List<ChildNode> preventLoop){
		boolean inserted = false;
		for (ChildNode child : this.children) {
			if ((!inserted) & (child.addIfInside(newChildNode, rdfSubject, preventLoop))){
				inserted = true;
			}
		}
		if ((this.name == rdfSubject) && !(this == newChildNode)){
			this.addChildNode(newChildNode);
			preventLoop.add(this);
		}
		return inserted;
	}	
}
