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

    private final Map<String, ParameterRecord> parameters = 
            new HashMap<String, ParameterRecord>();
    
    private final Map<String, ParameterRecord> switch_parameters =
            new HashMap<String, ParameterRecord>();
    
    private StringBuilder help_builder = new StringBuilder( 
            "Usage: java -jar <ldcreeper jar> [ OPTION... ]\n" + 
            "where OPTION is one of:\n");
   
    
    private void addParametersFrom(Class<?> cls, Object obj) {
        final String line_format = "\t%-30s %s\n";
        
        Field[] fields = cls.getDeclaredFields();
        
        
        for (Field field : fields) {
            Parameter param;
            
            if ((param = field.getAnnotation(Parameter.class)) != null) {
                if (param.names().length != 0) {
                    String names_str = "";
                    
                    for (String name : param.names()) {
                        if (field.getType().equals(Boolean.class)) {
                            addSwitchParameter(name, param, obj, field);
                        }
                        else {
                            addParameter(name, param, obj, field);
                        }
                        
                        names_str += name + ", ";
                    }
                    
                    names_str = names_str.substring(0, names_str.length() - 2);
                    
                    if (!field.getType().equals(Boolean.class)) {
                        names_str += " VALUE";
                    }
                    
                    help_builder.append(String.format(line_format, names_str, 
                            formatDescription(param.description(), 31)));
                }
                else {
                    throw new ArgParseException("Parameter names empty for " +
                            "field " + field.toString());
                }
            }
        }
        
        help_builder.append(String.format(line_format, "-h, -help", 
                "Print this help"));
    }
    
    public void addParametersFrom(Object obj) {
        addParametersFrom(obj.getClass(), obj);
    }
    
    public void addParametersFrom(Class<?> cls) {
        addParametersFrom(cls, cls);
    }
    
    private void addParameter(String name, Parameter p, Object o, Field f) {
        if (parameters.put(name, new ParameterRecord(p, o, f)) != null) {
            throw new ArgParseException("Multiple parameters with " + 
                    "the same name: " + name);
        }
    }
    
    private void addSwitchParameter(String name, Parameter p, Object o, Field f) {
        if (switch_parameters.put(name, new ParameterRecord(p, o, f)) != null) {
            throw new ArgParseException("Multiple parameters with " + 
                    "the same name: " + name);
        }
    }
    
    public void parse(String[] args) {
        if (args.length == 0) {
            help(null);
        }
        
        /*
         * state 0 -> expecting option
         * state 1 -> expecting value
         */
        int state = 0;
        
        String parameter;
        ParameterRecord rec = null;
        
        for (String arg : args) {
            if (state == 0) {
                if (arg.equals("-h") || arg.equals("-help")) {
                    help(null);
                }
                
                if ((rec = switch_parameters.get(arg)) != null) {
                    setField(rec.getField(), rec.getObject(), "");
                }
                else if ((rec = parameters.get(arg)) != null) {
                    parameter = arg;
                    state = 1;
                }
                else {
                    help("Unknown parameter: " + arg);
                }
            }
            else {
                setField(rec.getField(), rec.getObject(), arg);
                state = 0;
            }
        }
        
        if (state == 1) {
            help("Argument expected after " + args[-1]);
        }
    }
    
    private void setField(Field field, Object instance, Object value) {
        field.setAccessible(true);
        
        Class<?> cls = field.getType();

        if (cls.equals(List.class)) {
            try {
                Object list = field.get(instance);

                Class<?>[] params = { Object.class };
                Method add = list.getClass().getDeclaredMethod("add", params);

                add.invoke(list, value);
            }
            catch (Exception ex) {
                Logger.getLogger(ArgParser.class.getName()).log(Level.SEVERE, 
                        "Reflection exception", ex);
                System.exit(1);
            }                  
        }
        else {
            Converter<?> converter = ConverterFactory.converterForClass(cls);

            if (converter == null) {
                throw new ConversionException("Converter not found for " 
                        + cls.toString());
            }

            try {
                field.set(field.get(instance), converter.convert((String)value));
            } catch (Exception ex) {
                Logger.getLogger(ArgParser.class.getName()).log(Level.SEVERE, 
                        "Reflection exception", ex);
                System.exit(1);
            } 
        }
    }
    
    private void help(String str) {
        if (str == null) {
            System.out.println(help_builder.toString());
            System.exit(0);
        }
        else {
            System.err.println(str);
            System.out.println(help_builder.toString());
            System.exit(1);
        }
    }

    private static String formatDescription(String desc, int offset) {
        final String line_prefix = String.format("%" + 
                Integer.toString(offset) + "s", "");
        int width = 80 - offset;
        
        if (desc.length() <= width) {
            return desc + "\n";
        }
        
        String[] words = desc.split("\\s");
        StringBuilder desc_builder = new StringBuilder(words[0]);
        int line_width = words[0].length();
        
        for (int i = 1; i < words.length; ++i) {
            if (line_width + 1 + words[i].length() < width) {
                desc_builder.append(" ");
                desc_builder.append(words[i]);
                line_width += words[i].length() + 1;
            }
            else {
                desc_builder.append("\n\t");
                desc_builder.append(line_prefix);
                desc_builder.append(words[i]);
                line_width = words[i].length();
            }
        }
        
        desc_builder.append("\n");
        
        return desc_builder.toString();
    }
    
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
    
}
