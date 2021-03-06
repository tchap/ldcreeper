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
package ldcreeper.model.build;

import com.hp.hpl.jena.rdf.model.Model;
import java.net.URI;
import java.util.logging.Logger;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public abstract class ModelBuilder {
       
    public static ModelBuilder getModelBuilder() {
        ModelBuilder builder = new ContentTypeModelBuilder();
        
        Logger.getLogger("ldcreeper").info("ModelBuilder created");
        
        return new ContentTypeModelBuilder();
    }
    
    public abstract Model buildFromURI(URI uri);
    
    public abstract Model buildFromURI(String uri);
    
}
