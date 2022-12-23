package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class Crawler {
    private HashSet<String> urlLink;
    private int Max_DEPTH=2;
    public Connection connection;
    public Crawler(){
        //set connection to the mysql
        connection=DatabaseConnection.getConnection();
        urlLink=new HashSet<String>();
    }
    public void getPageTextAndLinks(String url,int depth){
        if(!urlLink.contains(url))
        {
            if (urlLink.add(url))
            {
                System.out.println(url);
                //insert data into page table

            }
            try {
                //parsing HTML object to java Document object
                Document document = Jsoup.connect(url).timeout(5000).get();
                //get text from document object
                String text = document.text().length()<500?document.text():document.text().substring(0,499);
                //print text
                System.out.println(text);
                PreparedStatement preparedStatement=connection.prepareStatement("Insert into pages values(?,?,?)");
                preparedStatement.setString(1,document.title());
                preparedStatement.setString(2,url);
                preparedStatement.setString(3,text);
                preparedStatement.executeUpdate();
                //increase depth
                depth++;
                //if depth is greater than max than return
                if(depth>Max_DEPTH){
                    return;
                }
                //get hypelinks available on the current page
               Elements availableLinkOnPage =document.select("a[href]");
                //run method recursively for every link available on current page
                for(Element currentLink: availableLinkOnPage){
                    getPageTextAndLinks(currentLink.attr("abs:href"),depth);
                }
            }
            catch (IOException ioException){
                ioException.printStackTrace();
            }
            catch(SQLException sqlException){
                sqlException.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
       Crawler crawler=new Crawler();
       crawler.getPageTextAndLinks("http://www.javapoint.com/",0);
    }
}