/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.scheduling;

import java.net.URI;

/**
 *
 * @author tchap
 */
public class URIContext {
    private URI uri;
    private int depth;

    public URIContext(URI uri, int depth) {
        this.uri = uri;
        this.depth = depth;
    }
    
    public int getDepth() {
        return depth;
    }

    public URI getURI() {
        return uri;
    }
}
