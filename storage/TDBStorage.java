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
public class TDBStorage implements GraphStorage {

    @Override
    public void saveModel(Model model, String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Model loadModel(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}