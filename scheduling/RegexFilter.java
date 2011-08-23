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
package ldcreeper.scheduling;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
public class RegexFilter implements URIServer {
    
    private URIServer server;
    private Pattern patt;

    public RegexFilter(URIServer server, String regex) {
        this.server = server;
        patt = Pattern.compile(regex);
    }
    
    @Override
    public void proposeURI(URIContext uri) {
        Matcher m = patt.matcher(uri.getURI().toString());
        
        if (m.matches()) {
            server.proposeURI(uri);
        }
    }

    @Override
    public URIContext requestURI() {
        return server.requestURI();
    }
    
}
