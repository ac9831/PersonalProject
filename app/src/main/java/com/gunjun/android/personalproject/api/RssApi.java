package com.gunjun.android.personalproject.api;

import com.gunjun.android.personalproject.Rss.RssReader;
import com.gunjun.android.personalproject.interfaces.RssReceiver;
import com.gunjun.android.personalproject.models.RssFeed;
import com.gunjun.android.personalproject.models.RssItem;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by gunjunLee on 2017-02-24.
 */

public class RssApi {


    private RssReceiver rssReceiver;

    public void setRssReceiver(RssReceiver rssReceiver) {
        this.rssReceiver = rssReceiver;
    }

    public void rssCall() {

        new Thread() {
            public void run() {
                try {
                    ArrayList<RssItem> rssItems;

                    URL url = null;
                    try {
                        url = new URL("http://www.chosun.com/site/data/rss/rss.xml");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    RssFeed feed = null;

                    feed = RssReader.read(url);
                    rssItems = feed.getRssItems();


                    for (int i = 0; i < rssItems.size(); i++) {
                        Document doc = Jsoup.connect(rssItems.get(i).getLink()).get();
                        Elements items = doc.select("meta");
                        for (int j = 0; j < items.size(); j++) {
                            if (items.get(j).attr("property").equals("og:image")) {
                                rssItems.get(i).setImageUrl(items.get(j).attr("content"));
                                break;
                            }
                        }
                    }

                    rssReceiver.rssDataReceiver(rssItems);

                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }.start();
    };
}
