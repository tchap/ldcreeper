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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
/*
 * TODO: Optimize locking
 */
/*
 * TODO: Make statements add/remove a more beautiful code
 */
public class TDBScheduler implements URIServer {

    private static final String prefix_uri = "";
    
    private final Model sched_model;
    private final Object submitted_cond;
    
    private final Object rw_cond;
    private int writers_count;
    private int readers_count;

    public TDBScheduler(String directory) {
        String dir = directory + "scheduling";
        sched_model = TDBFactory.createModel(dir);
        sched_model.setNsPrefix("sched", prefix_uri);
        
        submitted_cond = new Object();
        rw_cond = new Object();
        
        cleanup();
    }
    
    @Override
    public void submitURI(URI uri) {
        acquireWriteLock();
        
        Resource uri_res = sched_model.createResource(uri.toString());
        
        if (sched_model.containsResource(uri_res)) {
            System.err.println("SKIP " + uri.toString());
            return;
        }
        else {
            System.err.println("SUBMIT " + uri.toString());
        }
        
        Property status_prop = sched_model.createProperty(prefix_uri, "status");
        RDFNode submitted_lit = sched_model.createLiteral("submitted");
        
        sched_model.add(uri_res, status_prop, submitted_lit);
        sched_model.commit();
        
        releaseWriteLock();
    }

    @Override
    public URI nextURI() {
        acquireWriteLock();
        
        Property status_prop = sched_model.createProperty(prefix_uri, "status");
        RDFNode submitted_lit = sched_model.createLiteral("submitted");
        ResIterator iter = sched_model.listResourcesWithProperty(status_prop, "submitted");
        
        URI uri = null;
        
        if (iter.hasNext()) {
            try {
                uri = new URI(iter.nextResource().getURI());
            } catch (URISyntaxException ex) {
                System.err.println("Malformed URI");
                releaseWriteLock();
                return null;
            }
        }
        else {
            releaseWriteLock();
            System.err.println("URI pool empty");
            return null;
        }
       
        Resource uri_res = sched_model.createResource(uri.toString());
        RDFNode processing_lit = sched_model.createLiteral("being_processed");
        
        sched_model.add(uri_res, status_prop, processing_lit);
        sched_model.remove(uri_res, status_prop, submitted_lit);
        sched_model.commit();
        
        releaseWriteLock();
        
        return uri;
    }

    @Override
    public void setURIVisited(URI uri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getCond() {
        return submitted_cond;
    }
    
    private void cleanup() {
        acquireWriteLock();
        
        Property status_prop = sched_model.createProperty(prefix_uri, "status");
        RDFNode submitted_lit = sched_model.createLiteral("submitted");
        RDFNode processing_lit = sched_model.createLiteral("being_processed");
        RDFNode visited_lit = sched_model.createLiteral("visited");
        
        ResIterator iter = sched_model.listResourcesWithProperty(status_prop, processing_lit);
        
        while (iter.hasNext()) {
            Resource res = iter.nextResource();
            
            if (sched_model.contains(res, status_prop, visited_lit)) {
                sched_model.remove(res, status_prop, processing_lit);
            }
            else {
                sched_model.add(res, status_prop, submitted_lit);
                sched_model.remove(res, status_prop, processing_lit);
            }
        }
        
        sched_model.commit();
        releaseWriteLock();
    }
    
    private void acquireReadLock() {
        synchronized (rw_cond) {
            while (writers_count > 0) {
                try {
                    rw_cond.wait();
                }
                catch (InterruptedException ex) {}
            }
            
            readers_count += 1;
        }
    }
    
    private void releaseReadLock() {
        synchronized (rw_cond) {
            readers_count -= 1;
            rw_cond.notifyAll();
        }
    }
    
    private void acquireWriteLock() {
        synchronized (rw_cond) {
                while (readers_count > 0 || writers_count > 0) {
                    try {
                        rw_cond.wait();
                    }
                    catch (InterruptedException ex) {}
                }
                
                writers_count += 1;
            }
    }
    
    private void releaseWriteLock() {
        synchronized (rw_cond) {
            writers_count -= 1;
            rw_cond.notifyAll();
        }
    }

}
