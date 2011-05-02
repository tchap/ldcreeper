/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.scheduling;

import java.net.URI;
import java.util.HashSet;

/**
 *
 * @author tchap
 */
public class URIScheduler implements URIServer {
    
    private HashSet<String> visited_uris;
    private URIServer server;

    public URIScheduler(URIServer server) {
        this.server = server;
    }
    
    @Override
    public void proposeURI(String uri) {
        server.proposeURI(uri);
    }

    @Override
    public void proposeURI(URI uri) {
        server.proposeURI(uri);
    }

    @Override
    public URI nextURI() {
        return server.nextURI();
    }

    @Override
    public void signalURI(String uri, URISignal err) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void signalURI(URI uri, URISignal err) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
