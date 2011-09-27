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

/*
 * TODO: Shorten imports
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import ldcreeper.argparse.ArgParser;
import ldcreeper.argparse.DBConnectionArgs;
import ldcreeper.argparse.Parameter;
import ldcreeper.argparse.exceptions.ArgParseException;
import ldcreeper.logging.formatters.ConsoleFormatter;
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
import ldcreeper.scheduling.TDBScheduler;
import ldcreeper.scheduling.URIServer;
import org.postgresql.ds.PGPoolingDataSource;


/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class LDCreeper {

    @Parameter(names="-v", description="Print verbose output to the console")
    private static Boolean verbose = false;
    
    @Parameter(names={"-l", "-logpattern"}, description="Pattern of the logfile " + ""
            + "as specified by java.util.logging.FileHandler")
    private static String log_pattern = null;
    
    @Parameter(names={"-t", "-threads"}, 
               description="Number of threads to start")
    private static Integer thread_count = 4;
    
    @Parameter(names="-tdb", 
               description="Directory path for tdb files to be used for " + 
                    "making downloaded models persistent")
    private static String tdb_path = null;
    
    @Parameter(names={"-p", "-postgres"},
               description="Specification of PostgreSQL DB connection " + 
                    "to be used for making URI pool persistent")
    private static DBConnectionArgs db_args = null;
    
    @Parameter(names={"-s", "-select-query"}, 
               description="Path to a file containing SPARQL SELECT query " +
                    "to be used for getting new links from models fetched")
    private static List<String> extractor_files = new ArrayList<String>();
    
    @Parameter(names={"-c", "-construct-query"},
               description="Path to a file containing SPARQL CONSTRUCT query " +
                    "to be used for building models being saved")
    private static List<String> miner_files = new ArrayList<String>();
    
    private static final Logger log = Logger.getLogger("ldcreeper");
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final String starting_point = "http://dig.csail.mit.edu/2008/webdav/timbl/foaf.rdf";
        

        String[] argz = {
            //"-h",
            "-v",
            "-t",           "5",
            "-tdb",         "/home/tchap/Matfyz/Rocnikovy_projekt/ldcreeper_runtime/TDB",
            //"-p",           "ldcreeper:ldcreeper_db",
            "-s",           "/home/tchap/Matfyz/Rocnikovy_projekt/ldcreeper_runtime/SPARQL/select.sparql",
            "-c",           "/home/tchap/Matfyz/Rocnikovy_projekt/ldcreeper_runtime/SPARQL/construct.sparql",
            //"-l",           "/"
        };
        
        ArgParser arg_parser = new ArgParser();
        
        try {
            arg_parser.addParametersFrom(LDCreeper.class);
            arg_parser.parse(argz);
        }
        catch (ArgParseException ex) {
            Logger.getLogger(LDCreeper.class.getName()).log(Level.SEVERE, 
                    "Arguments parser error", ex);
            System.exit(1);
        }
        
        
        initLogger();
        
        
        ModelCreator creator = getModelCreator(); 
          
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
            return;
        }
        
        server.submitURI(starting_uri);
        
        System.out.print("Starting in ");
        
        for (int i = 10; i > 0; --i) {
            System.out.print(Integer.toString(i) + " ");
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {}
        }
        
        System.out.println("GO!\n");
        
        
        miners.start();
        miners.join();
    
    }
    
    private static void initLogger() {
        log.setUseParentHandlers(false);
        
        for (Handler handler : log.getHandlers()) {
            log.removeHandler(handler);
        }
        
        
        ConsoleHandler console_handler = new ConsoleHandler();
        console_handler.setFormatter(new ConsoleFormatter());
        
        if (verbose) {
            console_handler.setLevel(Level.ALL);
        }
        else {
            console_handler.setLevel(Level.WARNING);
        }
        
        log.addHandler(console_handler);
        
        
        if (log_pattern != null) {
            FileHandler file_handler = null;
            
            try {
                file_handler = new FileHandler(log_pattern, 1000, 5);
            } catch (IOException ex) {
                log.log(Level.SEVERE, "I/O exception", ex);
                return;
            } catch (SecurityException ex) {
                log.log(Level.SEVERE, "Security exception", ex);
                return;
            }
            
            file_handler.setLevel(Level.WARNING);
            log.addHandler(file_handler);
        }
        
        
        LogManager.getLogManager().addLogger(log);
    }

    private static URIServer getURIServer() {
        if (db_args == null) {
            log.warning("No database specified for URI Server, " + 
                    "trying other solutions");
            
            if (tdb_path != null) {
                log.warning("Using TDB-backed URI Server, " + 
                        "that can cause performace problems");
                return new TDBScheduler(tdb_path);
            }
            
            log.warning("No persistent URI Server available, " +
                    "falling back to in-memory implementation");
            return new SimpleScheduler();
        }
        else {
            PGPoolingDataSource ds = new PGPoolingDataSource();

            ds.setDataSourceName("jdbc/postgres/pool/ldcreeper");
            ds.setUser(db_args.getDBUsername());
            ds.setDatabaseName(db_args.getDBName());
            
            String passwd = new String(db_args.getDBPassword());
            Arrays.fill(db_args.getDBPassword(), ' ');
            
            ds.setPassword(passwd);
            
            return new PostgresScheduler(ds);
        }
    }

    private static ModelCreator getModelCreator() {
        return new ContentTypeModelCreator();
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
                log.warning("Skipping SPARQL query");
                
                if (!extractor_file.exists()) {
                    log.log(Level.WARNING, "\t(file %s does not exist)", path);
                }
                else if (!extractor_file.canRead()) {
                    log.log(Level.WARNING, "\t(file %s not readable)", path);
                }
                else {
                    /*
                     * TODO: Print more specific message
                     */
                    log.warning("(unknown I/O error)");
                }
            }            
        }
        
        if (extractor == null) {
            log.warning("No SPARQL URI Extractor created, " + 
                    "using default (extract all URIs)");
            return new EveryURIExtractor(server, null);
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
                log.warning("Skipping SPARQL query ");
                
                if (!extractor_file.exists()) {
                    log.log(Level.WARNING, "(file %s does not exist)", path);
                }
                else if (!extractor_file.canRead()) {
                    log.log(Level.WARNING, "(file %s not readable)", path);
                }
                else {
                    /*
                     * TODO: Print more specific message
                     */
                    log.warning("(unknown I/O error)");
                }
            }            
        }
        
        if (filter == null) {
            log.warning("No Model Filter created, " + 
                    "using default (no filtering)");
            return new NullFilter(null);
        }
        
        return filter;
    }

    private static NamedModelStore getModelStore() {
        if (tdb_path == null) {
            log.warning("No TDB directory " + 
                    "for Model Store specified, using stdout...");
            return new SimpleModelStore();
        }
        else {
            return new TDBModelStore(tdb_path);
        }
    }

}
