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


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ldcreeper.argparse.converters.Converter;
import ldcreeper.argparse.converters.ConverterFactory;
import ldcreeper.argparse.exceptions.ArgParseException;
import ldcreeper.argparse.exceptions.ConversionException;



/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class ArgParser {

    private void help(String string) {
        /*
         * TODO: Print help
         */
        System.err.println(string);
        System.exit(1);
    }

    private class ParameterRecord {
        
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
    
    private Map<String, ParameterRecord> parameters = new HashMap<String, ParameterRecord>();
    private ParameterRecord main_args = null;
    
    
    private void addParametersFrom(Class<?> cls, Object obj) {
        Field[] fields = cls.getDeclaredFields();
        
        for (Field field : fields) {
            Parameter param;
            
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
        if (parameters.put(name, new ParameterRecord(p, o, f)) != null) {
            throw new ArgParseException("Multiple parameters with the same name");
        }
    }
    
    public void parse(String[] args) {
        /*
         * state 0 -> expecting option
         * state 1 -> expecting value
         */
        int state = 0;
        String parameter = "";
        ParameterRecord rec = null;
        
        for (String arg : args) {
            if (state == 0) {
                if ((rec = parameters.get(arg)) == null) {
                    help("Unknown option: " + arg);
                }
                
                parameter = arg;
                state = 1;
            }
            else {
                Field field = rec.getField();
                field.setAccessible(true);
                
                Object instance = rec.getObject();
                
                Class<?> cls = field.getType();
                
                if (cls.equals(List.class)) {
                    try {
                        Object list = field.get(instance);
                        
                        Class<?>[] params = {Object.class};
                        Method add = list.getClass().getDeclaredMethod("add", params);

                        add.invoke(list, arg);
                    }
                    catch (NoSuchMethodException ex) {
                        Logger.getLogger(ArgParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SecurityException ex) {
                        Logger.getLogger(ArgParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(ArgParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(ArgParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex) {
                        Logger.getLogger(ArgParser.class.getName()).log(Level.SEVERE, null, ex);
                    }                    
                }
                else {
                    Converter<?> converter = ConverterFactory.converterForClass(cls);

                    if (converter == null) {
                        throw new ConversionException("Converter not found for " 
                                + cls.toString());
                    }
                    
                    try {
                        if (field.get(instance) != null) {
                            throw new ArgParseException("Parameter " + 
                                    parameter + " specified multiple times");
                        }
                        
                        field.set(instance, converter.convert(arg));
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(ArgParser.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(ArgParser.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
  
                
                state = 0;
            }
        }
        
        if (state == 1) {
            help("Argument expected after: " + args[-1]);
        }
    }
    
}
