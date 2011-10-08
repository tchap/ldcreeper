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
package ldcreeper.argparse.converters;

import ldcreeper.argparse.DBConnectionArgs;
import ldcreeper.argparse.exceptions.ConversionException;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class DBConnectionArgsConverter implements Converter<DBConnectionArgs>{

    @Override
    public DBConnectionArgs convert(String toConvert) {
        String db_name;
        String db_username;
        String db_host = null;
        Integer db_port = null;
        
        
        String[] parts = toConvert.split("@");
        
        if (parts.length > 2) {
            throw new ConversionException("No more than one '@' separator expected");
        }
        
        
        String[] login = parts[0].split(":");
        
        if (login.length != 2) {
            throw new ConversionException("Exactly one ':' separator expected");
        }
        
        db_username = login[0];
        db_name = login[1];
        
        
        if (parts.length > 1) {
            String[] conn = parts[1].split(":");
            
            if (conn.length > 2) {
                throw new ConversionException("No more than one ':' separator expected");
            }
            
            db_host = conn[0];
            
            if (conn.length > 1) {
                db_port = Integer.parseInt(conn[1]);
            }
        }
        
        return new DBConnectionArgs(db_username, db_name, db_host, db_port);
    }
    
}
