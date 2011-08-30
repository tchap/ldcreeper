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
package ldcreeper.scheduling;

import java.net.URI;


/**
 *
 * @author Ondrej Kupka <ondra DOT cap AT gmail DOT com>
 */
public abstract class URIServer {
    
    private final URIServer next_server;

    public URIServer(URIServer next_server) {
        this.next_server = next_server;
    }
    
    public boolean proposeURI(URI uri) {
        if (propose(uri)) {
            if (next_server != null) {
                return next_server.proposeURI(uri);
            }
            else {
                return true;
            }
        }
        else {
            return false;
        }
    }
    
    public URI requestURI() {
        if (next_server != null) {
            return next_server.requestURI();
        }
        else {
            return null;
        }
    }
    
    public Object getLock() {
        if (next_server != null) {
            return next_server.getLock();
        }
        else {
            return null;
        }
    }
    
    abstract protected boolean propose(URI uri);
    
}
