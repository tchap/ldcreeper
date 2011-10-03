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
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class PostgresScheduler extends URIServer {

    private final String next_sql = 
            "UPDATE scheduling " +
            "SET state = 2 " +
            "WHERE uri = ( " +
            "   SELECT uri " +
            "   FROM scheduling " +
            "   WHERE state = 1 " +
            "   LIMIT 1 " +
            ") " +
            "RETURNING uri ";
        
    
    private final String DUPLICATE_TABLE = "42P07";
    private final String UNIQUE_VIOLATION = "23505";
  
    
    private final DataSource ds;
        
    private final Object cond = new Object();
    
    private static final Logger log = Logger.getLogger("ldcreeper");
    

    public PostgresScheduler(DataSource ds) {
        this.ds = ds;
        
        create_table();
    }
    
    @Override
    public void submitURI(URI uri) {
        
        final String submit_sql =
                "INSERT INTO scheduling VALUES ( " + 
                "   '" + uri.toString() + "', " +
                "   1 " +
                ");";
        
        
        try {    
            execUpdate(submit_sql);
            log.log(Level.INFO, "SUBMIT %s", uri.toString());
            
            synchronized (cond) {
                cond.notify();
            }
        } catch (SQLException ex) {
            if (ex.getSQLState().equals(UNIQUE_VIOLATION)) {
                log.log(Level.INFO, "SKIP %s", uri.toString());
            }
            else {
                log.log(Level.SEVERE, "SQL exception", ex);
            }
        }
        
    }

    @Override
    public URI nextURI() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet result;
        String uri_string;
        
        try {
            try {
                conn = ds.getConnection();
                stmt = conn.createStatement();
                result = stmt.executeQuery(next_sql);
                
                if (result.next()) {
                    uri_string = result.getString("uri");
                }
                else {
                    return null;
                }
            }
            finally {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            }
        }
        catch (SQLException ex) {
            log.log(Level.SEVERE, "SQL exception", ex);
            return null;
        }
        
        
        URI uri;
        
        try {
            uri = new URI(uri_string);
        } catch (URISyntaxException ex) {
            return null;
        }
        
        log.log(Level.INFO, "NEXT %s", uri.toString());
        
        return uri;
    }

    @Override
    public void markURIVisited(URI uri) {
        final String visited_sql = 
                "UPDATE scheduling " +
                "SET state = 3 " +
                "WHERE uri = '" + uri.toString() + "' ; ";
        
        
        try {
            log.log(Level.INFO, "PROCESSED %s", uri.toString());
            
            execUpdate(visited_sql);
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "SQL exception", ex);
        }
    }

    @Override
    public Object getCond() {
        return cond;
    }

    private void create_table() {
        final String create_table_sql = 
                "CREATE TABLE scheduling ( " +
                "   uri VARCHAR(128) PRIMARY KEY, " +
                "   state INT " +
                ");";
        
        final String clean_table_sql = 
                "UPDATE scheduling " +
                "SET state = 1 " +
                "WHERE state = 2 ;";
        
        
        log.info("Creating table for PostgresScheduler");
        
        try {
            execUpdate(create_table_sql);
        } catch (SQLException ex) {
            if (!ex.getSQLState().equals(DUPLICATE_TABLE)) {
                log.log(Level.SEVERE, "SQL exception", ex);
                log.severe("Creating table failed");
                System.exit(1);
            }
            
            log.info("Table exists");
        }
        
        
        log.info("Cleaning up the table");
        
        try {
            execUpdate(clean_table_sql);
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "SQL exception", ex);
            log.severe("Table cleanup failed");
            System.exit(1);
        }
        
    }
    
    private int execUpdate(String sql) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            return stmt.executeUpdate(sql);
        }
        finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
}
