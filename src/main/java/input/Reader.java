package input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringEscapeUtils;

// supposed to be running in a thread - each with a different data crawl
public class Reader {

	// static Pattern regex = Pattern.compile("m/('.*?'|\".*?\"|\\S+)/g");
	static Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

	private File file;

	public Reader(File file) {
		this.file = file;
	}

	// consume the feed
	public List<DisjointSet> read() throws IOException {
		InputReduction reduce = new InputReduction(10000,
				StringEscapeUtils.escapeJava(file.getName()));
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new GZIPInputStream(new FileInputStream(file))));

		String line;

		while ((line = in.readLine()) != null) {
			List<String> matchList = new ArrayList<String>();
			Matcher regexMatcher = regex.matcher(line);
			while (regexMatcher.find()) {
				if (regexMatcher.group(1) != null) {
					// Add double-quoted string without the quotes
					matchList.add(regexMatcher.group(1).replace("\n", "")
							.trim());
				} else if (regexMatcher.group(2) != null) {
					// Add single-quoted string without the quotes
					matchList.add(regexMatcher.group(2).replace("\n", "")
							.trim());
				} else {
					// Add unquoted word
					matchList
							.add(regexMatcher.group().replace("\n", "").trim());
				}
			}

			reduce.consume(matchList.get(0), matchList.get(1), matchList.get(2));
		}
		in.close();
		reduce.commit();
		return reduce.getSets();

	}
}
