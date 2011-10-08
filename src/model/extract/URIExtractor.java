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
package ldcreeper.model.extract;

import com.hp.hpl.jena.rdf.model.Model;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ldcreeper.scheduling.URIServer;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public abstract class URIExtractor {
    
    private final URIExtractor next_extractor;

    public URIExtractor(URIExtractor next_extractor) {
        this.next_extractor = next_extractor;
    }
    
    public static URIExtractor getURIExtractor(List<String> paths, 
            URIServer server) {
        
        final Logger log = Logger.getLogger("ldcreeper");
        
        URIExtractor extractor = null;
        
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
                
                extractor = new SPARQLExtractor(server, sparql, extractor);
            } catch (IOException ex) {
                log.warning("I/O exception occures, skipping SPARQL query");
                
                if (!extractor_file.exists()) {
                    log.log(Level.WARNING, "\t(file %s does not exist)", path);
                }
                else if (!extractor_file.canRead()) {
                    log.log(Level.WARNING, "\t(file %s not readable)", path);
                }
                else {
                    log.log(Level.WARNING, "\t(%s)", ex.getMessage());
                }
            }            
        }
        
        if (extractor == null) {
            log.warning("No SPARQL URI Extractor created, " + 
                    "using default (extract all URIs)");
            extractor = new EveryURIExtractor(server, null);
        }
        
        log.info("URIExtractor created");
        
        return extractor;
    }
    
    public void extractFromModel(Model model) {
        extractFrom(model);
        
        if (next_extractor != null) {
            next_extractor.extractFromModel(model);
        }
    }
    
    abstract protected void extractFrom(Model model);
      
}
