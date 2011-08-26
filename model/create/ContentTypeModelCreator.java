/*
 * Copyright (C) 2011 Ondrej Kupka <ondra dot cap at gmail dot com>
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
package ldcreeper.model.create;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
public class ContentTypeModelCreator implements ModelCreator {
    
    private static final HashMap<String, String> ct_map = new HashMap<String, String>();
    
    static {
        ct_map.put("application/rdf+xml", "RDF/XML");
        ct_map.put("application/x-turtle", "TURTLE");
        ct_map.put("text/turtle", "TURTLE");
        ct_map.put("text/n3", "N3");
    }
    
    @Override
    public Model createFromURI(String uri) {
        try {
            return createFromURI(new URI(uri));
        } catch (URISyntaxException ex) {
            Logger.getLogger(ContentTypeModelCreator.class.getName()).log(Level.INFO, null, ex);
            return null;
        }
    }

    @Override
    public Model createFromURI(URI uri) {
        System.err.println("Creating model from " + uri.toString());
        
        URLConnection conn;
        
        try {
            conn = uri.toURL().openConnection();
        } catch (MalformedURLException ex) {
            Logger.getLogger(ContentTypeModelCreator.class.getName()).log(Level.INFO, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(ContentTypeModelCreator.class.getName()).log(Level.WARNING, null, ex);
            return null;
        }
        
        String mime = conn.getContentType().split(";")[0];
        String lang = ct_map.get(mime);
        
        if (lang == null) {
            System.err.println("Content type '" + mime + "' not supported");
            return null;
        }
        
        Model model = ModelFactory.createDefaultModel();
        
        try {
            model.read(conn.getInputStream(), uri.toString(), lang);
        } catch (IOException ex) {
            Logger.getLogger(ContentTypeModelCreator.class.getName()).log(Level.WARNING, null, ex);
            return null;
        }
        
        return model;
    }
    
}
