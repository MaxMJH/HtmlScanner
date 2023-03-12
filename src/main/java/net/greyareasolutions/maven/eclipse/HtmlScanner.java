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

public class HtmlScanner {
	/*---- Fields ----*/
	private URI uri;
	private HashMap<String, String> headers;
	private Duration timeout;
	private HttpCookie cookie;
	private HttpClient client;
	private HttpRequest request;
	private HttpResponse<String> response;
	
	/*---- Constructors ----*/
	public HtmlScanner(String uri, String cookie, HashMap<String, String> headers, Duration timeout) {
		try {
			this.uri = new URI(uri);
		} catch (URISyntaxException e) {
			System.out.println("AN ERROR HAS OCCURED!\n" + e.getMessage() + "\nCAUSED BY: " + e.getReason());
			
			// Set to null otherwise.
			this.uri = null;
		}
		this.headers = headers;
		this.timeout = timeout;
		this.constructHtml(cookie);
	}
	
	public HtmlScanner(String uri, String cookie, HashMap<String, String> headers) {
		this(uri, cookie, headers, Duration.ofSeconds(30));
	}
	
	public HtmlScanner(String uri, String cookie) {
		this(uri, cookie, new HashMap<>(), Duration.ofSeconds(30));
	}
	
	public HtmlScanner(String uri) {
		this(uri, "", new HashMap<>(), Duration.ofSeconds(30));
	}
	
	/*---- Methods ----*/
	public void constructHtml(String cookie) {
		this.cookie = this.generateCookie(cookie);
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
	
	/*---- Getters and Setters ----*/
	public URI getURI() {
		return this.uri;
	}
	
	public void setURI(URI uri) {
		this.uri = uri;
	}
	
	public HashMap<String, String> getHeaders() {
		return this.headers;
	}
	
	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}
	
	public Duration getTimeout() {
		return this.timeout;
	}
	
	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}
	
	public HttpCookie getCookie() {
		return this.cookie;
	}
	
	public void setCookie(HttpCookie cookie) {
		this.cookie = cookie;
	}
	
	public HttpClient getClient() {
		return this.client;
	}
	
	public void setClient(HttpClient client) {
		this.client = client;
	}
	
	public HttpRequest getRequest() {
		return this.request;
	}
	
	public void setRequest(HttpRequest request) {
		this.request = request;
	}
	
	public HttpResponse<String> getResponse() throws NullPointerException {
		if (this.response == null) {
			throw new NullPointerException("Due to a malformatity, the reponse could not be obtained");
		}
		return this.response;
	}
}
