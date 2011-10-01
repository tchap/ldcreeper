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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class ResultPageContentHandler extends DefaultHandler {

    private final List<URI> links = new ArrayList<URI>();
    
    private String next_page = null;
    
    private static final Logger log = Logger.getLogger("ldcreeper");
    

    @Override
    public void startDocument() throws SAXException {
        links.clear();
        next_page = null;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (localName.equals("link")) {
            URI link;
            
            try {
                link = new URI(atts.getValue(0));
            } catch (URISyntaxException ex) {
                log.log(Level.INFO, "URI syntax exception", ex);
                return;
            }
            
            links.add(link);
        }
        
        if (localName.equals("next")) {
            next_page = atts.getValue(0);
        }
    }

    public URL getNextPage() {
        try {
            return new URL(next_page);
        } catch (MalformedURLException ex) {
            log.log(Level.WARNING, "Malformed URL exception", ex);
            return null;
        }
    }
    
    public List<URI> getResultLinks() {
        return links;
    }
}
