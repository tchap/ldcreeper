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
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.net.URI;


/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class TDBScheduler extends URIServer {

    private final Model visited_model;
    private final Object cond;
    private int writers_count;
    private int readers_count;
    
    public TDBScheduler(String directory, URIServer server) {
        super(server);
        String dir = directory + "visited";
        visited_model = TDBFactory.createModel(dir);
        cond = new Object();
        writers_count = 0;
        readers_count = 0;
    }

    @Override
    protected boolean propose(URI uri) {
        if (visited(uri)) {
            return false;
        }
        else {
            return true;
        }
    }
    
    private boolean visited(URI uri) {
        Resource uri_res;
        boolean visited;
        
        synchronized (cond) {
            while (writers_count > 0) {
                try {
                    cond.wait();
                }
                catch (InterruptedException ex) {}
            }
            
            readers_count += 1;
        }
        
        uri_res =  visited_model.createResource(uri.toString());
        visited = visited_model.contains(uri_res, RDF.type, RDFS.Resource);
        
        synchronized (cond) {
            readers_count -= 1;
            cond.notifyAll();
        }
        
        if (visited) {
            System.err.println("SKIP " + uri.toString());
            
            return true;
        }
        else {
            System.err.println("PROPOSE " + uri.toString());
            
            synchronized (cond) {
                while (readers_count > 0 || writers_count > 0) {
                    try {
                        cond.wait();
                    }
                    catch (InterruptedException ex) {}
                }
                
                writers_count += 1;
            }
            
            visited_model.add(uri_res, RDF.type, RDFS.Resource); 
            visited_model.commit();
            
            synchronized (cond) {
                writers_count -= 1;
                cond.notifyAll();
            }
            
            return false;
        }  
    }
    
}
