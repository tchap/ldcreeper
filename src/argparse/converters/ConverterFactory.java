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

import java.io.File;
import ldcreeper.argparse.DBConnectionArgs;
import ldcreeper.mining.sindice.SindiceFQQuery;
import ldcreeper.mining.sindice.SindiceNQQuery;
import ldcreeper.mining.sindice.SindiceQQuery;


/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class ConverterFactory {
    
    public static Converter<?> converterForClass(Class cls) {
        if (cls.equals(Integer.class)) {
            return new IntegerConverter();
        }
        else if (cls.equals(Long.class)) {
            return new LongConverter();
        }
        else if (cls.equals(Float.class)) {
            return new FloatConverter();
        }
        else if (cls.equals(Double.class)) {
            return new DoubleConverter();
        }
        else if (cls.equals(String.class)) {
            return new StringConverter();
        }
        else if (cls.equals(DBConnectionArgs.class)) {
            return new DBConnectionArgsConverter();
        }
        else if (cls.equals(File.class)) {
            return new FileConverter();
        }
        else if (cls.equals(Boolean.class)) {
            return new BooleanConverter();
        }
        else if (cls.equals(SindiceQQuery.class)) {
            return new SindiceQQueryConverter();
        }
        else if (cls.equals(SindiceNQQuery.class)) {
            return new SindiceNQQueryConverter();
        }
        else if (cls.equals(SindiceFQQuery.class)) {
            return new SindiceFQQueryConverter();
        }
        else {
            return null;
        }    
    }
    
}
