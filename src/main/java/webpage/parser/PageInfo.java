package webpage.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PageInfo {
    Document document;

    public PageInfo(String address) {
        try {
            document = Jsoup.connect(address).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PageInfo(File htmlFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(htmlFile));
        String buff;
        StringBuilder sb = new StringBuilder();
        while ((buff = br.readLine()) != null) {
            sb.append(buff);
        }
        document = Jsoup.parse(sb.toString());
    }

    public String getTitle() {
        return document.title();
    }

    public String getKeywords() {
        Elements elements = document.select("meta[name=keywords]");
        if (elements.size() == 0) {
            return "";
        }
        return elements.first().attr("content");
    }

    public String getDescription() {
        if (null != document.select("meta[name=description]")) {
            if (null != document.select("meta[name=description]").first()) {
                return document.select("meta[name=description]").first().attr("content");
            }
        }
        return "No description found";
    }

    public String getRobots() {
        Elements elements = document.select("meta[name=robots]");
        if (elements.size() == 0) {
            return "";
        }
        return elements.first().attr("content");
    }

    public List<String> getLinks() {
        List<String> list = new LinkedList<String>();
        Elements links = document.select("a");
        for (Element e : links) {
            String link = e.attr("abs:href");
            if (link.contains("#")) {
                int i = link.indexOf("#");
                list.add(link.substring(0, i));
            } else {
                list.add(link);
            }
        }
        return list;
    }

    public String getText() {
        return document.body().text();
    }
}
