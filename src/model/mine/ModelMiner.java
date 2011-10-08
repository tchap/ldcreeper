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
package ldcreeper.model.mine;

import com.hp.hpl.jena.rdf.model.Model;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public abstract class ModelMiner {
    
    private final ModelMiner next_miner;

    public ModelMiner(ModelMiner next_miner) {
        this.next_miner = next_miner;
    }
    
    public static ModelMiner getModelMiner(List<String> paths) {
        final Logger log = Logger.getLogger("ldcreeper");
        
        ModelMiner miner = null;
        
        for (String path : paths) {
            File extractor_file = new File(path);

            String sparql = "";

            BufferedReader reader = null;
            String line;

            try {
                reader = new BufferedReader(new FileReader(extractor_file));
                
                while ((line = reader.readLine()) != null) {
                    sparql += line;
                }
                
                miner = new SPARQLMiner(sparql, miner);
            } catch (IOException ex) {
                log.warning("Skipping SPARQL query ");
                
                if (!extractor_file.exists()) {
                    log.log(Level.WARNING, "(file %s does not exist)", path);
                }
                else if (!extractor_file.canRead()) {
                    log.log(Level.WARNING, "(file %s not readable)", path);
                }
                else {
                    log.log(Level.WARNING, "(%s)", ex.getMessage());
                }
            }            
        }
        
        if (miner == null) {
            log.warning("No ModelMiner found, " + 
                    "using default (mine everything)");
            miner =  new SimpleMiner(null);
        }
        
        log.info("ModelMiner created");
        
        return miner;
    }
    
    public Model mineModel(Model model) {
        Model mined = mine(model);
        
        if (next_miner != null) {
            return mined.add(next_miner.mineModel(model));
        }
        else {
            return mined;
        }
    }
    
    abstract protected Model mine(Model model);
    
}
