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
package ldcreeper.storage;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
public class TDBStorage implements NamedGraphStorage {
    
    private String directory;
    
    public TDBStorage(String directory) {
        this.directory = directory + "main";
    }
    
    @Override
    public void saveNamedGraph(Graph graph, String name) {
        /*
         * TODO: Implement saveNamedGraph()
         */
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public synchronized void saveNamedModel(Model model, String name) {
        Model tdb_model = TDBFactory.createNamedModel(name, directory);
        tdb_model.add(model).close();
    }

    @Override
    public void saveGraph(Graph graph) {
        /*
         * TODO: Implement saveGraph()
         */
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public synchronized void saveModel(Model model) {
        Model tdb_model = TDBFactory.createModel(directory);
        tdb_model.add(model).close();
    }
    
}
