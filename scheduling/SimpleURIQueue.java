/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.scheduling;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author tchap
 */
public class SimpleURIQueue implements URIServer {
    
    private ConcurrentLinkedQueue<URIContext> queue;

    @Override
    public void proposeURI(URIContext uri) {
        queue.offer(uri);
    }

    @Override
    public URIContext requestURI() {
        return queue.poll();
    }
    
}
