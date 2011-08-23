/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.scheduling;


/**
 *
 * @author tchap
 */
public interface URIServer {
    public void proposeURI(URIContext uri);
    public URIContext requestURI();
}
