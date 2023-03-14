package net.greyareasolutions.maven.eclipse;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The class MultiHtmlScanner is used to send requests to a multitude of target URIs of the same
 * domain. Inside this class is a private class which allows for each URI to be ran on a separate
 * thread to speed up the process.
 * 
 * In order for the class to function as intended, the main URI must be the root URI, for example,
 * <i>http://www.test.com/</i> with the remaining URIs being along the lines of <i>login/</i> or
 * <i>login/login.php</i> for example.
 * 
 * @author Max Harris - mjh@greyareasolutions.net
 * @version v0.0.1
 * @since 12-03-2023
 */
public class MultiHtmlScanner {
	/*---- Fields ----*/
	/**
	 * Field which stores the options that need to be sent with the request.
	 */
	private HttpOptions options;
	
	/**
	 * Field which stores the sub URIs in which requests will be sent to.
	 */
	private List<String> subUris;
	
	/**
	 * Field which stores the executor that will run the separate threads for each request.
	 */
	private ExecutorService executor;
	
	/**
	 * Field which stores the responses of the requests.
	 */
	private HashMap<URI, String> responses;
	
	/*---- Constructor ----*/
	/**
	 * Core constructor that aims to initialise all declared fields of the class.
	 * The size of the executor pool is dependent on the amount of sub URIs.
	 * 
	 * @param options reference of {@link HttpOptions}.
	 * @param subUris array of all sub URIs where requests should be sent to.
	 */
	public MultiHtmlScanner(HttpOptions options, ArrayList<String> subUris) {
		this.options = options;
		this.subUris = subUris;
		this.executor = Executors.newFixedThreadPool(subUris.size());
		this.responses = new HashMap<>();
	}
	
	/*---- Method ----*/
	/**
	 * This method aims to send requests to all sub URIs and add their responses to a
	 * Map. Each mapping is that of URI --> HttpResponse<String>. In order to prevent
	 * threads attempting to add responses to the same index, a concurrent linked queue
	 * is used. 
	 * 
	 * If no sub URIs are available, it is recommended to use {@link HtmlScanner}.
	 */
	public void generateResponses() {
		// Check to see if there are actually any sub URIs.
		if (this.subUris.size() > 0) {
			// Create a concurrent linked queue so that collisions are avoided due to threads.
			ConcurrentLinkedQueue<HttpResponse<String>> queue = new ConcurrentLinkedQueue<>();
			
			// Get the current root URI.
			URI rootUri = this.options.getURI();
			
			// Iterate through each sub URI.
			for (int i = 0; i < this.subUris.size(); i++) {
				// Create a URI object which will hold the entire sub URI.
				URI currentUri;
				
				// Get a sub URI from the list.
				String subUri = this.subUris.get(i);
				
				// Attempt to create the entire sub URI.
				try {
					currentUri = new URI(rootUri.toString() + subUri);
				} catch (URISyntaxException e) {
					// Set URI to null - will need to be checked later on.
					currentUri = null;
					System.out.println("ERROR:\n" + e.getMessage() + " returned!" + "\nEXCEPTION:\n" + e.toString());
				}
				
				// Check to see if URI could be correctly parsed (not null).
				if(currentUri != null) {
					/*
					 * Create a new HttpOptions reference due to the fact that if the field 
					 * options is used, it holds the same reference, meaning the URI
					 * will be overwritten by the latest thread. However, only the URI should change,
					 * other parameters of the class can be kept the same.
					 */
					HttpOptions newOptions = new HttpOptions(currentUri, this.options.getCookie(), this.options.getHeaders(), this.options.getTimeout());
					
					// Send a HTTP request to the sub URI.
					this.executor.execute(new MultiHtmlScannerRunnable(newOptions, queue));
				}
			}
			
			// Attempt to prevent further threads from being created as well as wait for all threads to finish.
			try {
				// Block further threads from being created.
				this.executor.shutdown();
				
				// Allow a timer to wait for threads to finish task.
				this.executor.awaitTermination(60, TimeUnit.SECONDS);
				
				// Create a list to store all responses from queue.
				List<HttpResponse<String>> responseResults = new ArrayList<HttpResponse<String>>(queue);
				
				// Iterate through each response obtained from requests.
				for (int i = 0; i < responseResults.size(); i++) {
					// Get the URI.
					URI uri = responseResults.get(i).uri();
					
					// Get the URI's response.
					String response = responseResults.get(i).body();
					
					// Put each in the map.
					this.responses.put(uri, response);
				}
			} catch (InterruptedException e) {
				System.out.println("ERROR:\n" + e.getMessage() + " returned!" + "\nEXCEPTION:\n" + e.toString());
			}
		}
	}
	
	/*---- Getters ----*/
	/**
	 * Returns the class's responses from HTTP requests.
	 * 
	 * @return the class's responses from HTTP requests.
	 */
	public HashMap<URI, String> getResponses() {
		return this.responses;
	}
	
	/*---- Private Class ----*/
	/**
	 * This class is used to run the threads need to send a multitude of requests to a multitude
	 * of URIs. In order to actually send the requests {@link HtmlScanner} is used.
	 * 
	 * @author Max Harris - mjh@greyareasolutions.net
	 * @version v0.0.1
	 * @since 13-03-2023
	 */
	private class MultiHtmlScannerRunnable implements Runnable {
		/*---- Fields ----*/
		/**
		 * Fields which stores the options that need to be sent with the request.
		 */
		private HttpOptions options;
		
		/**
		 * Field which stores responses in a queue to prevent collisions. 
		 */
		private ConcurrentLinkedQueue<HttpResponse<String>> queue;
		
		/*---- Constructor ----*/
		/**
		 * Core constructor that aims to initialise the class's fields which will be used
		 * when sending requests to URIs.
		 * 
		 * @param options reference of {@link HttpOptions}.
		 * @param currentUri field which stores the current URI.
		 * @param queue queue that stores all HTTP responses.
		 */
		public MultiHtmlScannerRunnable(HttpOptions options, ConcurrentLinkedQueue<HttpResponse<String>> queue) {
			this.options = options;
			this.queue = queue;
		}
		
		/*---- Overridden Method ----*/
		/**
		 * Main run method overridden from Runnable. A {@link HtmlScanner} is initialised and
		 * a request is sent. The response is then added to the queue so that it can be added 
		 * to a list at a later date.
		 */
		@Override
		public void run() {
			// Create an instance of HtmlScanner to send the request.
			HtmlScanner htmlScanner = new HtmlScanner(this.options);
			
			// Add the outcome of the request to a queue.
			this.queue.add(htmlScanner.getResponse());
		}
	}
}
