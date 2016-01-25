package DataMining.RDFAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import input.DisjointSet;
import input.Reader;
import threads.BagWorker;
import threads.Threadpool;
import util.FilePathFormatter;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Threadpool threadpool = new Threadpool();
    	String crawl = "\\workspace\\java\\Uni\\data0";
    	String homeDir = System.getProperty("user.home");
		Reader reader=new Reader(new File(homeDir+FilePathFormatter.setSeparators(crawl)));
		
		try {
			List<input.DisjointSet>sets=reader.read();
			int bagID = 0;
			for (DisjointSet set :sets){
				System.out.println(set.getSize());
				threadpool.execute(new BagWorker(set, bagID, 0));
				bagID++;
			}
			
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
