package com.gunjun.android.personalproject.Rss;

import com.gunjun.android.personalproject.models.RssFeed;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by gunjunLee on 2017-02-10.
 */

public class RssReader {

    public static RssFeed read(URL url) throws SAXException, IOException {

        return read(url.openStream());

    }

    public static RssFeed read(InputStream stream) throws SAXException, IOException {

        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            RssHandler handler = new RssHandler();
            InputSource input = new InputSource(stream);

            reader.setContentHandler(handler);
            reader.parse(input);

            return handler.getResult();

        } catch (ParserConfigurationException e) {
            throw new SAXException();
        }

    }

    public static RssFeed read(String source) throws SAXException, IOException {
        return read(new ByteArrayInputStream(source.getBytes()));
    }
}
