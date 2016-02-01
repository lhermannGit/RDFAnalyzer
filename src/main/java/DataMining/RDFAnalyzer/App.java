package DataMining.RDFAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import input.DisjointSet;
import input.Reader;
import threads.BagWorker;
import threads.Threadpool;

import java.net.URL;
import java.net.URLClassLoader;

public class App {
	
	
	
	public static void main(String[] args) {
		
		ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
        	System.out.println(url.getFile());
        }
		
		Threadpool threadpool = new Threadpool();
		Logger LOG = LogManager.getLogger(App.class.getName());
		
		PrintWriter pw;
		try {
			pw = new PrintWriter("logs/debug.log");
			pw.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		String crawl = "data0";
		String homeDir = System.getProperty("user.home");
		Reader reader = new Reader(
				new File(/* homeDir+ FilePathFormatter.setSeparators( */crawl/* ) */));

		
		try {
			LOG.info("Start processing crawl " + crawl);
			List<input.DisjointSet> sets = reader.read();
			LOG.info("Finished building all bags for crawl " + crawl);
			int bagID = 0;
			for (DisjointSet set : sets) {
				threadpool.execute(new BagWorker(set, bagID, 0));
				bagID++;
			}
			
			threadpool.endJobQueue();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
