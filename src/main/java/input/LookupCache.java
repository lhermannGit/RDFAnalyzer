package input;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringEscapeUtils;


import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import input.util.CharArrHashingStrategy;
import util.Database;

public class LookupCache {

	public enum Table {
		objects, predicates
	}

	public enum Cache {
		old, recent
	}

	protected TObjectIntCustomHashMap<char[]> objects_recent;
	protected TObjectIntCustomHashMap<char[]> objects_old;
	protected int object_id = Integer.MIN_VALUE + 1;

	protected int upperBound;
	protected boolean databaseEmpty = true;
	protected Database db;
	protected String table;

	public LookupCache(int upperBound, Table table, String prefix, Database db) {
		objects_recent = new TObjectIntCustomHashMap<char[]>(CharArrHashingStrategy.create(), upperBound / 2);
		objects_old = new TObjectIntCustomHashMap<char[]>(CharArrHashingStrategy.create(), upperBound / 2);
		this.upperBound = upperBound / 2;
		this.db = db;

		switch (table) {
		case objects:
			this.table = prefix + "objects";
			break;
		case predicates:
			this.table = prefix + "predicates";
			break;
		}
		db.query("DROP TABLE " + this.table+";");
		db.query("CREATE TABLE " + this.table + " (k text, v int );");
	}

	public int get(char[] tupleElem) {
		Integer result = objects_recent.get(tupleElem);
		if (!(result == objects_recent.getNoEntryValue())) {
			// System.out.println(table + ": Object within recent objects");
			return result;
		}
		result = objects_old.get(tupleElem);
		if (!(result == objects_old.getNoEntryValue())) {
			// System.out.println(table + ": Object within old objects");
			return result;
		}
		if (!databaseEmpty) {
			// System.out.println(table + ": looking in DB");
			ResultSet res = db.query("SELECT v FROM " + table + " WHERE k ='"
					+ StringEscapeUtils.escapeJava(String.copyValueOf(tupleElem).replace("\n", "")) + "'");
			if (res == null)
				return Integer.MIN_VALUE;

			try {
				while (res.next()) {
					// System.out.println("found in DB");
					return res.getInt("v");
				}
			} catch (SQLException e) {
				System.out.println("Problem retrieving value from table!");
				e.printStackTrace();
			}
		}
		if (objects_recent.size() >= upperBound)
			if (objects_old.isEmpty()) {
				TObjectIntCustomHashMap<char[]> tmp = objects_old;
				objects_old = objects_recent;
				objects_recent = tmp;
			} else {
				flush(Cache.old);
				objects_old.clear();
				TObjectIntCustomHashMap<char[]> tmp = objects_old;
				objects_old = objects_recent;
				objects_recent = tmp;
			}

		objects_recent.put(tupleElem, object_id);
		return object_id++;
	}

	public int getObjectsOldSize() {
		return objects_old.size();
	}

	public int getObjectsSize() {
		return objects_recent.size();
	}

	public void flush(Cache cache) {
		// System.out.println("flushing " + cache.toString());
		TObjectIntIterator<char[]> iterator = null;
		switch (cache) {
		case old:
			iterator = objects_old.iterator();
			break;
		case recent:
			iterator = objects_recent.iterator();
		}
		int i = 0;
		String query = "INSERT INTO " + table + " VALUES ";

		if (!iterator.hasNext())
			return;

		// TODO check iterator
		while (iterator.hasNext()) {
			if (i > 0)
				query = query + ",";
			iterator.advance();
			query = query + "(\"" + StringEscapeUtils.escapeJava(String.copyValueOf(iterator.key())) + "\", "
					+ iterator.value() + ")";
			iterator.remove();
			i++;
		}

		query = query + ";";
		// System.out.println(query);
		// System.out.println("Inserting :" + i + "elements into " + table);
		if (i > 0)
			db.query(query);
		databaseEmpty = false;
	}

	public void close() {
		flush(Cache.old);
		flush(Cache.recent);
		objects_recent.clear();
		objects_old.clear();
	}
}