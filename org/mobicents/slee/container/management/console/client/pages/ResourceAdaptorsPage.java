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

package org.mobicents.slee.container.management.console.client.pages;

import org.mobicents.slee.container.management.console.client.common.CardControl;
import org.mobicents.slee.container.management.console.client.common.SmartTabPage;
import org.mobicents.slee.container.management.console.client.resources.ResourceAdaptorsCard;
import org.mobicents.slee.container.management.console.client.usage.RaUsageCard;

public class ResourceAdaptorsPage extends SmartTabPage {

  private CardControl cardControl = new CardControl();

  private ResourceAdaptorsCard resourceAdaptorsCard;

  private RaUsageCard usageCard;

  public ResourceAdaptorsPage() {
    initWidget(cardControl);
  }

  public static SmartTabPageInfo getInfo() {
    return new SmartTabPageInfo("<image src='images/resources.gif' /> Resources", "Resource Adaptors") {
      protected SmartTabPage createInstance() {
        return new ResourceAdaptorsPage();
      }
    };
  }

  public void onInit() {
    resourceAdaptorsCard = new ResourceAdaptorsCard();
    usageCard = new RaUsageCard();

    cardControl.onInit();
    cardControl.add(resourceAdaptorsCard, "<image align='absbottom' src='images/resources.gif' /> Resource Adaptors", true);
    cardControl.add(usageCard, "<image align='absbottom' src='images/usage.gif' /> Usage Parameters", true);

    cardControl.setWidth("100%");
    cardControl.selectTab(0);
  }

  public void onHide() {
    cardControl.onHide();
  }

  public void onShow() {
    cardControl.onShow();
  }
}
