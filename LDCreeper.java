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

import java.io.IOException;
import java.util.ArrayList;
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
import ldcreeper.mining.sindice.SindiceNQQuery;
import ldcreeper.mining.sindice.SindiceQQuery;
import ldcreeper.model.build.ModelBuilder;
import ldcreeper.model.extract.URIExtractor;
import ldcreeper.model.mine.ModelMiner;
import ldcreeper.model.store.ModelStore;
import ldcreeper.scheduling.URIServer;


/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
/*
 * TODO: Add info messages everywhere
 */
public class LDCreeper {

    @Parameter(names="-v", description="Print verbose output to the console")
    private static Boolean verbose = false;
    
    @Parameter(names={"-l", "-logpattern"}, description="Pattern of " + 
            "the logfile as specified by java.util.logging.FileHandler")
    private static String log_pattern = null;
    
    @Parameter(names={"-t", "-threads"}, 
               description="Number of threads to start")
    private static Integer thread_count = 4;
    
    @Parameter(names="-q", description="Add sindice keyword search query " +
            "to get initial URI set")
    private static SindiceQQuery q_query = null;
    
    @Parameter(names="-nq", description="Add sindice ntriple search query " +
            "to get initial URI set")
    private static SindiceNQQuery nq_query = null;
    
    @Parameter(names="-fq", description="Add sindice filter search query " +
            "to get initial URI set. Filter query can be specified " + 
            "multiple times.")
    private static List<String> fq_queries = new ArrayList<String>();
    
    @Parameter(names={"-p", "-pages"}, description="Number of pages to be " + 
            "requested from Sindice")
    private static Integer page_count = 10;
    
    @Parameter(names="-tdb", 
               description="Directory path for tdb files to be used for " + 
                    "making downloaded models persistent")
    private static String tdb_path = null;
    
    @Parameter(names="-postgres",
               description="Specification of PostgreSQL DB connection " + 
                    "to be used for making URI pool persistent. " +
                    "Format is user:database@host:port. Host and port " +
                    "can be skipped if localhost:5432.")
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
        
        String[] argz = {
            //"-h",
            "-v",
            "-t",           "5",
            "-q",           "Tim Berners-Lee foaf",
            "-nq",          "* <rdf:type> <foaf:Person>",
            "-p",           "3",
            //"-tdb",         "/home/tchap/Matfyz/Rocnikovy_projekt/ldcreeper_runtime/TDB",
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
        
        
        initLogging();
        
       
        log.info("Initializing pipeline");
        
        ModelBuilder builder = ModelBuilder.getModelBuilder(); 
          
        URIServer server = URIServer.getURIServer(db_args, tdb_path);
        
        URIExtractor extractor = URIExtractor.getURIExtractor(extractor_files, 
                server);
        
        ModelMiner miner = ModelMiner.getModelMiner(miner_files);
        
        ModelStore store = ModelStore.getModelStore(tdb_path);
        
        
        Pipeline pipeline = new Pipeline(builder, extractor, miner, store);
        
        MinerPool miners = new MinerPool(server, pipeline, thread_count);
        
        log.info("Pipeline initialized");
        
        
        server.querySindiceForLinks(q_query, nq_query, fq_queries, page_count);
        
        
        miners.start();
        miners.join();
    
    }
    
    private static void initLogging() {
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

}
