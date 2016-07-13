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

package org.mobicents.slee.container.management.console.client.deployableunits;

import org.mobicents.slee.container.management.console.client.common.BrowseContainer;
import org.mobicents.slee.container.management.console.client.common.ControlContainer;
import org.mobicents.slee.container.management.console.client.common.PropertiesPanel;
import org.mobicents.slee.container.management.console.client.components.ComponentNameLabel;

import com.google.gwt.user.client.ui.Composite;

/**
 * 
 * @author Stefano Zappaterra
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class DeployableUnitPanel extends Composite {

  private ControlContainer rootPanel = new ControlContainer();

  private DeployableUnitInfo deployableUnitInfo;

  private BrowseContainer browseContainer;

  public DeployableUnitPanel(BrowseContainer browseContainer, DeployableUnitInfo deployableUnitInfo) {
    super();
    this.browseContainer = browseContainer;
    this.deployableUnitInfo = deployableUnitInfo;

    initWidget(rootPanel);

    setData();
  }

  private void setData() {
    PropertiesPanel propertiesPanel = new PropertiesPanel();

    propertiesPanel.add("Name", deployableUnitInfo.getName());
    propertiesPanel.add("ID", deployableUnitInfo.getID());
    propertiesPanel.add("Date", deployableUnitInfo.getDeploymentDate().toString());
    propertiesPanel.add("URL", deployableUnitInfo.getURL());
    propertiesPanel.add("Components", ComponentNameLabel.toArray(deployableUnitInfo.getComponents(), browseContainer));

    rootPanel.setWidget(0, 0, propertiesPanel);
  }
}
