/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.scheduling;

/**
 *
 * @author tchap
 */
public class DepthFilter implements URIServer {
    
    private URIServer server;
    private int limit;

    public DepthFilter(URIServer server, int limit) {
        this.server = server;
        this.limit = limit;
    }

    @Override
    public void proposeURI(URIContext uri) {
        if (uri.getDepth() <= limit) {
            server.proposeURI(uri);
        }
    }

    @Override
    public URIContext requestURI() {
        return server.requestURI();
    }
    
}
