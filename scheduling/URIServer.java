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
public interface URIServer {
    public void proposeURI(String uri);
    public void proposeURI(URI uri);
    public URI nextURI();
    public void signalURI(String uri, URISignal err);
    public void signalURI(URI uri, URISignal err);
}
