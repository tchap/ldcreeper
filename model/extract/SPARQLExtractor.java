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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import ldcreeper.scheduling.URIServer;

/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public class SPARQLExtractor extends URIExtractor {

    private final URIServer server;
    private final Query query;

    public SPARQLExtractor(URIServer server, String query, URIExtractor extractor) {
        super(extractor);
        this.server = server;
        this.query = QueryFactory.create(query);
    }

    @Override
    protected void extractFrom(Model model) {
        /*
         * TODO: Understand better how ResultSet works => more efficient code
         */
        System.err.println("Extracting new URIs from model");
        
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet result = qexec.execSelect();
        
        while (result.hasNext()) {
            System.err.println("Processing next binding");
            
            QuerySolution solution = result.nextSolution();
            Iterator<String> varNames = solution.varNames();
            
            while (varNames.hasNext()) {
                String varName = varNames.next();
                Resource res = solution.getResource(varName);
                
                System.err.println("Processing next variable: " + varName + " = " + res.toString());
                
                if (res.isURIResource()) {
                    URI uric;
                            
                    try {
                         uric = new URI(res.getURI());
                    } catch (URISyntaxException ex) {
                        continue;
                    }
                    
                    server.proposeURI(uric);
                }
            }        
        }
        
        qexec.close();
    }
    
}
