package input;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;

public class DisjointSet {

	TIntArrayList set = new TIntArrayList();

	public boolean has(int id) {
		int i = 0;
		while (i < set.size()) {
			// present as subject
			if (set.get(i) == id)
				return true;
			// present as object
			else if (set.get(i + 2) == id)
				return true;
			else
				i = i + 4;
		}
		return false;
	}

	public void add(int subj, int pred, int obj) {
		set.add(subj);
		set.add(pred);
		set.add(obj);
		set.add(0);
	}

	public void merge(DisjointSet set) {
		this.set.add(set.getSet().toArray());
		set.close();
	}

	public TIntArrayList getSet() {
		return set;
	}

	public void setFrequencies(TIntIntHashMap map) {
		int i = 0;
		while (i < set.size()) {
			set.set(i + 3, map.get(set.get(i + 1)));
			i += 4;
		}
	}

	public void print() {
		int i = 0;
		while (i < set.size()) {
			System.out.println(set.get(i) + " " + set.get(i + 1) + " " + set.get(i + 2) + " " + set.get(i + 3) + " ");
			i += 4;
		}
		System.out.println(set.size());
	}

	public void close() {
		set.clear();
	}

	public int getSize() {
		return set.size();
	}

}
