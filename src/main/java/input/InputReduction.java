package input;

import java.util.ArrayList;

import util.Database;
import gnu.trove.map.hash.TIntIntHashMap;
import input.LookupCache.Cache;
import input.LookupCache.Table;

public class InputReduction {

	TIntIntHashMap preds_freq = new TIntIntHashMap();

	ArrayList<DisjointSet> sets = new ArrayList<DisjointSet>();

	LookupCache predicates;
	LookupCache objects;

	public InputReduction(int upperBound, String prefix) {
		Database db = new Database(null);
		predicates = new LookupCache(upperBound, Table.predicates, prefix, db);
		objects = new LookupCache(upperBound, Table.objects, prefix, db);
	}

	private int existsWithinSets(int id) {
		int offset = 0;

		// TODO traverse in reverse order instead(check)
		for (DisjointSet set : sets) {
			if (set.has(id))
				return offset;
			offset++;
		}
		return -1;
	}

	public void consume(String subj, String pred, String obj) {

		// Subject
		char[] c = subj.toCharArray();
		// System.out.println("Getting ID for " + String.copyValueOf(c));
		int subjID = objects.get(c);

		// Predicate
		c = pred.toCharArray();
		// System.out.println("Getting ID for " + String.copyValueOf(c));
		int predID = predicates.get(c);
		preds_freq.adjustOrPutValue(predID, 1, 1);

		// Object
		c = obj.toCharArray();
		// System.out.println("Getting ID for " + String.copyValueOf(c));
		int objID = objects.get(c);
		if (objID == Integer.MIN_VALUE)
			return;

		int subjSet = existsWithinSets(subjID);
		int objSet = existsWithinSets(objID);

		if (subjSet == -1 && objSet == -1) {
			sets.add(new DisjointSet());
			sets.get(sets.size() - 1).add(subjID, predID, objID);
		} else if (subjSet >= 0 && subjSet == objSet) {

			sets.get(subjSet).add(subjID, predID, objID);
			// either subjBits or objBits has more than one Bit
		} else {
			DisjointSet set;

			if (subjSet >= 0 && objSet >= 0) {
				set = sets.get(subjSet);
				set.merge(sets.get(objSet));
				sets.remove(objSet);
			} else if (subjSet >= 0)
				set = sets.get(subjSet);
			else
				set = sets.get(objSet);
			set.add(subjID, predID, objID);
		}
	}

	public ArrayList<DisjointSet> getSets() {
		for (DisjointSet set : sets) {
			set.setFrequencies(preds_freq);
		}
		return sets;
	}

	public void commit() {
		predicates.flush(Cache.old);
		predicates.flush(Cache.recent);
		objects.flush(Cache.old);
		objects.flush(Cache.recent);
	}

	public void close() {
		predicates.close();
		objects.close();
		preds_freq.clear();
	}
}