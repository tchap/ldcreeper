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
public class PostgresScheduler implements URIServer {

    private final DataSource ds;
  
    private final Object cond;

    public PostgresScheduler(DataSource ds) {
        this.ds = ds;
        
        cond = new Object();
        
        create_table();
        
        System.exit(1);
    }
    
    @Override
    public void submitURI(URI uri) {
        
        final String submit_sql =
                "INSERT INTO scheduling VALUES (" + 
                    uri.toString() + ", " +
                    "1" +
                ");";
        
        try {
            exec_update(submit_sql);
        } catch (SQLException ex) {
            Logger.getLogger(PostgresScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public URI nextURI() {
        
        final String next_sql = 
                "UPDATE scheduling" +
                "SET state = 2" +
                "WHERE uri = (" +
                "   SELECT uri" +
                "   FROM scheduling" +
                "   WHERE state = 1" +
                "   LIMIT 1" +
                ")" +
                "RETURNING uri";
        
        ResultSet result;
        String uri_string;
        
        try {
            result = exec_query(next_sql);
            uri_string = result.getString("uri");
        } catch (SQLException ex) {
            Logger.getLogger(PostgresScheduler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        
        URI uri;
        
        try {
            uri = new URI(uri_string);
        } catch (URISyntaxException ex) {
            Logger.getLogger(PostgresScheduler.class.getName()).log(Level.WARNING, null, ex);
            return null;
        }
        
        return uri;
    }

    @Override
    public void markURIVisited(URI uri) {
        
        final String visited_sql = 
                "UPDATE scheduling" +
                "SET state = 3" +
                "WHERE uri = " + uri.toString() + " AND state = 2 ;";
        
        try {
            if (exec_update(visited_sql) != 1) {
                Logger.getLogger(PostgresScheduler.class.getName()).log(Level.WARNING, "No such uri in 'processing' state");
            }
        } catch (SQLException ex) {
            Logger.getLogger(PostgresScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Object getCond() {
        return cond;
    }

    private void create_table() {
        
        final String create_table_sql = 
                "CREATE TABLE scheduling (" +
                "   uri VARCHAR(128) PRIMARY KEY," +
                "   state INT" +
                ");";
        
        final String clean_table_sql = 
                "UPDATE scheduling" +
                "SET state = 1" +
                "WHERE state = 2 ;";
        
        try {
            exec_update(create_table_sql);
            exec_update(clean_table_sql);
        } catch (SQLException ex) {
            Logger.getLogger(PostgresScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private int exec_update(String sql) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            return stmt.executeUpdate(sql);
        }
        finally {
            stmt.close();
            conn.close();
        }
    }
    
    private ResultSet exec_query(String sql) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            return stmt.executeQuery(sql);
        }
        finally {
            stmt.close();
            conn.close();
        }
    }
    
}
