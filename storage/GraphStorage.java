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
public interface GraphStorage {
    public void saveModel(Model model, String name);
    public Model loadModel(String name);
}
