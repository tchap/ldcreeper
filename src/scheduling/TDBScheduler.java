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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class TDBScheduler extends URIServer {

    private static final String prefix_uri = "http://localhost/scheduling/";
 
    private final Model sched_model;
    
    private final Property status;
    private final RDFNode submitted;
    private final RDFNode processing;
    private final RDFNode visited;
    
    private final Object submitted_cond = new Object();
    
    private final Object rw_cond = new Object();
    private int writers_count;
    private int readers_count;
    
    private static final Logger log = Logger.getLogger("ldcreeper");


    public TDBScheduler(String directory) {
        String dir = directory + "scheduling";
        sched_model = TDBFactory.createModel(dir);
        sched_model.setNsPrefix("sched", prefix_uri);
        
        status = sched_model.createProperty(prefix_uri, "status");
        submitted = sched_model.createLiteral("submitted");
        processing = sched_model.createLiteral("being_processed");
        visited = sched_model.createLiteral("visited");

        cleanup();
    }
    
    @Override
    public void submitURI(URI uri) {
        acquireWriteLock();
        
        Resource uri_res = sched_model.createResource(uri.toString());
        
        if (sched_model.containsResource(uri_res)) {
            log.log(Level.INFO, "SKIP %s", uri.toString());
            releaseWriteLock();
            return;
        }
        
        sched_model.add(uri_res, status, submitted);
        sched_model.commit();
        
        releaseWriteLock();
        
        
        log.log(Level.INFO, "SUBMIT %s", uri.toString());
        
        
        synchronized (submitted_cond) {
            submitted_cond.notify();
        }
    }

    @Override
    public synchronized URI nextURI() {
        acquireReadLock();
        
        ResIterator iter = sched_model.listResourcesWithProperty(status, submitted);
        Resource uri_res;
        
        try {
            if (iter.hasNext()) {
                uri_res = iter.nextResource();
            }
            else {
                log.info("NEXT NULL");
                return null;
            }
        }
        finally {
            releaseReadLock();   
        }
        
        
        acquireWriteLock();
        
        sched_model.add(uri_res, status, processing);
        sched_model.remove(uri_res, status, submitted);
        sched_model.commit();
        
        releaseWriteLock();

        
        URI uri;
        
        try {
            uri = new URI(uri_res.getURI());
        } catch (URISyntaxException ex) {
            /*
             * Cannot happen, only possible to insert valid URI
             */
            return nextURI();
        }
        
        
        log.log(Level.INFO, "NEXT %s", uri.toString());
        
        return uri;
    }

    @Override
    public void markURIVisited(URI uri) {
        acquireWriteLock();
        
        Resource uri_res = sched_model.createResource(uri.toString());

        sched_model.add(uri_res, status, visited);
        sched_model.remove(uri_res, status, processing);
        sched_model.commit();
        
        releaseWriteLock();
        
        
        log.log(Level.INFO, "PROCESSED %s", uri.toString());
    }

    @Override
    public Object getCond() {
        return submitted_cond;
    }
    
    private void cleanup() {
        log.info("Starting TDB cleanup");
        
        acquireWriteLock();
        
        ResIterator iter = sched_model.listResourcesWithProperty(status, processing);
        
        while (iter.hasNext()) {
            Resource res = iter.nextResource();
            
            if (sched_model.contains(res, status, visited)) {
                sched_model.remove(res, status, processing);
            }
            else {
                sched_model.add(res, status, submitted);
                sched_model.remove(res, status, processing);
            }
        }
        
        sched_model.commit();
        
        releaseWriteLock();
        
        log.info("TDB cleanup finished");
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
