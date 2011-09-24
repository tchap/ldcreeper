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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ldcreeper.argparse.ArgParser;
import ldcreeper.argparse.DBConnectionArgs;
import ldcreeper.argparse.Parameter;
import ldcreeper.mining.MinerPool;
import ldcreeper.mining.Pipeline;
import ldcreeper.model.create.ContentTypeModelCreator;
import ldcreeper.model.create.ModelCreator;
import ldcreeper.model.extract.EveryURIExtractor;
import ldcreeper.model.extract.SPARQLExtractor;
import ldcreeper.model.extract.URIExtractor;
import ldcreeper.model.filter.ModelFilter;
import ldcreeper.model.filter.NullFilter;
import ldcreeper.model.filter.SPARQLFilter;
import ldcreeper.model.store.NamedModelStore;
import ldcreeper.model.store.SimpleModelStore;
import ldcreeper.model.store.TDBModelStore;
import ldcreeper.scheduling.PostgresScheduler;
import ldcreeper.scheduling.SimpleScheduler;
import ldcreeper.scheduling.URIServer;
import org.postgresql.ds.PGPoolingDataSource;


/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class LDCreeper {

    @Parameter(names={"-t", "-threads"}, 
               description="Number of threads to start")
    private static Integer thread_count = 4;
    
    @Parameter(names="-tdb", 
               description="Directory path for tdb files to be used for " + 
                    "making downloaded models persistent")
    private static String tdb_path = null;
    
    @Parameter(names="-postgres",
               description="Specification of PostgreSQL DB connection " + 
                    "to be used for making URI pool persistent")
    private static DBConnectionArgs db_args = null;
    
    @Parameter(names={"-s", "-select-query"}, 
               description="Path to a file containing SPARQL SELECT query " +
                    "to be used for getting new links from models fetched", 
               required=true)
    private static List<String> extractor_files = new ArrayList<String>();
    
    @Parameter(names={"-c", "-construct-query"},
               description="Path to a file containing SPARQL CONSTRUCT query " +
                    "to be used for building models being saved")
    private static List<String> miner_files = new ArrayList<String>();
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final String starting_point = "http://dig.csail.mit.edu/2008/webdav/timbl/foaf.rdf";
        

        String[] argz = {
            "-t",           "5",
            "-tdb",         "/home/tchap/Matfyz/Rocnikovy_projekt/ldcreeper_runtime/TDB",
            "-postgres",    "ldcreeper:ldcreeper_db",
            "-s",           "/home/tchap/Matfyz/Rocnikovy_projekt/ldcreeper_runtime/SPARQL/select.sparql",
            "-c",           "/home/tchap/Matfyz/Rocnikovy_projekt/ldcreeper_runtime/SPARQL/construct.sparql"
        };
        
        
        ArgParser arg_parser = new ArgParser();
        
        arg_parser.addParametersFrom(LDCreeper.class);
        arg_parser.parse(argz);
        
        
        ModelCreator creator = new ContentTypeModelCreator();
          
        URIServer server = getURIServer();
        
        URIExtractor extractor = getURIExtractor(server);
        
        ModelFilter filter = getModelFilter();
        
        NamedModelStore store = getModelStore();
        
        
        Pipeline pipeline = new Pipeline(creator, extractor, filter, store);
        
        MinerPool miners = new MinerPool(server, pipeline, thread_count);
        
       
        URI starting_uri = null;
        
        try {
            starting_uri = new URI(starting_point);
        } catch (URISyntaxException ex) {
            Logger.getLogger(LDCreeper.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        server.submitURI(starting_uri);
        
        
        System.out.println("\nWaiting 10 seconds before starting...\n");
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {}
        
        
        miners.start();
        miners.join();
    
    }
    
    private static URIServer getURIServer() {
        if (db_args == null) {
            System.err.println("WARNING: No database " + 
                    "for URI Server specified, using in-memory URI Server...");
            return new SimpleScheduler();
        }
        else {
            PGPoolingDataSource ds = new PGPoolingDataSource();

            ds.setDataSourceName("jdbc/postgres/pool/ldcreeper");
            ds.setUser(db_args.getDBUsername());
            ds.setPassword(db_args.getDBPassword());
            ds.setDatabaseName(db_args.getDBName());
            
            return new PostgresScheduler(ds);
        }
    }

    private static URIExtractor getURIExtractor(URIServer server) {
        URIExtractor extractor = null;
        
        for (String path : extractor_files) {
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
                Logger.getLogger(LDCreeper.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("WARNING: Skipping SPARQL query from file " + path);
            }            
        }
        
        if (extractor == null) {
            System.err.println("WARNING: No SPARQL URI Extractor created, " + 
                    "using default (extract all links)");
            return new EveryURIExtractor(null);
        }
        
        return extractor;
    }

    private static ModelFilter getModelFilter() {
        ModelFilter filter = null;
        
        for (String path : miner_files) {
            File extractor_file = new File(path);

            String sparql = "";

            BufferedReader reader = null;
            String line;

            try {
                reader = new BufferedReader(new FileReader(extractor_file));
                
                while ((line = reader.readLine()) != null) {
                    sparql += line;
                }
                
                filter = new SPARQLFilter(sparql, filter);
            } catch (IOException ex) {
                Logger.getLogger(LDCreeper.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("WARNING: Skipping SPARQL query from file " + path);
            }            
        }
        
        if (filter == null) {
            System.err.println("WARNING: No Model Filter created, " + 
                    "using default (no filtering)");
            return new NullFilter(null);
        }
        
        return filter;
    }

    private static NamedModelStore getModelStore() {
        if (tdb_path == null) {
            System.err.println("WARNING: No TDB directory " + 
                    "for Model Store specified, using stdout...");
            return new SimpleModelStore();
        }
        else {
            return new TDBModelStore(tdb_path);
        }
    }
    
}
