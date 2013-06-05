/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.ide.extension.samples.client.startpage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import org.exoplatform.ide.client.framework.ui.impl.ViewImpl;
import org.exoplatform.ide.client.framework.util.Utils;
import org.exoplatform.ide.extension.samples.client.SamplesClientBundle;
import org.exoplatform.ide.extension.samples.client.SamplesExtension;

/**
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: WelcomeView.java Aug 25, 2011 12:33:32 PM vereshchaka $
 */
public class StartPageView extends ViewImpl implements StartPagePresenter.Display {

    private static final String ID = "WelcomeViewId";

    private static final String TITLE = SamplesExtension.LOCALIZATION_CONSTANT.welcomeTitle();

    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, StartPageView> {
    }

    @UiField
    Anchor invitationsLink;

    @UiField
    Anchor cloneLink;

    @UiField
    Anchor projectLink;

    @UiField
    Anchor importLink;

    @UiField
    Anchor documentationLink;

    @UiField
    Anchor supportLink;
    
    @UiField
    Frame fbFrame;

    @UiField
    Image logo;

    public StartPageView() {
        super(ID, "editor", TITLE, new Image(SamplesClientBundle.INSTANCE.welcome()));
        add(uiBinder.createAndBindUi(this));
        fbFrame.setUrl(UriUtils.fromString("/" + Utils.getWorkspaceName() +"/ide/fblike.html"));
    }

    /** @see org.exoplatform.ide.client.StartPagePresenter.WelcomePresenter.Display#getCloneLink() */
    @Override
    public HasClickHandlers getCloneLink() {
        return cloneLink;
    }

    /** @see org.exoplatform.ide.extension.samples.client.startpage.StartPagePresenter.Display#getProjectLink() */
    @Override
    public HasClickHandlers getProjectLink() {
        return projectLink;
    }

    /** @see org.exoplatform.ide.extension.samples.client.startpage.StartPagePresenter.Display#getImportLink() */
    @Override
    public HasClickHandlers getImportLink() {
        return importLink;
    }

    @Override
    public HasClickHandlers getInvitationsLink() {
        return invitationsLink;
    }

}
