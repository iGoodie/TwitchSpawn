package igoodie.twitchspawn.tracer;

import com.google.gson.JsonObject;

import igoodie.twitchspawn.utils.FileUtils;

public abstract class JsonTracer {
	
	private Thread tracerThread;
	private String threadName;
	private int threadPause = 30;
	
	private volatile boolean running = false;
	private volatile int tracked = 0;
	private String jsonURL;

	/* Constructors and Getters/Setters */
	public JsonTracer(String threadName, String jsonURL, int threadPause) {
		this.threadName = threadName;
		this.jsonURL = jsonURL;
		this.threadPause = threadPause;
	}

	public JsonTracer(String jsonURL) {
		this.jsonURL = jsonURL;
	}
	
	public boolean isRunning() {
		return running;
	}

	public int getTrackedCount() {
		return tracked;
	}

	public String getURL() {
		return jsonURL;
	}
	
	protected JsonObject fetch() {
		return FileUtils.fetchJson(jsonURL);
	}
	
	/* Igniters */
	public void start() {
		if(tracerThread == null) { // Initial run
			running = true;
			
			// Start thread
			tracerThread = new Thread(()->{
				preTrace();
				while(running) {
					JsonObject fetchedJSON = fetch();
					if(fetchedJSON!=null) {
						trace(fetchedJSON);
						tracked++;
						freeze(threadPause); // default: 30 sec break
					}
				}
			}, this.threadName);
			tracerThread.start();
		}
		else if(running || tracerThread.isAlive()) {
			throw new IllegalStateException("Tracer is already running.");
		}
	}
	
	public void stop() {
		if(!running || (tracerThread!=null && !tracerThread.isAlive())) {
			throw new IllegalStateException("Tracer is already stopped.");
		}
		
		running = false;
		tracerThread = null;
	}
	
	public void freeze(int timeSec) {
		try {
			synchronized (tracerThread) { 
				tracerThread.wait(timeSec * 1000); 
			}
		}
		catch (Exception e) {}
	}
	
	/* Tracing methods */
	/**
	 * Method to handle pre-fetching phase.
	 * Executed only once before the continuous run loop.
	 */
	protected void preTrace() {}
	
	/**
	 * Method to handle fetched JSON object.
	 * The JSON data is guarantied to be not null.
	 * @param fetchedJSON NotNull JSON fetched from the URL
	 */
	protected abstract void trace(JsonObject fetchedJSON);
}
