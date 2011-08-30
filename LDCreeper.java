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
package ldcreeper;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ldcreeper.mining.MinerPool;
import ldcreeper.mining.Pipeline;
import ldcreeper.model.create.ContentTypeModelCreator;
import ldcreeper.model.create.ModelCreator;
import ldcreeper.model.extract.SPARQLExtractor;
import ldcreeper.model.extract.URIExtractor;
import ldcreeper.model.filter.ModelFilter;
import ldcreeper.model.filter.SPARQLFilter;
import ldcreeper.model.store.NamedModelStore;
import ldcreeper.model.store.TDBModelStore;
import ldcreeper.scheduling.TDBScheduler;
import ldcreeper.scheduling.TDBURIPool;
import ldcreeper.scheduling.URIServer;


/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class LDCreeper {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String starting_point = "http://dig.csail.mit.edu/2008/webdav/timbl/foaf.rdf";
        String tdb_path = "/home/tchap/Matfyz/Rocnikovy_projekt/ldcreeper_runtime/TDB/";
        

        /*
         * TODO: Read queries from a file
         */
        /*
         * TODO: Rewrite CONSTRUCT query to follow anon nodes
         */
        String extractor_select = 
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "SELECT DISTINCT ?person ?sameas ?seealso" +
                "WHERE { " + 
                "   ?person a foaf:Person . " +
                "   OPTIONAL { ?person owl:sameAs ?sameas . }" +
                "   OPTIONAL { ?person rdfs:seeAlso ?seealso . }" +
                "}"
                ;
        
        String friend_construct = 
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
                "CONSTRUCT { ?person foaf:knows ?friend }" + 
                "WHERE { " + 
                "   ?person foaf:knows ?friend ." +
                "}"
                ;

        
        URIServer pool = new TDBURIPool(tdb_path, null);
        URIServer server = new TDBScheduler(tdb_path, pool);
        
        
        ModelCreator creator = new ContentTypeModelCreator();
        
        URIExtractor extractor = new SPARQLExtractor(server, extractor_select, null);
        
        ModelFilter filter = new SPARQLFilter(friend_construct, null);
        
        NamedModelStore store = new TDBModelStore(tdb_path);
        
        Pipeline pipeline = new Pipeline(creator, extractor, filter, store);
        
        MinerPool miners = new MinerPool(server, pipeline, 1);
        
       
        URI starting_uri = null;
        
        try {
            starting_uri = new URI(starting_point);
        } catch (URISyntaxException ex) {
            Logger.getLogger(LDCreeper.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        server.proposeURI(starting_uri);
        
        
        miners.start();
        miners.join();
    
    }
}
