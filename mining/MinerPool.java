/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.mining;

import java.util.logging.Level;
import java.util.logging.Logger;
import ldcreeper.scheduling.URIContext;
import ldcreeper.scheduling.URIServer;

/**
 *
 * @author tchap
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
