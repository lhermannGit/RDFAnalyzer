package threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Threadpool {
	
	static int threadAmount = 5;
	ExecutorService executor;
	
	public Threadpool(){
		executor = Executors.newFixedThreadPool(threadAmount);
	}
	
	public void execute(Runnable runner){
		executor.execute(runner);
	}

}
