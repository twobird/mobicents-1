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

import org.jivesoftware.smack.packet.IQ;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

/**
 * A Time IQ packet, which is used by XMPP clients to exchange their respective local
 * times. Clients that wish to fully support the entitity time protocol should register
 * a PacketListener for incoming time requests that then respond with the local time.
 * This class can be used to request the time from other clients, such as in the
 * following code snippet:
 *
 * <pre>
 * // Request the time from a remote user.
 * Time timeRequest = new Time();
 * timeRequest.setType(IQ.Type.GET);
 * timeRequest.setTo(someUser@example.com);
 *
 * // Create a packet collector to listen for a response.
 * PacketCollector collector = con.createPacketCollector(
 *                new PacketIDFilter(timeRequest.getPacketID()));
 *
 * con.sendPacket(timeRequest);
 *
 * // Wait up to 5 seconds for a result.
 * IQ result = (IQ)collector.nextResult(5000);
 * if (result != null && result.getType() == IQ.Type.RESULT) {
 *     Time timeResult = (Time)result;
 *     // Do something with result...
 * }</pre><p>
 *
 * Warning: this is an non-standard protocol documented by
 * <a href="http://www.jabber.org/jeps/jep-0090.html">JEP-90</a>. Because this is a
 * non-standard protocol, it is subject to change.
 *
 * @author Matt Tucker
 */
public class Time extends IQ {

    private static SimpleDateFormat utcFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
    private static DateFormat displayFormat = DateFormat.getDateTimeInstance();

    private String utc = null;
    private String tz = null;
    private String display = null;

    /**
     * Creates a new Time instance with empty values for all fields.
     */
    public Time() {
        this(Calendar.getInstance());
    }

    /**
     * Creates a new Time instance using the specified calendar instance as
     * the time value to send.
     *
     * @param cal the time value.
     */
    public Time(Calendar cal) {
        TimeZone timeZone = cal.getTimeZone();
        tz = cal.getTimeZone().getID();
        display = displayFormat.format(cal.getTime());
        // Convert local time to the UTC time.
        utc = utcFormat.format(new Date(
                cal.getTimeInMillis() - timeZone.getOffset(cal.getTimeInMillis())));
    }

    /**
     * Returns the local time or <tt>null</tt> if the time hasn't been set.
     *
     * @return the lcocal time.
     */
    public Date getTime() {
        if (utc == null) {
            return null;
        }
        Date date = null;
        try {
            Calendar cal = Calendar.getInstance();
            // Convert the UTC time to local time.
            cal.setTime(new Date(utcFormat.parse(utc).getTime() +
                    cal.getTimeZone().getOffset(cal.getTimeInMillis())));
            date = cal.getTime();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * Sets the time using the local time.
     *
     * @param time the current local time.
     */
    public void setTime(Date time) {
        // Convert local time to UTC time.
        utc = utcFormat.format(new Date(
                time.getTime() - TimeZone.getDefault().getOffset(time.getTime())));
    }

    /**
     * Returns the time as a UTC formatted String using the format CCYYMMDDThh:mm:ss.
     *
     * @return the time as a UTC formatted String.
     */
    public String getUtc() {
        return utc;
    }

    /**
     * Sets the time using UTC formatted String in the format CCYYMMDDThh:mm:ss.
     *
     * @param utc the time using a formatted String.
     */
    public void setUtc(String utc) {
        this.utc = utc;

    }

    /**
     * Returns the time zone.
     *
     * @return the time zone.
     */
    public String getTz() {
        return tz;
    }

    /**
     * Sets the time zone.
     *
     * @param tz the time zone.
     */
    public void setTz(String tz) {
        this.tz = tz;
    }

    /**
     * Returns the local (non-utc) time in human-friendly format.
     *
     * @return the local time in human-friendly format.
     */
    public String getDisplay() {
        return display;
    }

    /**
     * Sets the local time in human-friendly format.
     *
     * @param display the local time in human-friendly format.
     */
    public void setDisplay(String display) {
        this.display = display;
    }

    public String getChildElementXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<query xmlns=\"jabber:iq:time\">");
        if (utc != null) {
            buf.append("<utc>").append(utc).append("</utc>");
        }
        if (tz != null) {
            buf.append("<tz>").append(tz).append("</tz>");
        }
        if (display != null) {
            buf.append("<display>").append(display).append("</display>");
        }
        buf.append("</query>");
        return buf.toString();
    }
}