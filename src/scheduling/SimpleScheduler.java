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

import java.net.URI;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class SimpleScheduler extends URIServer {

    private final Set<URI> processing_uris = new TreeSet<URI>();
    private final Set<URI> visited_uris = new HashSet<URI>();
    private final Queue<URI> queue = new ConcurrentLinkedQueue<URI>();
    private final Object cond = new Object();
    private final Object lock = new Object();
    
    private static final Logger log = Logger.getLogger("ldcreeper");
    
    
    @Override
    public void submitURI(URI uri) {
        synchronized (lock) {
            if (visited_uris.contains(uri) || processing_uris.contains(uri)) {
                log.log(Level.INFO, "SKIP %s", uri.toString());
                return;
            }
        }
        
       log.log(Level.INFO, "SUBMIT %s", uri.toString());
        
        queue.offer(uri);
        
        synchronized (cond) {    
            cond.notify();
        }
    }

    @Override
    public URI nextURI() {
        URI uri = queue.poll();
        
        if (uri != null) {
            synchronized (lock) {
                processing_uris.add(uri);
            }
            
            log.log(Level.INFO, "NEXT %s", uri.toString());
        }
        else {
            log.info("NEXT NULL");
        }
            
        return uri;
    }

    @Override
    public void markURIVisited(URI uri) {
        synchronized (lock) {
            processing_uris.remove(uri);
            visited_uris.add(uri);
        }
        
        log.log(Level.INFO, "PROCESSED %s", uri.toString());
    }

    @Override
    public Object getCond() {
        return cond;
    }
    
}
