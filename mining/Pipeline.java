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
package ldcreeper.mining;

import com.hp.hpl.jena.rdf.model.Model;
import java.net.URI;
import ldcreeper.model.build.ModelBuilder;
import ldcreeper.model.extract.URIExtractor;
import ldcreeper.model.mine.ModelMiner;
import ldcreeper.model.store.ModelStore;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class Pipeline implements Runnable, Cloneable {
    
    private final ModelBuilder builder;
    private final URIExtractor extractor;
    private final ModelMiner miner;
    private final ModelStore store;
    
    protected URI uri;

    public Pipeline(ModelBuilder builder, URIExtractor extractor, ModelMiner miner, ModelStore store) {
        this.builder = builder;
        this.extractor = extractor;
        this.miner = miner;
        this.store = store;
    }
    
    public void setURI(URI uri) {
        this.uri = uri;
    }
    
    @Override
    public void run() {
        if (uri == null) {
            throw new NullPointerException("URI not set");
        }
        
        Model model = builder.buildFromURI(uri);
        
        if (model == null) {
            return;
        }
        
        extractor.extractFromModel(model);
        
        model = miner.mineModel(model);
        
        if (model == null) {
            return;
        }
        
        store.storeNamedModel(model, uri.toString());
    }

    @Override
    protected Pipeline clone() throws CloneNotSupportedException {
        Pipeline pipeline = (Pipeline) super.clone();
        pipeline.setURI(null);
        return pipeline;
    }
}
