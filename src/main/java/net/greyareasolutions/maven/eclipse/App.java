package net.greyareasolutions.maven.eclipse;

import java.net.URI;
import java.net.URISyntaxException;

public class App {
    public static void main(String[] args) throws URISyntaxException {
    	HttpOptions options = new HttpOptions(new URI("https://www.google.com"));
    	
    	HtmlScanner htmlScanner = new HtmlScanner(options);
    	
    	System.out.println(htmlScanner.getResponse().uri());
    	System.out.println(htmlScanner.getResponse().body());
    }
}
