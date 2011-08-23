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
package ldcreeper.mining;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.net.URLConnection;
import ldcreeper.scheduling.URIServer;
import ldcreeper.storage.GraphStorage;

/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
public class TDBPipeline extends Pipeline {

    public TDBPipeline(URIServer server, GraphStorage storage) {
        super(server, storage);
    }
    
    @Override
    Model fetch() throws Exception {
        URLConnection conn = uri.getURI().toURL().openConnection();
        
        String mime = conn.getContentType();
        String lang = null;
        
        if (mime.equals("application/rdf+xml")) {
            lang = "RDF/XML";
        }
        else if (mime.equals("text/turtle") || mime.equals("application/x-turtle")) {
            lang = "TURTLE";
        }
        else if (mime.equals("text/n3")) {
            lang = "N3";
        }
        else {
            
        }
        
        Model model = ModelFactory.createDefaultModel();
        model.read(conn.getInputStream(), uri.getURI().toString(), lang);
        
        return model;
    }

    @Override
    Model discover(Model model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    Model filter(Model model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    void store(Model model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
