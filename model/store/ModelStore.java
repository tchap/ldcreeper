/*
 * Copyright (C) 2011 Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ldcreeper.model.store;


import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import java.util.logging.Logger;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public abstract class ModelStore {
    
    public static ModelStore getModelStore(String tdb_path) {
        final Logger log = Logger.getLogger("ldcreeper");
        
        ModelStore store;
        
        if (tdb_path == null) {
            log.warning("No TDB directory " + 
                    "specified for ModelStore, ALL DATA WILL BE DUMPED");
            store = new DevNullModelStore();
        }
        else {
            store = new TDBModelStore(tdb_path);
        }
        
        log.info("ModelStore created");
        
        return store;
    }
    
    public abstract void storeNamedGraph(Graph graph, String name);
    
    public abstract void storeNamedModel(Model model, String name);
    
}
