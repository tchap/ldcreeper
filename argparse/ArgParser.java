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


import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import ldcreeper.argparse.converters.Converter;
import ldcreeper.argparse.converters.DBConnectionArgsConverter;
import ldcreeper.argparse.converters.DoubleConverter;
import ldcreeper.argparse.converters.FileConverter;
import ldcreeper.argparse.converters.FloatConverter;
import ldcreeper.argparse.converters.IntegerConverter;
import ldcreeper.argparse.converters.LongConverter;
import ldcreeper.argparse.converters.StringConverter;
import ldcreeper.argparse.exceptions.ArgParseException;



/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class ArgParser {

    private static class ParameterRecord {
        
        private Parameter parameter;
        private Object object;
        private Field field;
        
        public ParameterRecord(Parameter p, Object o, Field f) {
            parameter = p;
            object = o;
            field = f;
        }

        public Field getField() {
            return field;
        }

        public Object getObject() {
            return object;
        }

        public Parameter getParameter() {
            return parameter;
        }
        
    }
    
    private ConverterFactory cfactory;
    private Map<String, ParameterRecord> parameters = new HashMap<String, ParameterRecord>();
    private ParameterRecord main_args = null;
    
    public ArgParser() {
        
        cfactory = new ConverterFactory() {

            @Override
            public Converter<?> converterForClass(Class cls) {
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
                else {
                    return null;
                }    
            }
        };
        
    }
    
    private void addParametersFrom(Class<?> cls, Object obj) {
        Field[] fields = cls.getDeclaredFields();
        
        System.out.println(Integer.toString(fields.length));
        
        for (Field field : fields) {
            Parameter param;
            
            System.out.println(field.toString());
            
            if ((param = field.getAnnotation(Parameter.class)) != null) {
                if (param.names().length != 0) {
                    for (String name : param.names()) {
                        addParameter(name, param, obj, field);
                    }
                }
                else {
                    addParameter(null, param, obj, field);
                }
            }
        }
    }
    
    public void addParametersFrom(Object obj) {
        addParametersFrom(obj.getClass(), obj);
    }
    
    public void addParametersFrom(Class<?> cls) {
        addParametersFrom(cls, cls);
    }
    
    private void addParameter(String name, Parameter p, Object o, Field f) {
        if (name == null) {
            setMainArgumentsRecord(p, o, f);
            return;
        }
        
        if (parameters.put(name, new ParameterRecord(p, o, f)) != null) {
            throw new ArgParseException("Multiple parameters with the same name");
        }
    } 
    
    private void setMainArgumentsRecord(Parameter p, Object o, Field f) {
        if (main_args == null) {
            if (!f.getGenericType().toString().equals("java.util.List<java.lang.String>")) {
                throw new ArgParseException("Exact type of List<String> " + 
                        "required for main arguments");
            }
            
            main_args = new ParameterRecord(p, o, f);
        }
        else {
            throw new ArgParseException("List for main arguments already set" +
                    "('names' skipped multiple times)");
        }
    }
    
    public void parse(String[] args) {
        
    }
    
    public void dump() {
        for (ParameterRecord rec : parameters.values()) {
            System.out.println("======================");
            System.out.print("Parameter: [ ");
            
            for (String name : rec.getParameter().names()) {
                System.out.print(name + ", ");
            }
            
            System.out.println("]");
            System.out.println("Object: " + rec.getObject());
            System.out.println("Field: " + rec.getField());
            System.out.println("======================");
        }
    }
}
