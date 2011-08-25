/*
 * Copyright (C) 2011 Ondrej Kupka <ondra dot cap at gmail dot com>
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
package ldcreeper.model.filter;

import com.hp.hpl.jena.rdf.model.Model;

/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
public abstract class ModelFilter {
    
    private ModelFilter next_filter;

    public ModelFilter(ModelFilter next_filter) {
        this.next_filter = next_filter;
    }
    
    public Model filterModel(Model model) {
        Model filtered = filter(model);
        
        if (next_filter != null) {
            return next_filter.filterModel(filtered);
        }
        else {
            return filtered;
        }
    }
    
    abstract protected Model filter(Model model);
    
}
