package net.greyareasolutions.maven.eclipse;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

/**
 * The class HtmlScannerMain is used to run the entire program. The class's {@link main(String[] args} method
 * aims to take arguments and use them to construct HTTP requests and gather their responses
 * to see if the HTML contains any comments or hidden attributes.
 * 
 * An example of the command being used can be seen below:
 * <pre>
 * java HtmlScannerMain -uri http://www.example.com -cookie PHPSESSID=id;... -header test=header;another=header;... -timeout 40s -c -hi
 * </pre>
 * 
 * @author Max Harris - mjh@greyareasolutions.net
 * @version v0.0.1
 * @since 12-03-2023
 */
public class HtmlScannerMain {
	/**
	 * This method is used to parse the program's arguments and deal with them
	 * accordingly.
	 * 
	 * @param args program's arguments.
	 */
    public static void main(String[] args) {
    	// Check to see if there are any arguments passed.
    	if (args.length > 1) {
    		// Booleans used to see if main flags are used (comments and hidden attributes).
    		boolean searchComments = false;
    		boolean searchHidden = false;
    		
    		// List of registered flags, any others supplied are incorrect.
    		List<String> flags = new ArrayList<>(Stream.of("-uri", "-subUris", "-cookie", "-header", "-headers", "-timeout", "-c", "-hi", "-o").toList());
    		
    		// Instantiate HttpOptions so that each flag will add to construction.
    		HttpOptions httpOptions = new HttpOptions();
    		
    		// If sub-URIs are needed, create an array to store them.
    		ArrayList<String> subUris = new ArrayList<>();
    		
    		// Iterate through each argument.
    		for (int i = 0; i < args.length; i++) {
    			// Check each argument to see if they are a flag.
    			switch(args[i]) {
    				// -uri flag.
    				case "-uri":
    					// Check to see if flag has a proceeding value.
    					if (i + 1 != args.length && !flags.contains(args[i + 1])) {
    						// Create a variable for the target URI.
    						URI uri;
    						
    						// Attempt to parse the URI.
    						try {
    							// Set the URI to the argument proceeding the -uri flag.
								uri = new URI(args[i + 1]);
							} catch (URISyntaxException e) {
								// Set the URI to null indicating failure to parse.
								uri = null;
								System.out.println("ERROR:\n" + e.getMessage() + " returned!" + "\nEXCEPTION:\n" + e.toString());
								return;
							}
    						
    						// Check if the URI parsed correctly.
    						if (uri != null) {
    							// Add the URI to the reference of HttpOptions.
    							httpOptions.setURI(uri);
    						} 
    						
    						// Increment the iteration so that it ignores the flag value.
    						i++;
    					} else {
    						// If no value is present for the flag, exit program.
    						System.out.println("A URI is expected after the -uri flag!");
    						return;
    					}
    					
    					break;
    				// -subUris flag.
    				case "-subUris":
    					// Check to see if flag has a proceeding value.
    					if (i + 1 != args.length && !flags.contains(args[i + 1])) {
    						// Attempt to read a file that contains sub-URIs.
    						try(FileInputStream file = new FileInputStream(args[i + 1]) ; DataInputStream in = new DataInputStream(file) ; BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
        			    		// Set a variable that represents each line in the file.
    							String subUri;
    							
    							// Iterate through each line in the file until EOL.
        			    		while((subUri = br.readLine()) != null) {
        			    			// Add sub-URI to array.
        			    			subUris.add(subUri);
        			    		}
        			    	} catch(IOException e) {
        			    		// If the any exceptions concerning the file occurred, notify and exit program.
        			    		System.out.println("ERROR:\n" + e.getMessage() + " returned!" + "\nEXCEPTION:\n" + e.toString());
								return;
        			    	}
    						
    						// Increment the iteration so that it ignores the flag value.
    						i++;
    					} else {
    						// If no value is present for the flag, exit program.
    						System.out.println("A file containing sub URIs is expected after the -subUris flag!");
    						return;
    					}
    					
    					break;
    				// -cookie flag.
    				case "-cookie":
    					// Check to see if flag has a proceeding value.
    					if (i + 1 != args.length && !flags.contains(args[i + 1])) {
    						httpOptions.setCookie(args[i + 1]);
    						
    						// Increment the iteration so that it ignores the flag value.
    						i++;
    					} else {
    						// If no value is present for the flag, exit program.
    						System.out.println("A cookie is expected after the -cookie flag!");
    						return;
    					}
    					
    					break;
    				// -header flag.
    				case "-header":
    					// Check to see if flag has a proceeding value.
    					if (i + 1 != args.length && !flags.contains(args[i + 1])) {
    						// Check to see if argument is in form x=y;a=b...
    						if (Pattern.matches(".*=.*", args[i + 1])) {
    							// Split the headers if more than header is supplied.
    							String[] headers = args[i + 1].split(";");
    							
    							// Check if there are more than one headers present.
    							if (headers.length > 1) {
    								// Iterate through each header.
    								for (int j = 0; j < headers.length; j++) {
    									// Split the individual header into name and value.
    									String[] header = args[i + 1].split("=");
    									
    									// Add the header in form HeaderName: HeaderValue.
    									httpOptions.addHeader(header[0], header[1]);
    								}
    							} else {
    								// Insinuates that there is only one header, split into name and value.
    								String[] header = args[i + 1].split("=");
    								
    								// Add the header in form HeaderName: HeaderValue.
    								httpOptions.addHeader(header[0], header[1]);
    							}
    							
    							// Increment the iteration so that it ignores the flag value.
    							i++;
    						} else {
    							// If the header is not in the correct format, notify and exit program.
    							System.out.println("Invalid header format!");
    							return;
    						}
    					} else {
    						// If no value is present for the flag, exit program.
    						System.out.println("A header is expected after the -header flag!");
    						return;
    					}
    					
    					break;
    				// -headers flag.
    				case "-headers":
    					// Check to see if flag has a proceeding value.
    					if (i + 1 != args.length && !flags.contains(args[i + 1])) {
    						// Attempt to read a file that contains headers.
    						try(FileInputStream file = new FileInputStream(args[i + 1]) ; DataInputStream in = new DataInputStream(file) ; BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
    							// Set a variable that represents each line in the file.
    							String currentLine;
    							
    							// Iterate through each line in the file until EOL.
        			    		while((currentLine = br.readLine()) != null) {
        			    			// Check to see if headers are in form x=y;a=b...
        			    			if (Pattern.matches(".*=.*", currentLine)) {
        			    				// Split the individual header into name and value.
            							String[] header = currentLine.split("=");
            							
            							// Add the header in form HeaderName: HeaderValue.
            							httpOptions.addHeader(header[0], header[1]);
            						} else {
            							// If the header is not in the correct format, notify and exit program.
            							System.out.println("Invalid header format!");
            							return;
            						}
        			    		}
        			    	} catch(IOException e) {
        			    		// If the any exceptions concerning the file occured, notify and exit program.
        			    		System.out.println("ERROR:\n" + e.getMessage() + " returned!" + "\nEXCEPTION:\n" + e.toString());
								return;
        			    	}
    						
    						// Increment the iteration so that it ignores the flag value.
    						i++;
    					} else {
    						// If no value is present for the flag, exit program.
    						System.out.println("A file containing headers is expected after the -headers flag!");
    						return;
    					}
    					
    					break;
    				// -timeout flag.
    				case "-timeout":
    					// Check to see if flag has a proceeding value.
    					if (i + 1 != args.length && !flags.contains(args[i + 1])) {
    						// Check to see if argument is in form [0-9]s.
    						if (Pattern.matches("^[0-9]+s$", args[i + 1])) {
    							// Add the timeout to the options, also remove the s suffix.
    							httpOptions.setTimeout(Duration.ofSeconds(Long.parseLong(args[i + 1].replace("s", ""))));
    						} else {
    							// If the timeout is not in the correct format, notify and exit program.
    							System.out.println("Invalid timeout format!");
    							return;
    						}
    						
    						// Increment the iteration so that it ignores the flag value.
    						i++;
    					} else {
    						// If no value is present for the flag, exit program.
    						System.out.println("A timeout is expected after the -timeout flag!");
    						return;
    					}
    					
    					break;
    				// -c flag.
    				case "-c":
    					// If flag is present in arguments, set a flag.
    					searchComments = true;
    					break;
    				// -hi flag.
    				case "-hi":
    					// If flag is present in arguments, set a flag.
    					searchHidden = true;
    					break;
    				// -random-agent flag.
    				case "-random-agent":
    					// Create an instance of random.
    					Random rand = new Random();
    					
    					// Get all values from UserAgents enum.
    					UserAgents[] userAgents = UserAgents.values();
    					
    					// Select a random integer.
    					int randomNumber = rand.nextInt(userAgents.length);
    					
    					// Select a random user-agent.
    					UserAgents randomUserAgent = userAgents[randomNumber];
    					
    					// Add user-agent to options.
    					httpOptions.addHeader("User-Agent", randomUserAgent.toString());
    					
    					break;
    				// Default.
    				default:
    					// Notify that a flag was incorrect and exit program.
    					System.out.println("Invalid Flag!");
    					return;
    			}
    		}
    		
    		// Check to see if sub-URIs are present in arguments.
    		if (subUris.size() > 1) {
    			// Create a MultiHtmlScanner with generated options and required sub-URIs.
				MultiHtmlScanner multiHtmlScanner = new MultiHtmlScanner(httpOptions, subUris);
				
				// Generate HTTP requests, send them.
				multiHtmlScanner.generateResponses();
				
				// Gather responses from HTTP requests.
				HashMap<URI, String> responses = multiHtmlScanner.getResponses();
		    	
				// Iterate through each response.
		    	for (int j = 0; j < responses.size(); j++) {
		    		// Get the current URI.
		    		URI currentURI = (URI) responses.keySet().toArray()[j];
		    		
		    		// Parse the response corresponding to the current iteration's URI.
		    		Document html = Jsoup.parse(responses.get(currentURI));
		    		
		    		// Print the current URI.
		    		System.out.println(responses.keySet().toArray()[j] + ":");		    		
			
		    		// Print the response, depending on flags used (comments and / or hidden attributes).
		    		checkJSoupFlags(searchComments, searchHidden, html);
		    	}
			} else {
				// Create a HtmlScanner with generated options.
				HtmlScanner htmlScanner = new HtmlScanner(httpOptions);
				
				// Gather response from HTTP request.
				HttpResponse<String> response = htmlScanner.getResponse();
				
				// Print the current URI.
				System.out.println(response.uri() + ":");
				
				// Parse the response of HTTP request.
				Document html = Jsoup.parse(response.body());
	    		
				// Print response, depending on flags used (comments and / or hidden attributes).
				checkJSoupFlags(searchComments, searchHidden, html);
			}
    	} else {
    		// Print an example usage of the program.
    		System.out.println("This script takes the following flags as input:");
    		System.out.println("\t-uri: The URI you want to connect to (if multiple sub-URIs are used, this must be set at the root URI).");
    		System.out.println("\t-subUris: A file containing all sub-URIs, these must be on seperate lines, if any.");
    		System.out.println("\t-cookie: The cookie you want to include in your request, if any. Must be in form, cookieName=cookieValue OR cookieName=cookieValue;...");
    		System.out.println("\t-header: A single or list of HTTP headers you want to include in your request(s), if any. Must be in same form as -cookie above.");
    		System.out.println("\t-headers: A file containing all headers, these must be on seperate lines, if any.");
    		System.out.println("\t-timeout: The timout for the connection in seconds, if any. Must be in the form 0-9s.");
    		System.out.println("\t-c: Used to find all comments in each HTTP request's response.");
    		System.out.println("\t-hi: Used to find all hidden attributes in each HTTP request's response.");
    		System.out.println("\t-random-agent: Used to generate a random user-agent for each request, if needed.");
    		System.out.println("\nTo use this script, simply specify the appropriate flags on the command line.");
    		System.out.println("Example Usage:");
    		System.out.println("\tjava HtmlScannerMain -uri http://www.example.com -cookie PHPSESSID=sessID;... -header test=header;another=header;... -timeout 40s -c -hi -random-agent");
    	}
    }
    
