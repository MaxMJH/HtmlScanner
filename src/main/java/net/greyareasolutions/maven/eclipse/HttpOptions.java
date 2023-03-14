package net.greyareasolutions.maven.eclipse;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * The class HttpOptions is used to store general information used when sending a request.
 * The three main variables which are stored are the target URI, optional headers, and a 
 * request timeout.
 * 
 * This class is typically used in conjunction with {@link HtmlScanner}.
 * 
 * @author Max Harris - mjh@greyareasolutions.net
 * @version v0.0.1
 * @since 13-03-2023
 */
public class HttpOptions {
	/*---- Fields ----*/
	/**
	 * Field which stores the target URI.
	 */
	private URI uri;
	
	/**
	 * Field which stores the string representation of a cookie.
	 */
	private String cookie;
	
	/**
	 * Field which stores the target headers.
	 */
	private Map<String, String> headers;
	
	/**
	 * Field which stores the target request timeout.
	 */
	private Duration timeout;
	
	/*---- Constructors ----*/
	/**
	 * Core constructor that aims to initialises the class's fields which will be used
	 * when sending a HTTP GET request to a target URI. This class is mainly used in the
	 * constructor of {@link HtmlScanner}.
	 * 
	 * @param uri URI of the target.
	 * @param cookie sets the relevant cookie needed if authentication is present.
	 * @param headers sets the relevant headers required by the target (if any).
	 * @param timeout sets the lifetime of the request, best to leave it null unless required. 
	 */
	public HttpOptions(URI uri, String cookie, Map<String, String> headers, Duration timeout) {
		// Initialise the class's fields.
		this.uri = uri;
		this.cookie = cookie;
		this.headers = headers;
		this.timeout = timeout;
	}
	
	/**
	 * Constructor which sets a default duration of 30 seconds. This constructor passes the 
	 * remaining parameters to the top-level constructor.
	 * 
	 * @param uri URI of the target.
	 * @param cookie sets the relevant cookie needed if authentication is present.
	 * @param headers sets the relevant headers required by the target (if any).
	 */
	public HttpOptions(URI uri, String cookie, HashMap<String, String> headers) {
		// Call the top-level constructor, setting a default timeout.
		this(uri, cookie, headers, Duration.ofSeconds(30));
	}
	
	/**
	 * Constructor which sets a default duration of 30 seconds, as well as an empty HashMap
	 * for headers. This constructor passes the remaining parameters to the top-level
	 * constructor.
	 * 
	 * @param uri URI of the target.
	 * @param cookie sets the relevant cookie needed if authentication is present.
	 */
	public HttpOptions(URI uri, String cookie) {
		// Call the top-level constructor, setting a default timeout and an empty HashMap for headers.
		this(uri, cookie, new HashMap<>(), Duration.ofSeconds(30));
	}
	
	/**
	 * Constructor which sets a default duration of 30 seconds, an empty HashMap for headers,
	 * as well as an empty cookie. This constructor passes the remaining parameters to the
	 * top-level constructor.
	 * 
	 * @param uri URI of the target.
	 */
	public HttpOptions(URI uri) {
		// Call the top-level constructor, setting a default timeout, an empty HashMap for headers, and an empty cookie.
		this(uri, "", new HashMap<>(), Duration.ofSeconds(30));
	}
	
	/*---- Getters and Setters ----*/
	/**
	 * Returns the class's initialised uri field.
	 * 
	 * @return the class's uri field.
	 */
	public URI getURI() {
		return this.uri;
	}
	
	/**
	 * Sets the class's uri field.
	 * 
	 * @param uri uri of the target.
	 */
	public void setURI(URI uri) {
		this.uri = uri;
	}
	
	/**
	 * Returns the class's initialised headers field.
	 * 
	 * @return the class's headers field.
	 */
	public Map<String, String> getHeaders() {
		return this.headers;
	}
	
	/**
	 * Sets the class's headers field.
	 * 
	 * @param headers headers to be sent with the request.
	 */
	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}
	
	/**
	 * Returns the class's initialised timeout field.
	 * 
	 * @return the class's timeout field.
	 */
	public Duration getTimeout() {
		return this.timeout;
	}
	
	/**
	 * Sets the class's timeout field.
	 * 
	 * @param timeout timeout which will be attached to the request.
	 */
	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * Returns the class's string representation of the cookie field.
	 * 
	 * @return the class's cookie field.
	 */
	public String getCookie() {
		return this.cookie;
	}
	
	/**
	 * Sets the class's cookie field.
	 * 
	 * @param cookie string representation of cookie which will be attached to the client.
	 */
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
}
