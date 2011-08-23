/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.scheduling;

/**
 *
 * @author tchap
 */
public class CountFilter implements URIServer {
    
    private URIServer server;
    private int max_count;
    private int cur_count;

    public CountFilter(URIServer server, int max_count) {
        this.server = server;
        this.max_count = max_count;
        this.cur_count = 0;
    }
    
    @Override
    public void proposeURI(URIContext uri) {
        if (cur_count <= max_count) {
            server.proposeURI(uri);
            cur_count += 1;
        }
    }

    @Override
    public URIContext requestURI() {
        return server.requestURI();
    }
    
}
