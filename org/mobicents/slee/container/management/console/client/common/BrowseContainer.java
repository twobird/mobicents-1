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

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Stefano Zappaterra
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class BrowseContainer extends Composite {

  private static int MAX_BREADCRUMB_LINKS = 3;

  /**
   * @author Stefano Zappaterra
   * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
   */
  public class BrowseLink extends Hyperlink {

    private BrowseContainer browseContainer;

    private int index;

    private String title;

    @SuppressWarnings("deprecation") // GWT 1.x vs 2.x
    protected BrowseLink(BrowseContainer theBrowseContainer, int theIndex, String title) {
      super(title, title);
      this.browseContainer = theBrowseContainer;
      this.index = theIndex;
      this.title = title;

      this.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          browseContainer.select(index);
        }
      });
    }

    protected void setStyle(boolean isLast) {
      if (!isLast)
        setStyleName("common-BrowseContainer-BrowseLink");
      else
        setStyleName("common-BrowseContainer-BrowseLink-last");
    }

    public String getTitle() {
      return title;
    }
  }

  private boolean showAllLinks = false;

  private ArrayList<Widget> panels = new ArrayList<Widget>();

  private ArrayList<BrowseLink> links = new ArrayList<BrowseLink>();

  private VerticalPanel rootPanel = new VerticalPanel();

  public BrowseContainer() {
    super();
    initWidget(rootPanel);

    setStyleName("common-BrowseContainer");
  }

  public void add(String title, Widget widget) {
    panels.add(widget);
    links.add(new BrowseLink(this, links.size(), title));

    refresh();
  }

  private void refresh() {
    rootPanel.clear();
    refreshLinks();
    refreshTitle();
    refreshPanel();
  }

  private void refreshLinks() {

    if (links.size() <= 1)
      return;

    HorizontalPanel header = new HorizontalPanel();

    header.setStyleName("common-BrowseContainer-links");

    header.setSpacing(5);
    header.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    int firstLink = 0;

    if (!showAllLinks) {
      firstLink = Math.max(0, links.size() - MAX_BREADCRUMB_LINKS);

      if (firstLink > 0) {
        MenuBar hiddenMenu = new MenuBar();
        MenuBar subMenu = new MenuBar(true);

        for (int q = firstLink - 1; q >= 0; q--) {
          final int fq = q;
          Command cmd = new Command() {
            public void execute() {
              select(fq);
            }
          };
          BrowseLink link = (BrowseLink) links.get(q);
          subMenu.addItem(link.getTitle(), cmd);
        }
        MenuItem root = new MenuItem("History", subMenu);
        hiddenMenu.addItem(root);
        /*
         * Hyperlink showAllLink = new Hyperlink("(show hidden)", "(show hidden)"); ClickListener listener = new ClickListener() { public
         * void onClick(Widget source) { showAllLinks = true; refresh(); showAllLinks = false; } }; showAllLink.addClickListener(listener);
         */
        header.add(hiddenMenu);
        header.add(new Image("images/chain.separator.gif"));
      }
    }
    for (int i = firstLink; i <= links.size() - 1; i++) {
      BrowseLink link = links.get(i);
      shortifyAndAddLink(header, link);
      if (i != links.size() - 1) {
        header.add(new Image("images/chain.separator.gif"));
        ((BrowseLink) links.get(i)).setStyle(false);
      }
      else {
        ((BrowseLink) links.get(i)).setStyle(true);
      }
    }
    rootPanel.add(header);
  }

  private void shortifyAndAddLink(HorizontalPanel header, BrowseLink link) {
    // return linkText.length() > 35 ? (linkText.substring(0, 32) + "...") : linkText;
    // EventTypeID[name=net.java.slee.resource.diameter.gq.events.GqSessionTerminationRequest,vendor=java.net,version=0.8]
    String linkText = link.getText();
    String linkTitle = link.getTitle();

    // Have we handled this before ?
    String shortLinkText = linkText;
    if(linkText.contains("[name=")) {
      link.setTitle(linkText);
      // break it apart
      String compType = linkText.substring(0, linkText.indexOf("["));
      String compName = substring(linkText, "[name=", true, ",vendor=", true);
      String compVend = substring(linkText, ",vendor=", true, ",version=", true);
      String compVers = substring(linkText, ",version=", true, "]", true);

      // trim a bit if too big
      if(compName.length() > 50) {
        compName = compName.substring(0, 9) + ".." + compName.substring(compName.length()-29);
      }
      if(compVend.length() > 50) {
        compVend = compVend.substring(0, 9) + ".." + compVend.substring(compVend.length()-29);
      }
      if(compVers.length() > 50) {
        compVers = compVers.substring(0, 9) + ".." + compVers.substring(compVers.length()-29);
      }

      addComponentImage(header, compType);

      shortLinkText = compName + " / " + compVend + " / " + compVers;
      link.setText(shortLinkText);
    }
    else if(linkText.contains("[url=")) {
      link.setTitle(linkText);

      // We know it's DeployableUnitID.. but, this should be future-proof
      String compType = linkText.substring(0, linkText.indexOf("["));

      shortLinkText = linkText.substring(0, linkText.lastIndexOf(".jar"));
      shortLinkText = shortLinkText.substring(shortLinkText.lastIndexOf("/") + 1, shortLinkText.length());
      if(shortLinkText.length() > 50) {
        shortLinkText = shortLinkText.substring(0, 19) + ".." + shortLinkText.substring(shortLinkText.length()-19);
      }
      link.setText(shortLinkText);

      addComponentImage(header, compType);
    }
    else if(linkTitle.contains("[name=") || linkTitle.contains("[url=")) {
      String compType = linkTitle.substring(0, linkTitle.indexOf("["));
      addComponentImage(header, compType);
    }

    header.add(link);
  }

  private void addComponentImage(HorizontalPanel header, String componentType) {
    if(componentType.equals("EventTypeID")) {
      header.add(new Image("images/components.event type.gif"));
    }
    else if(componentType.equals("LibraryID")) {
      header.add(new Image("images/components.library.gif"));
    }
    else if(componentType.equals("ResourceAdaptorID")) {
      header.add(new Image("images/components.resource adaptor.gif"));
    }
    else if(componentType.equals("ResourceAdaptorTypeID")) {
      header.add(new Image("images/components.resource adaptor type.gif"));
    }
    else if(componentType.equals("ProfileSpecificationID")) {
      header.add(new Image("images/components.profile specification.gif"));
    }
    else if(componentType.equals("SbbID")) {
      header.add(new Image("images/components.sbb.gif"));
    }
    else if(componentType.equals("ServiceID")) {
      header.add(new Image("images/components.service.gif"));
    }
    else if(componentType.equals("DeployableUnitID")) {
      header.add(new Image("images/deployableunits.deployableunit.gif"));
    }
  }

  private String substring(String original, String beginString, boolean excludeBegin, String endString, boolean excludeEnd) {
    return original.substring(original.indexOf(beginString) + (excludeBegin ? beginString.length() : 0), original.indexOf(endString) + (excludeEnd ? 0 : endString.length()));
  }

  private void refreshTitle() {
    if (links.size() > 0) {
      Label title = new Label(((BrowseLink) links.get(links.size() - 1)).getTitle());
      title.setStyleName("common-BrowseContainer-title");
      rootPanel.add(title);
    }
  }

  private void refreshPanel() {
    if (panels.size() > 0 && panels.get(panels.size() - 1) != null)
      rootPanel.add((Widget) panels.get(panels.size() - 1));
  }

  protected void select(int index) {

    if (index >= panels.size())
      return;

    while (index + 1 < panels.size()) {
      panels.remove(index + 1);
      links.remove(index + 1);
    }

    refresh();
  }

  public void back() {
    if (panels.size() <= 1)
      return;

    select(panels.size() - 2);
  }

  public void empty() {
    panels.clear();
    links.clear();
    refresh();
  }

  public void setTitle(Widget widget, String title) {
    for (int i = 0; i < panels.size(); i++) {
      if (((Widget) panels.get(i)) == widget) {
        ((BrowseLink) links.get(i)).setText(title);
        refresh();
        return;
      }
    }
  }
}
