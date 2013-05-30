/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.ide.extension.gadget.client;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.autobean.shared.AutoBean;

import org.exoplatform.ide.client.framework.application.event.InitializeServicesEvent;
import org.exoplatform.ide.client.framework.application.event.InitializeServicesHandler;
import org.exoplatform.ide.client.framework.control.Docking;
import org.exoplatform.ide.client.framework.module.Extension;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.extension.gadget.client.controls.ShowGadgetPreviewControl;
import org.exoplatform.ide.extension.gadget.client.service.GadgetService;

/**
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: $
 */
public class GadgetExtension extends Extension implements InitializeServicesHandler {
    /** The generator of an {@link AutoBean}. */
    public static final GadgetAutoBeanFactory AUTO_BEAN_FACTORY = GWT.create(GadgetAutoBeanFactory.class);

    /** @see org.exoplatform.ide.client.framework.module.Extension#initialize(com.google.gwt.event.shared.HandlerManager) */
    @Override
    public void initialize() {
        IDE.getInstance().addControl(new ShowGadgetPreviewControl(), Docking.TOOLBAR_RIGHT);
        new GadgetPluginEventHandler();
        IDE.addHandler(InitializeServicesEvent.TYPE, this);
    }

    public void onInitializeServices(InitializeServicesEvent event) {
      
    }

}
