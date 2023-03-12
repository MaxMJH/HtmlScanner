package net.greyareasolutions.maven.eclipse;

public class App {
    public static void main(String[] args) {
    	HtmlScanner htmlScanner = new HtmlScanner("http://www.google.com");
    	
    	System.out.println(htmlScanner.getResponse().body());
    }
}
