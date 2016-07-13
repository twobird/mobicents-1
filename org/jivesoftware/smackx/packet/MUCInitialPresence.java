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

package org.jivesoftware.smackx.packet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.jivesoftware.smack.packet.PacketExtension;

/**
 * Represents extended presence information whose sole purpose is to signal the ability of 
 * the occupant to speak the MUC protocol when joining a room. If the room requires a password 
 * then the MUCInitialPresence should include one.<p>
 * 
 * The amount of discussion history provided on entering a room (perhaps because the 
 * user is on a low-bandwidth connection or is using a small-footprint client) could be managed by
 * setting a configured History instance to the MUCInitialPresence instance. 
 * @see MUCInitialPresence#setHistory(MUCInitialPresence.History).
 *
 * @author Gaston Dombiak
 */
public class MUCInitialPresence implements PacketExtension {

    private String password;
    private History history; 

    public String getElementName() {
        return "x";
    }

    public String getNamespace() {
        return "http://jabber.org/protocol/muc";
    }

    public String toXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<").append(getElementName()).append(" xmlns=\"").append(getNamespace()).append(
            "\">");
        if (getPassword() != null) {
            buf.append("<password>").append(getPassword()).append("</password>");
        }
        if (getHistory() != null) {
            buf.append(getHistory().toXML());
        }
        buf.append("</").append(getElementName()).append(">");
        return buf.toString();
    }

    /**
     * Returns the history that manages the amount of discussion history provided on 
     * entering a room.
     * 
     * @return the history that manages the amount of discussion history provided on 
     * entering a room.
     */
    public History getHistory() {
        return history;
    }

    /**
     * Returns the password to use when the room requires a password.
     * 
     * @return the password to use when the room requires a password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the History that manages the amount of discussion history provided on 
     * entering a room.
     * 
     * @param history that manages the amount of discussion history provided on 
     * entering a room.
     */
    public void setHistory(History history) {
        this.history = history;
    }

    /**
     * Sets the password to use when the room requires a password.
     * 
     * @param password the password to use when the room requires a password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * The History class controls the number of characters or messages to receive
     * when entering a room.
     * 
     * @author Gaston Dombiak
     */
    public static class History {

        private int maxChars = -1;
        private int maxStanzas = -1; 
        private int seconds = -1; 
        private Date since; 

        /**
         * Returns the total number of characters to receive in the history.
         * 
         * @return total number of characters to receive in the history.
         */
        public int getMaxChars() {
            return maxChars;
        }

        /**
         * Returns the total number of messages to receive in the history.
         * 
         * @return the total number of messages to receive in the history.
         */
        public int getMaxStanzas() {
            return maxStanzas;
        }

        /**
         * Returns the number of seconds to use to filter the messages received during that time. 
         * In other words, only the messages received in the last "X" seconds will be included in 
         * the history.
         * 
         * @return the number of seconds to use to filter the messages received during that time.
         */
        public int getSeconds() {
            return seconds;
        }

        /**
         * Returns the since date to use to filter the messages received during that time. 
         * In other words, only the messages received since the datetime specified will be 
         * included in the history.
         * 
         * @return the since date to use to filter the messages received during that time.
         */
        public Date getSince() {
            return since;
        }

        /**
         * Sets the total number of characters to receive in the history.
         * 
         * @param maxChars the total number of characters to receive in the history.
         */
        public void setMaxChars(int maxChars) {
            this.maxChars = maxChars;
        }

        /**
         * Sets the total number of messages to receive in the history.
         * 
         * @param maxStanzas the total number of messages to receive in the history.
         */
        public void setMaxStanzas(int maxStanzas) {
            this.maxStanzas = maxStanzas;
        }

        /**
         * Sets the number of seconds to use to filter the messages received during that time. 
         * In other words, only the messages received in the last "X" seconds will be included in 
         * the history.
         * 
         * @param seconds the number of seconds to use to filter the messages received during 
         * that time.
         */
        public void setSeconds(int seconds) {
            this.seconds = seconds;
        }

        /**
         * Sets the since date to use to filter the messages received during that time. 
         * In other words, only the messages received since the datetime specified will be 
         * included in the history.
         * 
         * @param since the since date to use to filter the messages received during that time.
         */
        public void setSince(Date since) {
            this.since = since;
        }

        public String toXML() {
            StringBuffer buf = new StringBuffer();
            buf.append("<history");
            if (getMaxChars() != -1) {
                buf.append(" maxchars=\"").append(getMaxChars()).append("\"");
            }
            if (getMaxStanzas() != -1) {
                buf.append(" maxstanzas=\"").append(getMaxStanzas()).append("\"");
            }
            if (getSeconds() != -1) {
                buf.append(" seconds=\"").append(getSeconds()).append("\"");
            }
            if (getSince() != null) {
                SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                buf.append(" since=\"").append(utcFormat.format(getSince())).append("\"");
            }
            buf.append("/>");
            return buf.toString();
        }
    }
}
