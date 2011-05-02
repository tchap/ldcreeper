/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ldcreeper.storage;

import com.hp.hpl.jena.rdf.model.Model;

/**
 *
 * @author tchap
 */
public class FileStore implements ModelStore {

    
    
    @Override
    public void saveModel(Model model, String name) {
        
    }

    @Override
    public Model loadModel(String name) {
        throw new UnsupportedOperationException("Not supported.");
    }
    
}
