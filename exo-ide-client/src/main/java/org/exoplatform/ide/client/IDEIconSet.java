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
package org.exoplatform.ide.client;

import org.exoplatform.gwtframework.ui.client.util.ImageFactory;

/**
 * Created by The eXo Platform SAS .
 *
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class IDEIconSet {

    public static void init() {
        ImageFactory.addImage("ok", IDEImageBundle.INSTANCE.ok(), IDEImageBundle.INSTANCE.okDisabled());
        ImageFactory.addImage("cancel", IDEImageBundle.INSTANCE.cancel(), IDEImageBundle.INSTANCE.cancelDisabled());
        ImageFactory.addImage("yes", IDEImageBundle.INSTANCE.ok(), IDEImageBundle.INSTANCE.okDisabled());
        ImageFactory.addImage("no", IDEImageBundle.INSTANCE.cancel(), IDEImageBundle.INSTANCE.cancelDisabled());
        ImageFactory.addImage("search", IDEImageBundle.INSTANCE.search(), IDEImageBundle.INSTANCE.searchDisabled());
        ImageFactory.addImage("delete", IDEImageBundle.INSTANCE.delete(), IDEImageBundle.INSTANCE.deleteDisabled());
        ImageFactory.addImage("properties", IDEImageBundle.INSTANCE.properties(),
                              IDEImageBundle.INSTANCE.propertiesDisabled());

        ImageFactory.addImage("add", IDEImageBundle.INSTANCE.add(), IDEImageBundle.INSTANCE.addDisabled());
        ImageFactory.addImage("remove", IDEImageBundle.INSTANCE.remove(), IDEImageBundle.INSTANCE.removeDisabled());
        ImageFactory.addImage("up", IDEImageBundle.INSTANCE.up(), IDEImageBundle.INSTANCE.upDisabled());
        ImageFactory.addImage("down", IDEImageBundle.INSTANCE.down(), IDEImageBundle.INSTANCE.downDisabled());
        ImageFactory.addImage("defaults", IDEImageBundle.INSTANCE.defaults(), IDEImageBundle.INSTANCE.defaultsDisabled());
        ImageFactory.addImage("hide", IDEImageBundle.INSTANCE.hide(), IDEImageBundle.INSTANCE.hideDisabled());
        ImageFactory.addImage("next", IDEImageBundle.INSTANCE.next(), IDEImageBundle.INSTANCE.nextDisabled());
        ImageFactory.addImage("back", IDEImageBundle.INSTANCE.back(), IDEImageBundle.INSTANCE.backDisabled());

        ImageFactory.addImage("upload", IDEImageBundle.INSTANCE.upload(), IDEImageBundle.INSTANCE.uploadDisabled());
        ImageFactory.addImage("edit", IDEImageBundle.INSTANCE.edit(), IDEImageBundle.INSTANCE.editDisabled());

        ImageFactory.addImage("link-with-editor", IDEImageBundle.INSTANCE.linkWithEditor(),
                              IDEImageBundle.INSTANCE.linkWithEditorDisabled());
    }

}
