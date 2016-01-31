package threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Threadpool {
	
	Logger LOG = LogManager.getLogger(this.getClass().getName());
	static int threadAmount = 5;
	ExecutorService executor;
	
	public Threadpool(){
		executor = Executors.newFixedThreadPool(threadAmount);
	}
	
	public void execute(BagWorker worker){
		LOG.debug("Bag " + worker.bagID + " queued in Threadpool!");
		executor.execute(worker);
	}
	
	public void endJobQueue(){
		executor.shutdown();
        while (!executor.isTerminated()) {
        }
	}

}
