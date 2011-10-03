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
package ldcreeper.scheduling;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ldcreeper.argparse.DBConnectionArgs;
import ldcreeper.mining.sindice.SindiceFQQuery;
import ldcreeper.mining.sindice.SindiceNQQuery;
import ldcreeper.mining.sindice.SindiceQQuery;
import ldcreeper.mining.sindice.SindiceQueryExecution;
import org.postgresql.ds.PGPoolingDataSource;


/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public abstract class URIServer {
    
    public static URIServer getURIServer(DBConnectionArgs db_args,
            String tdb_path) {
       
        final Logger log = Logger.getLogger("ldcreeper");
        URIServer server;
        
        if (db_args == null) {
            log.warning("No database specified for URI Server, " + 
                    "trying other solutions");
            
            if (tdb_path != null) {
                log.warning("TDB directory specified for URI Server, " + 
                        "that can cause performace problems");
                server = new TDBScheduler(tdb_path);
            }
            
            log.warning("No TDB directory specified for URI Server, " +
                    "using in-memory implementation");
            server = new SimpleScheduler();
        }
        else {
            PGPoolingDataSource ds = new PGPoolingDataSource();

            ds.setDataSourceName("jdbc/postgres/pool/ldcreeper");
            ds.setUser(db_args.getDBUsername());
            ds.setDatabaseName(db_args.getDBName());
            
            String passwd = new String(db_args.getDBPassword());
            Arrays.fill(db_args.getDBPassword(), ' ');
            
            ds.setPassword(passwd);
            
            server = new PostgresScheduler(ds);
        }
        
        log.info("URIServer created");
        
        return server;
    }
    
    public void querySindiceForLinks(SindiceQQuery q_query, SindiceNQQuery nq_query,
            List<String> fq_queries, int page_count) {
    
        final Logger log = Logger.getLogger("ldcreeper");
        
        SindiceQueryExecution qexec = new SindiceQueryExecution();
        
        if (q_query != null || nq_query != null) { 
            if (q_query != null) {
                qexec.addQuery(q_query);
            }
            else {
                qexec.addQuery(new SindiceQQuery(""));
            }
            
            if (nq_query != null) {
                qexec.addQuery(nq_query);
            }
            
            qexec.addQuery(new SindiceFQQuery("format:RDF"));
            
            qexec.addQuery(new SindiceFQQuery("-format:RDFA"));
            qexec.addQuery(new SindiceFQQuery("-format:MICRODATA"));
            qexec.addQuery(new SindiceFQQuery("-format:MICROFORMAT"));
            qexec.addQuery(new SindiceFQQuery("-format:XFN"));
            qexec.addQuery(new SindiceFQQuery("-format:HCARD"));
            qexec.addQuery(new SindiceFQQuery("-format:HCALENDAR"));
            qexec.addQuery(new SindiceFQQuery("-format:HLISTING"));
            qexec.addQuery(new SindiceFQQuery("-format:HRESUME"));
            qexec.addQuery(new SindiceFQQuery("-format:LICENSE"));
            qexec.addQuery(new SindiceFQQuery("-format:GEO"));
            qexec.addQuery(new SindiceFQQuery("-format:ADR"));
            
            for (String fq : fq_queries) {
                qexec.addQuery(new SindiceFQQuery(fq));
            }
        }
        
        
        List<URI> links = qexec.getResultLinks(page_count);
        
        if (links == null) {
            log.log(Level.SEVERE, "Sindice query execution failed");
            return;
        }
        
        
        for (URI uri : links) {
            submitURI(uri);
        }
    }
    
    public abstract void submitURI(URI uri);
    
    public abstract URI  nextURI();
    
    public abstract void markURIVisited(URI uri);
    
    public abstract Object getCond();
    
}
