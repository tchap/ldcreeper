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
package ldcreeper.mining;

import com.hp.hpl.jena.rdf.model.Model;
import java.util.logging.Level;
import java.util.logging.Logger;
import ldcreeper.scheduling.URIContext;
import ldcreeper.scheduling.URIServer;
import ldcreeper.storage.GraphStorage;

/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
abstract public class Pipeline implements Runnable, Cloneable {
    
    protected URIContext uri;
    protected URIServer server;
    protected GraphStorage storage;

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
        
        try {
            store(filter(discover(fetch())));
        } catch (Exception ex) {
            Logger.getLogger(Pipeline.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    
    abstract Model fetch() throws Exception;
    abstract Model discover(Model model) throws Exception;
    abstract Model filter(Model model) throws Exception;
    abstract void store(Model model) throws Exception;
}
