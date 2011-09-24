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
package ldcreeper.argparse;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class DBConnectionArgs {
    
    private final String default_host = "localhost";
    private final Integer default_port = 5432;
    
    private final String db_name;
    private final String db_username;
    private final String db_password;
    
    private final String db_host;
    private final Integer db_port;

    public DBConnectionArgs(String db_username, String db_name, String db_host, Integer db_port) {
        this.db_username = db_username;
        this.db_name = db_name;
        
        if (db_host == null) {
            this.db_host = default_host;
        }
        else {
            this.db_host = db_host;
        }
        
        if (db_port == null) {
            this.db_port = default_port;
        }
        else {
            this.db_port = db_port;
        }
        
        db_password = ask_for_password();
    }

    private String ask_for_password() {
        System.out.println("Password (" + db_username + ":" + db_name + "@" +
                db_host + ":" + Integer.toString(db_port) + "):");
        
        return "ldcreeper_supersecret_passwd";
    }

    public String getDBHost() {
        return db_host;
    }

    public String getDBName() {
        return db_name;
    }

    public String getDBPassword() {
        return db_password;
    }

    public Integer getDBPort() {
        return db_port;
    }

    public String getDBUsername() {
        return db_username;
    }
    
    
}
