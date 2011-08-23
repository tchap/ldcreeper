/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.scheduling;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author tchap
 */
public class RegexFilter implements URIServer {
    
    private URIServer server;
    private Pattern patt;

    public RegexFilter(URIServer server, String regex) {
        this.server = server;
        patt = Pattern.compile(regex);
    }
    
    @Override
    public void proposeURI(URIContext uri) {
        Matcher m = patt.matcher(uri.getURI().toString());
        
        if (m.matches()) {
            server.proposeURI(uri);
        }
    }

    @Override
    public URIContext requestURI() {
        return server.requestURI();
    }
    
}
