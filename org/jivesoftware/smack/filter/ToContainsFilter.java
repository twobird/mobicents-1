/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jivesoftware.smack.filter;

import org.jivesoftware.smack.packet.Packet;

/**
 * Filters for packets where the "to" field contains a specified value. For example,
 * the filter could be used to listen for all packets sent to a group chat nickname.
 *
 * @author Matt Tucker
 */
public class ToContainsFilter implements PacketFilter {

    private String to;

    /**
     * Creates a "to" contains filter using the "to" field part.
     *
     * @param to the to field value the packet must contain.
     */
    public ToContainsFilter(String to) {
        if (to == null) {
            throw new IllegalArgumentException("Parameter cannot be null.");
        }
        this.to = to.toLowerCase();
    }

    public boolean accept(Packet packet) {
        if (packet.getTo() == null) {
            return false;
        }
        else {
            return packet.getTo().toLowerCase().indexOf(to) != -1;
        }
    }
}