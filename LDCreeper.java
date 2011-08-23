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
import ldcreeper.mining.MinerPool;
import ldcreeper.mining.TDBPipeline;
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
        URIServer queue = new SimpleURIQueue();
        URIServer scheduler = new TDBScheduler(queue);
        
        TDBPipeline pipeline = new TDBPipeline(scheduler, null);
        
        MinerPool miners = new MinerPool(scheduler, pipeline, 4);
        
        URI start_uri = null;
        
        try {
            start_uri = new URI("http://dbpedia.org/data/The_Lord_of_the_Rings.rdf");
        } catch (URISyntaxException ex) {
            Logger.getLogger(LDCreeper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        URIContext start_context = new URIContext(start_uri, 0);
        
        scheduler.proposeURI(start_context);
        
        miners.start();
        miners.join();
    }
}
