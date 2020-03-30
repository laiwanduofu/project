package crawler;

import java.io.IOException;

public class test {
    public static void main(String[] args) throws IOException {
        Crawler crawler=new Crawler();
        String html= crawler.getPage("https://github.com/doov-io/doov");
        System.out.println(html);
    }
}
