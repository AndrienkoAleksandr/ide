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
package org.exoplatform.ide.client.navigation.template;

import org.exoplatform.ide.client.framework.template.FileTemplate;

/**
 * Callback to return from {@link CreateFileFromTemplateView}, when submit button was clicked.
 * <p/>
 * Used to return from form, when create project template and need to add file template to project.
 *
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: CreateFileFromTemplateCallback.java Aug 10, 2011 5:23:30 PM vereshchaka $
 */
public interface CreateFileFromTemplateCallback {
    void onSubmit(FileTemplate fileTemplate);
}
