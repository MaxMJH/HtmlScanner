package net.greyareasolutions.maven.eclipse;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;
import java.net.http.HttpClient.Version;

/**
 * The class HtmlScanner is used to create a connection to a specified URI.
 * Headers, Timeout Duration, and Cookies can be supplied in order to create
 * a specific connection to obtain the HTML page's content. 
 * 
 * An example of the class's usage can be seen below:
 * <pre>
 * HttpOptions options = new HttpOptions("https://www.google.com");
 * HtmlScanner htmlScanner = new HtmlScanner(options);
 * String html = htmlScanner.getResponse().body();
 * </pre>
 * If it is required for a URI to be changed, or an extra header needs to be added, etc...
 * it is crucial that the HTML is re-constructed, an example of this can be seen below:
 * <pre>
 * HashMap&lt;String, String&gt; headers = new HashMap&lt;&gt;();
 * headers.put("Connection", "keep-alive");
 * ...
 * options.setHeaders(headers);
 * <b>htmlScanner.constructHtml();</b>
 * ...
 * </pre>
 * @author Max Harris - mjh@greyareasolutions.net
 * @version v0.0.1
 * @since 12-03-2023
 */
public class HtmlScanner {
	/*---- Fields ----*/
	/**
	 * Fields which stores the options that need to be sent with the request.
	 */
	private HttpOptions options;
	
	/**
	 * Field which stores the target cookie.
	 */
	private HttpCookie cookie;
	
	/**
	 * Field which stores the client used to send the request.
	 */
	private HttpClient client;
	
	/**
	 * Field which stores the request that will be sent to target URI.
	 */
	private HttpRequest request;
	
	/**
	 * Field which stores the response of the request.
	 */
	private HttpResponse<String> response;
	
	/*---- Constructors ----*/
	/**
	 * Core constructor that aims to initialise all declared fields of the class. 
	 * 
	 * Constructor also calls the {@link #constructHtml()} method to initialise
	 * <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/HttpCookie.html" title="class or interface in java.net" class="external-link">HttpCookie</a>, <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/HttpClient.html" title="class or interface in java.net.http" class="external-link">HttpClient</a>, <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/HttpRequest.html" title="class or interface in java.net.http" class="external-link">HttpRequest</a>, and <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/HttpResponse.html" title="class or interface in java.net.http" class="external-link">HttpResponse</a>.
	 * 
	 * @param options reference of {@link HttpOptions}.
	 */
	public HtmlScanner(HttpOptions options) {
		// Initialise the options field.
		this.options = options;
		
		// As a user can re-construct the HTTP objects, this is decoupled into a separate function.
		this.constructHtml();
	}
	
	/*---- Methods ----*/
	/**
	 * This method aims to initialise the relevant HTTP objects so that a HTTP request can be sent to the target.
	 */
	public void constructHtml() {
		this.cookie = this.generateCookie(this.options.getCookie());
		this.client = this.generateClient(this.options.getURI(), this.cookie);
		this.request = this.generateRequest();
		this.response = this.generateResponse();
	}
	
	/**
	 * Initialises a cookie to be sent in a HTTP request, the path set will allow for the target's main directory as well as sub-directories.
	 * In order for the cookie to be correctly set it must follow a strict format.
	 * The correct form is that of <b>cookieName=cookieValue(;...)</b>.
	 * 
	 * @param cookie sets the relevant cookie needed if authentication is present.
	 * @return an initialised <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/HttpCookie.html" title="class or interface in java.net" class="external-link">HttpCookie</a> that follows allows a cookie of version 0.
	 */
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
	
	/**
	 * Initialises and sets up a client to be used when sending a HTTP request to the target URI.
	 * This method is also responsible for adding a cookie to the client if the cookie's name is not equal to <b>"none"</b>.
	 * 
	 * @param uri URI of the target.
	 * @param cookie sets the relevant cookie needed if authentication is present.
	 * @return an initialised and built <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/HttpClient.html" title="class or interface in java.net.http" class="external-link">HttpClient</a> with a default HTTP version of 2.
	 */
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
	
