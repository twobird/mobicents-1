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

package org.jivesoftware.smack;

import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.IQ;

import java.util.*;

/**
 * Each user in your roster is represented by a roster entry, which contains the user's
 * JID and a name or nickname you assign.
 *
 * @author Matt Tucker
 */
public class RosterEntry {

    private String user;
    private String name;
    private RosterPacket.ItemType type;
    private RosterPacket.ItemStatus status;
    private XMPPConnection connection;

    /**
     * Creates a new roster entry.
     *
     * @param user the user.
     * @param name the nickname for the entry.
     * @param type the subscription type.
     * @param status the subscription status (related to subscriptions pending to be approbed).
     * @param connection a connection to the XMPP server.
     */
    RosterEntry(String user, String name, RosterPacket.ItemType type,
                RosterPacket.ItemStatus status, XMPPConnection connection) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.status = status;
        this.connection = connection;
    }

    /**
     * Returns the JID of the user associated with this entry.
     *
     * @return the user associated with this entry.
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the name associated with this entry.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name associated with this entry.
     *
     * @param name the name.
     */
    public void setName(String name) {
        // Do nothing if the name hasn't changed.
        if (name != null && name.equals(this.name)) {
            return;
        }
        this.name = name;
        RosterPacket packet = new RosterPacket();
        packet.setType(IQ.Type.SET);
        packet.addRosterItem(toRosterItem(this));
        connection.sendPacket(packet);
    }

    /**
     * Updates the state of the entry with the new values.
     *
     * @param name the nickname for the entry.
     * @param type the subscription type.
     * @param status the subscription status (related to subscriptions pending to be approbed).
     */
    void updateState(String name, RosterPacket.ItemType type, RosterPacket.ItemStatus status) {
        this.name = name;
        this.type = type;
        this.status = status;
    }

    /**
     * Returns an iterator for all the roster groups that this entry belongs to.
     *
     * @return an iterator for the groups this entry belongs to.
     */
    public Iterator getGroups() {
        List results = new ArrayList();
        // Loop through all roster groups and find the ones that contain this
        // entry. This algorithm should be fine
        for (Iterator i=connection.roster.getGroups(); i.hasNext(); ) {
            RosterGroup group = (RosterGroup)i.next();
            if (group.contains(this)) {
                results.add(group);
            }
        }
        return results.iterator();
    }

    /**
     * Returns the roster subscription type of the entry. When the type is
     * {@link RosterPacket.ItemType#NONE} or {@link RosterPacket.ItemType#FROM},
     * refer to {@link RosterEntry getStatus()} to see if a subscription request
     * is pending.
     *
     * @return the type.
     */
    public RosterPacket.ItemType getType() {
        return type;
    }

    /**
     * Returns the roster subscription status of the entry. When the status is
     * RosterPacket.ItemStatus.SUBSCRIPTION_PENDING, the contact has to answer the
     * subscription request.
     *
     * @return the status.
     */
    public RosterPacket.ItemStatus getStatus() {
        return status;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (name != null) {
            buf.append(name).append(": ");
        }
        buf.append(user);
        Iterator groups = getGroups();
        if (groups.hasNext()) {
            buf.append(" [");
            RosterGroup group = (RosterGroup)groups.next();
            buf.append(group.getName());
            while (groups.hasNext()) {
            buf.append(", ");
                group = (RosterGroup)groups.next();
                buf.append(group.getName());
            }
            buf.append("]");
        }
        return buf.toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object != null && object instanceof RosterEntry) {
            return user.equals(((RosterEntry)object).getUser());
        }
        else {
            return false;
        }
    }

    static RosterPacket.Item toRosterItem(RosterEntry entry) {
        RosterPacket.Item item = new RosterPacket.Item(entry.getUser(), entry.getName());
        item.setItemType(entry.getType());
        item.setItemStatus(entry.getStatus());
        // Set the correct group names for the item.
        for (Iterator j=entry.getGroups(); j.hasNext(); ) {
            RosterGroup group = (RosterGroup)j.next();
            item.addGroupName(group.getName());
        }
        return item;
    }
}