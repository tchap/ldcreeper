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
package ldcreeper.model.extract;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.net.URI;
import java.net.URISyntaxException;
import ldcreeper.scheduling.URIServer;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class EveryURIExtractor extends URIExtractor {

    private final URIServer server;
    
    public EveryURIExtractor(URIServer server, URIExtractor next_extractor) {
        super(next_extractor);
        this.server = server;
    }

    @Override
    protected void extractFrom(Model model) {
        StmtIterator iter = model.listStatements();
        
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            
            URI uri;
            
            
            Resource subj = stmt.getSubject();
            
            if (subj.isURIResource()) {    
                try {
                    uri = new URI(subj.getURI());
                    server.submitURI(uri);
                } catch (URISyntaxException ex) {}
            }
            
            
            Resource obj = stmt.getObject().asResource();
            
            if (obj.isURIResource()) {
                try {
                    uri = new URI(obj.asResource().getURI());
                    server.submitURI(uri);
                } catch (URISyntaxException ex) {}
            }
        }
    }
    
}
