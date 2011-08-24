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
package ldcreeper.scheduling;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import java.net.URI;


/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
public class TDBScheduler implements URIServer {
    
    private URIServer server;
    private String directory;
    
    private static final String property_name = "scheduling_state";
    private static final String property_value = "visited";

    public TDBScheduler(URIServer server, String directory) {
        this.server = server;
        this.directory = directory + "scheduling";
    }

    @Override
    public void proposeURI(URIContext uri) {
        if (!visited(uri.getURI())) {
            server.proposeURI(uri);
        }
    }

    @Override
    public URIContext requestURI() {
        return server.requestURI();
    }
    
    private boolean visited(URI u) {
        /*
         * TODO: Don't create separate model for every query
         */
        /*
         * TODO: Rewrite visited() to use standard predicate
         */
        Model model = TDBFactory.createModel(directory);
        
        Resource uri = model.createResource(u.toString());
        Property state = model.createProperty(property_name);
        
        if (model.contains(uri, state, property_value)) {
            model.close();
            return true;
        }
        else {
            registerVisitedURI(model, uri, state);
            model.close();
            return false;
        }
    }
    
    private synchronized void registerVisitedURI(Model model, Resource uri, Property property) { 
        uri.addProperty(property, property_value);
        model.commit();
    }

}
