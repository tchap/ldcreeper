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

/**
 *
 * @author Ondrej Kupka <ondra dot cap at gmail dot com>
 */
public class CountFilter implements URIServer {
    
    private URIServer server;
    private int max_count;
    private int cur_count;

    public CountFilter(URIServer server, int max_count) {
        this.server = server;
        this.max_count = max_count;
        this.cur_count = 0;
    }
    
    @Override
    public void proposeURI(URIContext uri) {
        if (cur_count <= max_count) {
            server.proposeURI(uri);
            cur_count += 1;
        }
    }

    @Override
    public URIContext requestURI() {
        return server.requestURI();
    }
    
}
