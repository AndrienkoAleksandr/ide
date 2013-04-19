/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package com.codenvy.ide.wizard.newfolder;

import com.codenvy.ide.api.mvp.View;

/**
 * Interface of NewFolder view
 *
 * @author <a href="mailto:aplotnikov@exoplatform.com">Andrey Plotnikov</a>
 */
public interface NewFolderPageView extends View<NewFolderPageView.ActionDelegate> {
    /**
     * Returns folder name.
     *
     * @return
     */
    String getFolderName();

    /**
     * Set Folder name
     *
     * @param name
     */
    void setFolderName(String name);

    /** Needs for delegate some function into NewFolder view. */
    public interface ActionDelegate {
        /** Checks entered information(folder name and etc). */
        void checkEnteredInformation();
    }
}