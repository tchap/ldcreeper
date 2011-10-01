/*
 * Copyright (C) 2011 Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ldcreeper.mining.sindice;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class SindiceQueryExecution {
    /*
     * TODO: String <-> StringBuilder everywhere
     */
    private final StringBuilder query_builder;
    private boolean query_added = false;
    
    private static final Logger log = Logger.getLogger("ldcreeper");

    
    public SindiceQueryExecution() {
        query_builder = new StringBuilder("format=rdfxml&");
    }
    
    public void addQuery(SindiceQuery query) {
        query_builder.append(query.getQueryString());
        query_builder.append("&");
        
        query_added = true;
    }
    
    public List<URI> getResultLinks(int page_count) {
        log.info("Preparing to use Sindice search services");
        log.log(Level.INFO, "\tQuery: %s", query_builder.toString());
        
        if (!query_added) {
            log.log(Level.SEVERE, "At least one sindice query " + 
                    "has to be specified");
            return null;
        }
        
        
        URL next_page;
        
        try {
            URI np_uri = new URI("http", "api.sindice.com", "/v3/search", 
                    query_builder.toString(), null);
            next_page = new URL(np_uri.toASCIIString());
        } catch (URISyntaxException ex) {
            log.log(Level.SEVERE, "URI syntax exception", ex);
            return null;
        } catch (MalformedURLException ex) {
            log.log(Level.SEVERE, "Malformed URL exception", ex);
            return null;
        }
        
        XMLReader parser;
        
        try {
            parser = XMLReaderFactory.createXMLReader();
        } catch (SAXException ex) {
            log.log(Level.SEVERE, "SAX exception", ex);
            return null;
        }
        
        ResultPageContentHandler sax_handler = new ResultPageContentHandler();
        parser.setContentHandler(sax_handler);
        
        List<URI> links = new ArrayList<URI>();
        int counter = 1;
        
        log.info("Connecting to Sindice");
        
        while (next_page != null && counter <= page_count) {
            try {
                parser.parse(new InputSource(next_page.openStream()));
            } catch (IOException ex) {
                log.log(Level.SEVERE, "I/O exception", ex);
                return links;
            } catch (SAXException ex) {
                log.log(Level.SEVERE, "SAX exception", ex);
                return links;
            }
            
            links.addAll(sax_handler.getResultLinks());
            
            log.info(String.format("\tSearch results processed [page %d/%d]", 
                    counter, page_count));
            
            next_page = sax_handler.getNextPage();
            counter++;
        }
        
        log.info("Sindice search results processed");
        
        return links;
    }
   
}
