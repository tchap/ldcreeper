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
package ldcreeper;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ldcreeper.scheduling.SimpleURIQueue;
import ldcreeper.scheduling.TDBScheduler;
import ldcreeper.scheduling.URIContext;
import ldcreeper.scheduling.URIServer;


/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
public class LDCreeper {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String starting_point = "http://dig.csail.mit.edu/2008/webdav/timbl/foaf.rdf";
        String tdb_path = "/home/tchap/Matfyz/Rocnikovy_projekt/ldcreeper_runtime/TDB/";
        String query_path = "/home/tchap/Matfyz/Rocnikovy_projekt/ldcreeper_runtime/query/";
        
        /*
         * TODO: Read query file into variable
         */
        /*
         * TODO: Rewrite query to use CONSTRUCT
         */
        String discovery_string = "";
        
        URIServer queue = new SimpleURIQueue();
        URIServer scheduler = new TDBScheduler(queue, tdb_path);
        
        /*
         * TODO: Initialization, create pipeline
         */
       
        URI starting_uri = null;
        
        try {
            starting_uri = new URI(starting_point);
        } catch (URISyntaxException ex) {
            Logger.getLogger(LDCreeper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        URIContext starting_context = new URIContext(starting_uri, 0);
        
        scheduler.proposeURI(starting_context);
        
        /*
         * miners.start();
         * miners.join();
         */
    }
}
