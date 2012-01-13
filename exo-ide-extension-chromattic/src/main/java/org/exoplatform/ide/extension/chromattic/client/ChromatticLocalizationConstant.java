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
package org.exoplatform.ide.extension.chromattic.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author <a href="dnochevnov@gmail.com">Dmytro Nochevnov</a>
 * @version $Id:
 */
public interface ChromatticLocalizationConstant extends Messages
{

   /*
    * Views and Forms
    */
   @Key("view.title.deployNodeType")
   String deployNodeTypeViewTitle();

   @Key("view.title.generateNodeType")
   String generateNodeTypeViewTitle();

   @Key("form.title.generateNodeTypePreview")
   String generateNodeTypePreviewFormTitle();

   /*
    * Fields
    */
   @Key("field.nodeTypeFormat")
   String nodeTypeFormatField();

   @Key("field.whatToDoIfNodeExists")
   String whatToDoIfNodeExistsField();

   /*
    * Buttons
    */
   @Key("button.deploy")
   String deployButton();

   @Key("button.cancel")
   String cancelButton();

   @Key("button.generate")
   String generateButton();

   /*
    * Controls
    */
   @Key("control.title.previewNodeType")
   String previewNodeTypeControlTitle();

   @Key("control.title.deployNodeType")
   String deployNodeTypeControlTitle();
}
