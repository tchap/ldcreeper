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
package ldcreeper.model.extract;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Var;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import ldcreeper.scheduling.URIContext;
import ldcreeper.scheduling.URIServer;

/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
public class SPARQLExtractor extends URIExtractor {

    private URIServer server;
    private Query query;

    public SPARQLExtractor(URIServer server, String query, URIExtractor extractor) {
        super(extractor);
        this.server = server;
        this.query = QueryFactory.create(query);
    }

    @Override
    public void extractFrom(Model model, int depth) {
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet result = qexec.execSelect();
        
        while (result.hasNext()) {
            Iterator<Var> vars = result.nextBinding().vars();
            
            while (vars.hasNext()) {
                Var var = vars.next();
                
                if (var.isURI()) {
                    URIContext uric;
                            
                    try {
                         uric = new URIContext(new URI(var.getURI()), depth);
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(SPARQLExtractor.class.getName()).log(Level.INFO, null, ex);
                        continue;
                    }
                    
                    server.proposeURI(uric);
                }
            }        
        }
        
        qexec.close();
    }
    
}
