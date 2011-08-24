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
import ldcreeper.model.create.ModelCreator;
import ldcreeper.model.extract.URIExtractor;
import ldcreeper.model.filter.ModelFilter;
import ldcreeper.scheduling.URIContext;
import ldcreeper.model.store.NamedModelStore;

/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
public class Pipeline implements Runnable, Cloneable {
    
    private ModelCreator creator;
    private URIExtractor extractor;
    private ModelFilter filter;
    private NamedModelStore store;
    
    protected URIContext uric;

    public Pipeline(ModelCreator creator, URIExtractor extractor, ModelFilter filter, NamedModelStore store) {
        this.creator = creator;
        this.extractor = extractor;
        this.filter = filter;
        this.store = store;
    }
    
    public void setURIContext(URIContext uric) {
        this.uric = uric;
    }
    
    @Override
    public void run() {
        if (uric == null) {
            throw new NullPointerException("URI Context not set");
        }
        
        Model model = creator.createFromURI(uric.getURI());
        
        extractor.extractFromModel(model, uric.getDepth());
        
        model = filter.filterModel(model);
        
        store.storeNamedModel(model, uric.getURI().toString());
    }

    @Override
    protected Pipeline clone() throws CloneNotSupportedException {
        Pipeline pipeline = (Pipeline) super.clone();
        pipeline.setURIContext(null);
        return pipeline;
    }
}
