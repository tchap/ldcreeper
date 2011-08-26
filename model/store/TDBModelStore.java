/*
 * Copyright (C) 2011 Ondrej Kupka <ondra dot cap at gmail dot com>
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
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;


/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
public class TDBModelStore implements NamedModelStore {
    
    private final Dataset dataset;
    
    public TDBModelStore(String directory) {
        String dir = directory + "main";
        dataset = TDBFactory.createDataset(dir);
    }
    
    @Override
    public void storeNamedGraph(Graph graph, String name) {
        dataset.asDatasetGraph().addGraph(Node.createURI(name), graph);
    }

    /*
     * TODO: Find out how the hell does TDB API work and rewrite this
     */
    @Override
    public synchronized void storeNamedModel(Model model, String uri_name) {
        Model tdb_model = dataset.getNamedModel(uri_name);
        tdb_model.add(model);
        tdb_model.close();
    }
    
}
