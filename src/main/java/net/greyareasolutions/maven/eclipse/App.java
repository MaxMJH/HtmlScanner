package net.greyareasolutions.maven.eclipse;

public class App {
    public static void main(String[] args) {
    	HTMLScanner htmlScanner = new HTMLScanner("http://www.google.com");
    	
    	System.out.println(htmlScanner.getResponse().body());
    }
}