	/**
	 * Initialises and sets up a request that can be sent to the target URI.
	 * This method also checks if there are any headers to be sent, if so, they will be added iteratively - a timeout is also added.
	 * 
	 * @return an initialised and built <a href="https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/HttpRequest.html" title="class or interface in java.net.http" class="external-link">HttpRequest</a> specified to send a GET request.
	 */
	private HttpRequest generateRequest() {
		// Create a HttpRequest instance.
		Builder httpRequest = HttpRequest.newBuilder();
		
		// Set request URI.
		httpRequest.uri(this.options.getURI());
		
		if (this.options.getURI() == null) {
			return httpRequest.build();
		}
		
		// Check to see if any headers have been specified.
		if (this.options.getHeaders().size() > 0 && this.options.getHeaders() != null) {
			// Iterate through each header and add it to the request.
			for (int i = 0; i < this.options.getHeaders().size(); i++) {
				String key = (String) this.options.getHeaders().keySet().toArray()[i];
				String value = this.options.getHeaders().get(key);
				
				// Add header to request.
				httpRequest.setHeader(key, value);
			}
		}
		
		// Set as GET request.
		httpRequest.GET();
		
		// Check to see if specified duration is 0, less, or null.
		if (this.options.getTimeout().isNegative() || this.options.getTimeout().isZero() || this.options.getTimeout() == null) {
			// Set timeout to default.
			httpRequest.timeout(this.options.getTimeout());
		} else {
			// Set timeout to specified amount.
			httpRequest.timeout(this.options.getTimeout());
		}
		
		// Build and return the request.
		return httpRequest.build();
	}
	
	/**
	 * Initialises and stores the response obtained via the HTTP request.
	 * 
	 * @return the stored response from the request.
	 */
	private HttpResponse<String> generateResponse() {
		try {
			// Return the response of the HTTP request.
			return this.client.send(this.request, BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			// Will have to return null.
			System.out.println("ERROR:\n" + e.getMessage() + " returned!" + "\nEXCEPTION:\n" + e.toString());
			return null;
		}
	}
	
	/*---- Getters and Setters ----*/
	/**
	 * Returns the class's initialised options field.
	 * 
	 * @return the class's options field.
	 */
	public HttpOptions getOptions() {
		return this.options;
	}
	
	/**
	 * Sets the class's options field.
	 * 
	 * @param options options that will be attached when sending the request to the target URI.
	 */
	public void setOptions(HttpOptions options) {
		this.options = options;
	}
	
	/**
	 * Returns the class's initialised cookie field.
	 * 
	 * @return the class's cookie field.
	 */
	public HttpCookie getCookie() {
		return this.cookie;
	}
	
	/**
	 * Sets the class's cookie field.
	 * 
	 * @param cookie cookie which will be attached to the client.
	 */
	public void setCookie(HttpCookie cookie) {
		this.cookie = cookie;
	}
	
	/**
	 * Returns the class's initialised client field.
	 * 
	 * @return the class's client field.
	 */
	public HttpClient getClient() {
		return this.client;
	}
	
	/**
	 * Sets the class's client field.
	 * 
	 * @param client client which will be used to send the request.
	 */
	public void setClient(HttpClient client) {
		this.client = client;
	}
	
	/**
	 * Returns the class's initialised request field.
	 * 
	 * @return the class's request field.
	 */
	public HttpRequest getRequest() {
		return this.request;
	}
	
	/**
	 * Sets the class's request field.
	 * 
	 * @param request request which will be sent to the target URI.
	 */
	public void setRequest(HttpRequest request) {
		this.request = request;
	}
	
	/**
	 * Returns the class's initialised response field, if the response was unable to be obtained, an exception is thrown.
	 * 
	 * @return the class's response field.
	 * @throws NullPointerException indicates that the response failed to be obtained when the request was sent.
	 */
	public HttpResponse<String> getResponse() throws NullPointerException {
		// If no response is returned (indicating incorrect URI) throw a null pointer exception.
		if (this.response == null) {
			throw new NullPointerException("Due to a malformatity, the reponse could not be obtained");
		}
		return this.response;
	}
}
