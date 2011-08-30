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
package ldcreeper.scheduling;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.net.URI;
import java.net.URISyntaxException;


/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class TDBURIPool extends URIServer {

    private final Object queue_lock;
    private Model tdb_model;

    public TDBURIPool(String directory, URIServer next_server) {
        super(next_server);
        queue_lock = new Object();
        
        String dir = directory + "pool";
        tdb_model = TDBFactory.createModel(dir);
    }
    
    @Override
    protected synchronized boolean propose(URI uri) {
        Resource uri_res = tdb_model.createResource(uri.toString());
        
        tdb_model.add(uri_res, RDF.type, RDFS.Resource);
        tdb_model.commit();
        
        return true;
    }
    
    @Override
    public synchronized URI requestURI() {
        StmtIterator iter = tdb_model.listStatements();
        
        if (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            URI uri;
            
            try {
                uri = new URI(stmt.getSubject().getURI());
            } catch (URISyntaxException ex) {
                return null;
            }
            
            tdb_model.remove(stmt);
            tdb_model.commit();
            
            return uri;
        }
        else {
            return null;
        }
    }
    
    @Override
    public Object getLock() {
        return queue_lock;
    }
}
