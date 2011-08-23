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

import java.util.logging.Level;
import java.util.logging.Logger;
import ldcreeper.scheduling.URIContext;
import ldcreeper.scheduling.URIServer;

/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
public class MinerPool {
    
    private final URIServer server;
    private final Pipeline pipeline;
    private URIMiner[] miners;
    private int sleeping_miners;
    
    MinerPool(URIServer server, Pipeline pipeline, int miner_count) {
        this.pipeline = pipeline;
        this.server = server;
        miners = new URIMiner[miner_count];
        sleeping_miners = 0;
        
        for (int i = 0; i < miners.length; ++i) {
            miners[i] = new URIMiner(i+1);
        }
    }
    
    public void start() {
        assert(server != null);
            
        for (URIMiner miner : miners) {
            miner.start();
        }
    }
    
    private boolean isLastWorker() {
        return sleeping_miners == miners.length - 1;
    }
    
    private void interruptMiners() {
        for (URIMiner miner : miners) {
            miner.interrupt();
        }
    }
    
    class URIMiner extends Thread {
        
        private String tid;
        
        URIMiner(int tid) {
            this.tid = Integer.toString(tid);
        }
        
        @Override
        public void run() {
            URIContext uri;
            
            while (true) {
                
                synchronized (server) {
                    if ((uri = server.requestURI()) == null) {
                        if (isLastWorker()) {
                            interruptMiners();
                            Logger.getLogger(MinerPool.class.getName()).log(Level.INFO, "Miner {0} exiting", tid);
                            return;
                        }

                        try {
                            sleeping_miners += 1;
                            server.wait();
                            sleeping_miners -= 1;
                            continue;
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MinerPool.class.getName()).log(Level.INFO, "Miner {0} exiting", tid);
                            return;
                        }
                    }
                }
                
                Pipeline pipe_clone = pipeline.clone();
                pipe_clone.setURIContext(uri);
                
                pipe_clone.run();
            }
            
        }
    }
}
