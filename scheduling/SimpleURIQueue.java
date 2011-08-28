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

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class SimpleURIQueue extends URIServer {
    
    private final ConcurrentLinkedQueue<URIContext> queue;
    private final Object queue_lock;

    public SimpleURIQueue(URIServer next_server) {
        super(next_server);
        queue = new ConcurrentLinkedQueue<URIContext>();
        queue_lock = new Object();
    }
    
    @Override
    protected boolean propose(URIContext uri) {
        System.err.println("URI proposed: " + uri.getURI().toString());
        
         if (queue.offer(uri)) {
             synchronized (queue_lock) {
                queue_lock.notify();
             }
             return true;
         }
         else {
             return false;
         }
    }

    @Override
    public URIContext requestURI() {
        return queue.poll();
    }
    
    @Override
    public Object getLock() {
        return queue_lock;
    }
}
