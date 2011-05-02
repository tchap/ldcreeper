/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.scheduling;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tchap
 */
public class URIQueue implements URIServer {
    
    private LinkedBlockingQueue<URI> uri_queue;

    public URIQueue() {
        uri_queue = new LinkedBlockingQueue();
    }

    public URIQueue(int capacity) {
        uri_queue = new LinkedBlockingQueue<URI>(capacity);
    }
    
    @Override
    public void proposeURI(String uri) {
        try {
            proposeURI(new URI(uri));
        } catch (URISyntaxException ex) {
            Logger.getLogger(URIQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void proposeURI(URI uri) {
        try {
            uri_queue.put(uri);
        } catch (InterruptedException ex) {
            Logger.getLogger(URIQueue.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    @Override
    public URI nextURI() {
        try {
            return uri_queue.take();
        } catch (InterruptedException ex) {
            Logger.getLogger(URIQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    
    public Model nextModel() {
        URI uri = nextURI();
        
        if (uri == null) {
            return null;
        }
        
        URLConnection connection = null; 
        
        try {
            connection = uri.toURL().openConnection();
        } catch (MalformedURLException ex) {
            Logger.getLogger(URIQueue.class.getName()).log(Level.WARNING, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(URIQueue.class.getName()).log(Level.WARNING, null, ex);
            return null;
        }
        
        String content_type = connection.getContentType();
        String lang = null;
        
        if (content_type.equals("application/rdf+xml")) {
            lang = "RDF/XML";
        } 
        else if (content_type.equals("text/turtle") ||
                 content_type.equals("application/x-turtle")) {
            lang = "TURTLE";
        } 
        else if (content_type.equals("text/n3")) {
            lang = "N3";
        } 
        else {
            Logger.getLogger(URIQueue.class.getName()).log(Level.INFO, null, "MIME type not supported");
            return null;
        }
        
        /* 
         * TODO: Create ontology model instead and do some inference.
         */
        Model model = ModelFactory.createDefaultModel();
        
        try {
            model.read(connection.getInputStream(), connection.getURL().toString(), lang);
        } catch (IOException ex) {
            Logger.getLogger(URIQueue.class.getName()).log(Level.WARNING, null, ex);
            return null;
        }
        
        return model;
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
