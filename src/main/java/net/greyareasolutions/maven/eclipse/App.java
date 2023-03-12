package net.greyareasolutions.maven.eclipse;

import java.net.URI;
import java.net.URISyntaxException;

public class App {
    public static void main(String[] args) throws URISyntaxException {
    	HtmlScanner htmlScanner = new HtmlScanner("http://www.greyareasolutions.net");
    	
    	System.out.println(htmlScanner.getResponse().uri());
    	System.out.println(htmlScanner.getResponse().body());
    	
    	htmlScanner.setURI(new URI("http://www.google.com"));
    	htmlScanner.constructHtml();
    	
    	System.out.println();
    	System.out.println(htmlScanner.getResponse().uri());
    	System.out.println(htmlScanner.getResponse().body());
    }
}
