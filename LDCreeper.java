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
import ldcreeper.scheduling.SimpleURIQueue;
import ldcreeper.scheduling.TDBScheduler;
import ldcreeper.scheduling.URIContext;
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
         * TODO: Read query from file
         */
        /*
         * TODO: Not sure about the queries
         */
        String friend_select = 
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
                "SELECT DISTINCT ?friend " +
                "WHERE { " + 
                "   { ?person foaf:knows ?friend . }" +
                "   union" +
                "   { ?friend foaf:knows ?person . }" +
                "}"
                ;
        
        String ss_select = 
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "SELECT DISTINCT ?sameas_seealso " +
                "WHERE { " + 
                "   ?person a foaf:Person . " +
                "   {" +
                "       { ?person owl:sameAs ?sameas_seealso . }" +
                "       union" +
                "       { ?person rdfs:seeAlso ?sameas_seealso . }" +
                "   }" +
                "}"
                ;
        
        String friend_construct = 
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
                "CONSTRUCT { ?person foaf:knows ?friend }" + 
                "WHERE { " + 
                "   ?person foaf:knows ?friend ." +
                "}"
                ;

        
        URIServer queue = new SimpleURIQueue(null);
        URIServer scheduler = new TDBScheduler(tdb_path, queue);
        
        URIServer server = scheduler;
        
        ModelCreator creator = new ContentTypeModelCreator();
        
        URIExtractor friend_extractor = new SPARQLExtractor(server, friend_select, null);
        URIExtractor sameas_extractor = new SPARQLExtractor(server, ss_select, friend_extractor);
        
        ModelFilter filter = new SPARQLFilter(friend_construct, null);
        
        NamedModelStore store = new TDBModelStore(tdb_path);
        
        Pipeline pipeline = new Pipeline(creator, sameas_extractor, filter, store);
        
        MinerPool miners = new MinerPool(server, pipeline, 4);
        
       
        URI starting_uri = null;
        
        try {
            starting_uri = new URI(starting_point);
        } catch (URISyntaxException ex) {
            Logger.getLogger(LDCreeper.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        URIContext starting_context = new URIContext(starting_uri, 0);
        
        server.proposeURI(starting_context);
        
        
        miners.start();
        miners.join();
    
    }
}
