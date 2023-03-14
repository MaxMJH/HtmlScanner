package net.greyareasolutions.maven.eclipse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class App {
    public static void main(String[] args) throws URISyntaxException {
    	HttpOptions options = new HttpOptions(new URI("https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/"));
    	ArrayList<String> subUris = new ArrayList<>();
    	subUris.add("Executors.html");
    	subUris.add("ExecutorService.html");
    	
    	MultiHtmlScanner multiHtmlScanner = new MultiHtmlScanner(options, subUris);
    	multiHtmlScanner.generateResponses();
    	HashMap<URI, String> responses = multiHtmlScanner.getResponses();
    	
    	for (int i = 0; i < responses.size(); i++) {
    		
    		System.out.println(responses.keySet().toArray()[i] + ":");
    		System.out.println(responses.get(responses.keySet().toArray()[i]).substring(0, 500) + "\n");
    	}
    }
}
