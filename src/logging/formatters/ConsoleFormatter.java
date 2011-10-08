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
package ldcreeper.logging.formatters;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class ConsoleFormatter extends Formatter {

    private static final String output_format = "[ %7s ] %s\n";
    
    
    @Override
    public String format(LogRecord record) {
        String message = record.getMessage();
        
        if (record.getThrown() != null) {
            message += ": " + record.getThrown().getMessage();
        }
        else if (record.getParameters() != null) {
            Object param = record.getParameters()[0];
            
            message = String.format(message, param.toString());
        }
        
        return String.format(output_format, 
                record.getLevel().toString(), message);
    }
    
}
