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
public class URIFilter implements URIServer {

    private URIServer wrapped;

    public URIFilter(URIServer wrapped) {
        this.wrapped = wrapped;
    }
    
    @Override
    public void proposeURI(String uri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void proposeURI(URI uri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URI nextURI() {
        throw new UnsupportedOperationException("Not supported yet.");
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
