/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.mining;

import com.hp.hpl.jena.rdf.model.Model;
import java.util.logging.Level;
import java.util.logging.Logger;
import ldcreeper.scheduling.URIContext;
import ldcreeper.scheduling.URIServer;
import ldcreeper.storage.GraphStorage;

/**
 *
 * @author tchap
 */
abstract public class Pipeline implements Runnable, Cloneable {
    
    private URIContext uri;
    private URIServer server;
    private GraphStorage storage;

    public Pipeline(URIServer server, GraphStorage storage) {
        this.server = server;
        this.storage = storage;
    }
    
    public void setURIContext(URIContext uri) {
        this.uri = uri;
    }
    
    @Override
    public void run() {
        assert uri != null;
        
        store(filter(discover(fetch())));
    }

    @Override
    protected Pipeline clone() {
        try {
            Pipeline pipeline = (Pipeline) super.clone();
            pipeline.setURIContext(null);
            return pipeline;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Pipeline.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    abstract Model fetch();
    abstract Model discover(Model model);
    abstract Model filter(Model model);
    abstract void store(Model model);
}
