/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.scheduling;

import java.util.HashSet;

/**
 *
 * @author tchap
 */
public class SimpleURIScheduler implements URIServer {
    
    private URIServer server;
    private HashSet<String> visited;

    public SimpleURIScheduler(URIServer server) {
        this.server = server;
    }

    @Override
    public void proposeURI(URIContext uri) {
        if (!visited.add(uri.toString())) {
            server.proposeURI(uri);
        }
    }

    @Override
    public URIContext requestURI() {
        return server.requestURI();
    }
    
}
