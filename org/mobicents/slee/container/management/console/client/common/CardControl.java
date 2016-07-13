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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * @author Stefano Zappaterra
 * 
 */
public class CardControl extends Composite implements TabListener, CommonControl {

  private boolean firstTabSelectionEvent = true;

  private TabPanel tabPanel = new TabPanel();

  private Card selectedCard = null;

  public CardControl() {
    super();
    tabPanel.addTabListener(this);
    initWidget(tabPanel);
  }

  public void add(Card card, String tabText, boolean asHTML) {
    tabPanel.add(card, tabText, asHTML);
  }

  public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
    Card newSelectedCard = (Card) tabPanel.getWidget(tabIndex);
    if (newSelectedCard == null)
      return;

    if (newSelectedCard == selectedCard)
      return;

    if (selectedCard != null)
      selectedCard.onUnselect();

    newSelectedCard.onSelect();

    if (firstTabSelectionEvent)
      firstTabSelectionEvent = false;
    else
      newSelectedCard.onShow();

    selectedCard = newSelectedCard;
  }

  public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
    return true;
  }

  public void selectTab(int index) {
    tabPanel.selectTab(index);
  }

  public void onHide() {
    if (selectedCard != null)
      selectedCard.onHide();
  }

  public void onInit() {
  }

  public void onShow() {
    if (selectedCard != null)
      selectedCard.onShow();
  }
}