    /**
     * This method checks to see if any of the comments or hidden attributes
     * flags have been used in arguments. 
     * 
     * @param searchComments flag insinuating the usage of -c flag.
     * @param searchHidden flag insinuating the usage of -hi flag.
     * @param html value of HTTP request's response. 
     */
    private static void checkJSoupFlags(boolean searchComments, boolean searchHidden, Document html) {
    	// Check to see which flags have been set.
    	if (searchComments && searchHidden) {
    		// Both flags have been set, therefore get comments and hidden attributes from current response.
			getComments(html);
			getHiddenInputs(html);
		} else if (searchComments) {
			// Only -c flag has been set, therefore get comments from current response.
			getComments(html);
		} else if (searchHidden) {
			// Only -hi flag has been set, therefore get comments from current response.
			getHiddenInputs(html);
		} else {
			// Notify that the URI does not contain comments or hidden attributes.
			System.out.println("This URI does not contain any comments or hidden attributes!");
		}
    }
    
    /**
     * This method attempts to find the comments in the specified
     * HTTP request's response.
     * 
     * @param html value of HTTP request's response.
     */
    private static void getComments(Document html) {
    	// Get all elements of the HTTP request's response.
    	Elements elements = html.getAllElements();
    	
    	// Iterate through each element.
		for (Element element : elements) {
			// Iterate through each elements child node.
			for (Node child : element.childNodes()) {
				// Check to see if the child node is a comment.
				if (child.nodeName().equals("#comment")) {
					// Print the comment.
					System.out.println(((Comment) child).getData());
				}
			}
		}
    }
    
    /**
     * This method attempts to find the hidden attributes in the
     * specified HTTP request's response.
     * 
     * @param html value of HTTP request's response.
     */
    private static void getHiddenInputs(Document html) {
    	// Get all elements of the HTTP request's repsonse that have a hidden attribute.
    	Elements hiddenInputs = html.select("[type=hidden]");
    	
    	// Iterate through each element.
    	for (Element hiddenInput : hiddenInputs) {
    		// Print the entire hidden comment.
    		System.out.println(hiddenInput);
    	}
    }
}
