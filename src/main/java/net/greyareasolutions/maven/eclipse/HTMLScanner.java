package net.greyareasolutions.maven.eclipse;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.net.http.HttpClient.Version;

/**
 * Need to get URI String - MUST.
 * Need to get headers - OPTIONAL.
 * Need to get HTTP Version - OPTIONAL.
 * Need to get Timeout duration - OPTIONAL.
 * Need to get Cookie - OPTIONAL.
 * 
 * Need to make a HttpClient.
 * Need to make a HttpRequest.
 * Need to make a HttpResponse.
 * 
 * @author Max
 *
 */

public class HTMLScanner {
	private URI uri;
	private HashMap<String, String> headers;
	private Duration timeout;
	private HttpCookie cookie;
	private HttpClient client;
	private HttpRequest request;
	private HttpResponse<String> response;
	
	public HTMLScanner(String uri) {
		try {
			this.uri = new URI(uri);
		} catch (URISyntaxException e) {
			System.out.println("AN ERROR HAS OCCURED!\n" + e.getMessage() + "\nCAUSED BY: " + e.getReason());
			
			// Set to null otherwise.
			this.uri = null;
		}
		this.headers = new HashMap<>();
		this.timeout = Duration.ofSeconds(30);
		this.cookie = this.generateCookie("");
		this.client = this.generateClient(this.uri, this.cookie);
		this.request = this.generateRequest();
		this.response = this.generateResponse();
	}
	
	private HttpCookie generateCookie(String cookie) {
		// Check to see if cookie string is in correct format (cookieName=cookieValue).
		if (Pattern.matches(".*=.*", cookie)) {
			// Split the cookie on the "=" part.
			String cookieName = cookie.split("=")[0];
			String cookieValue = cookie.split("=")[1];
			
			// Create the cookie.
			HttpCookie httpCookie = new HttpCookie(cookieName, cookieValue);
			
			// Set the cookie path to apply to the current domain and all sub-domains.
			httpCookie.setPath("/");
			
			// Sets the normal cookie specification.
			httpCookie.setVersion(0);
			
			// Return the created cookie.
			return httpCookie;
		}
		
		// Return an empty cookie, signifying that a cookie is not present.
		return new HttpCookie("none", "");
	}
	
	private HttpClient generateClient(URI uri, HttpCookie cookie) {
		// Check to see if the initialised cookie actually has any set values present.
		if (!this.cookie.getName().equals("none")) {
			// Create a cookie manager so that the cookie can be passed alongside the client.
			CookieHandler.setDefault(new CookieManager());
			
			// Attempt to resolve the URI.
			((CookieManager) CookieHandler.getDefault()).getCookieStore().add(uri, cookie);

			// Return the client with a cookie, version is set to HTTP/2 to cover all basis.
			return HttpClient.newBuilder()
					.version(Version.HTTP_2)
					.cookieHandler(CookieHandler.getDefault())
					.build();
		}
		
		// Return a client without a cookie.
		return HttpClient.newBuilder()
				.version(Version.HTTP_2)
				.build();
	}
	
	private HttpRequest generateRequest() {
		// Create a HttpRequest instance.
		Builder httpRequest = HttpRequest.newBuilder();
		
		// Set request URI.
		httpRequest.uri(this.uri);
		
		if (this.uri == null) {
			return httpRequest.build();
		}
		
		// Check to see if any headers have been specified.
		if (headers.size() > 0 && headers != null) {
			// Iterate through each header and add it to the request.
			for (int i = 0; i < headers.size(); i++) {
				String key = (String) headers.keySet().toArray()[i];
				String value = headers.get(key);
				
				// Add header to request.
				httpRequest.setHeader(key, value);
			}
		}
		
		// Set as GET request.
		httpRequest.GET();
		
		// Check to see if specified duration is 0, less, or null.
		if (this.timeout.isNegative() || this.timeout.isZero() || this.timeout == null) {
			// Set timeout to default.
			httpRequest.timeout(this.timeout);
		} else {
			// Set timeout to specified amount.
			httpRequest.timeout(this.timeout);
		}
		
		// Build and return the request.
		return httpRequest.build();
	}
	
	private HttpResponse<String> generateResponse() {
		try {
			return this.client.send(this.request, BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			// Will have to return null.
			System.out.println("ERROR:\n" + e.getMessage() + " returned!" + "\nEXCEPTION:\n" + e.toString());
			return null;
		}
	}
	
	public HttpResponse<String> getResponse() throws NullPointerException {
		if (this.response == null) {
			throw new NullPointerException("Due to a malformatity, the reponse could not be obtained");
		}
		return this.response;
	}
}