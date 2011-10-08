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
package ldcreeper.mining;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import ldcreeper.scheduling.URIServer;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class MinerPool {
    
    private final URIServer server;
    private final Pipeline pipeline;
    private final Miner[] miners;
    private int sleeping_miners;
    
    private static final Logger log = Logger.getLogger("ldcreeper");
    
    
    public MinerPool(URIServer server, Pipeline pipeline, int miner_count) {  
        this.pipeline = pipeline;
        this.server = server;
        miners = new Miner[miner_count];
        sleeping_miners = 0;
        
        for (int i = 0; i < miners.length; ++i) {
            miners[i] = new Miner(i+1);
        }
    }
    
    public void start() {
        log.info("Starting miner threads");
        
        for (Miner miner : miners) {
            miner.start();
        }
    }
    
    public void join() {
        for (Miner miner : miners) {
            
            try {
                miner.join();
            } catch (InterruptedException ex) {
                log.log(Level.SEVERE, "Interrupted exception", ex);
            }
        }
    }
    
    private boolean isLastWorker() {
        return sleeping_miners == miners.length - 1;
    }
    
    private void interruptMiners() {
        log.info("Interrupting miners");
        
        for (Miner miner : miners) {
            miner.interrupt();
        }
    }
    
    class Miner extends Thread {
        
        private String tid;
        
        Miner(int tid) {
            this.tid = Integer.toString(tid);
        }
        
        @Override
        public void run() {
            URI uri;
            
            while (true) {
                
                synchronized (server.getCond()) {
                    if ((uri = server.nextURI()) == null) {
                        if (isLastWorker()) {
                            interruptMiners();
                            log.log(Level.INFO, "Miner %s exiting", tid);
                            return;
                        }

                        try {
                            sleeping_miners += 1;
                            server.getCond().wait();
                            sleeping_miners -= 1;
                            continue;
                        } catch (InterruptedException ex) {
                            log.log(Level.INFO, "Miner %s exiting", tid);
                            return;
                        }
                    }
                }
                
                Pipeline pipe_clone;
                
                try {
                    pipe_clone = pipeline.clone();
                } catch (CloneNotSupportedException ex) {
                    log.log(Level.SEVERE, "Clone not supported", ex);
                    return;
                }
                
                pipe_clone.setURI(uri);
                pipe_clone.run();
                
                server.markURIVisited(uri);
            }
            
        }
    }
}
