/*
 * JBoss, Home of Professional Open Source
 * Copyright 2003-2011, Red Hat, Inc. and individual contributors
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

package org.mobicents.slee.container.management.console.client.common;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class LogPanel extends VerticalPanel {

  private StringBuffer stringBuffer = new StringBuffer();

  private HTML logHTML = new HTML();

  private ScrollPanel scrollPanel = new ScrollPanel(logHTML);

  private HorizontalPanel header = new HorizontalPanel();

  @SuppressWarnings("deprecation") // GWT 1.x vs 2.x
  public LogPanel() {

    logHTML.setStyleName("common-LogPanel-text");

    Hyperlink clean = new Hyperlink("Clear", "Clear");
    clean.setStyleName("common-LogPanel-header");
    clean.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        onClear();
      }
    });

    HTML padding = new HTML();

    header.add(padding);
    header.add(clean);

    header.setStyleName("common-LogPanel-header");
    header.setCellWidth(padding, "100%");

    padding.setHTML("&nbsp;<img src='images/log.mgmt.1.jpg' align='absbottom' /> Mobicents Management Console Log");

    scrollPanel.setStyleName("common-LogPanel-area");

    add(header);
    add(scrollPanel);

    setStyleName("common-LogPanel");
    setCellHeight(scrollPanel, "100%");
  }

  public void info(String s) {
    println("[INFO] " + s, "#333333", "images/log.info.gif");
  }

  public void warning(String s) {
    println("[WARNING] " + s, "#EE8811", "images/log.warning.gif");
  }

  public void error(String s) {
    println("[ERROR] " + s, "##AA0000", "images/log.error.gif");
  }

  private void println(String s, String color, String image) {
    stringBuffer.append("<image align='absbottom' src='" + image + "'/><font color='" + color + "'>");
    stringBuffer.append(DateTimeFormat.getFormat("HH:mm:ss:SSS ").format(new Date()));
    stringBuffer.append(s);
    stringBuffer.append("</font><br>");
    logHTML.setHTML(stringBuffer.toString());
    scrollPanel.scrollToBottom();
  }

  private void onClear() {
    stringBuffer = new StringBuffer();
    logHTML.setHTML("");
    scrollPanel.scrollToTop();
  }

}
