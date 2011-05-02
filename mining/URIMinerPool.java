/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.mining;

import ldcreeper.scheduling.URIServer;

/**
 *
 * @author tchap
 */
public class URIMinerPool {
    
    private URIServer server = null;
    private URIMiner[] miners;
    
    URIMinerPool(int miner_count) {
        miners = new URIMiner[miner_count];
        
        for (int i = 0; i < miners.length; ++i) {
            miners[i] = new URIMiner(i);
        }
    }
    
    public void start() {
        assert(server != null);
            
        for (URIMiner miner : miners) {
            miner.start();
        }
    }
    
    public void setServer(URIServer server) {
        this.server = server;
    }
    
    public int getSleepingMiners() {
        /*
         * TODO: Implement
         */
        return 0;
    }
    
    class URIMiner extends Thread {
        
        URIMiner(int id) {
        
        }
        
        @Override
        public void run() {
        
        }
    }
}
