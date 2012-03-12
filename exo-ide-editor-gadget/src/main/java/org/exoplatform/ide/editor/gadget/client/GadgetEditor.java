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
package org.exoplatform.ide.editor.gadget.client;

import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.client.framework.control.NewItemControl;
import org.exoplatform.ide.client.framework.module.Extension;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.editor.ckeditor.CKEditorConfiguration;
import org.exoplatform.ide.editor.ckeditor.CKEditorProducer;
import org.exoplatform.ide.editor.codemirror.CodeMirrorConfiguration;
import org.exoplatform.ide.editor.codemirror.CodeMirrorProducer;
import org.exoplatform.ide.editor.gadget.client.codemirror.GoogleGadgetParser;
import org.exoplatform.ide.editor.html.client.codeassistant.HtmlCodeAssistant;
import org.exoplatform.ide.editor.html.client.codemirror.HtmlAutocompleteHelper;
import org.exoplatform.ide.editor.html.client.codemirror.HtmlOutlineItemCreator;

/**
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: GadgetEditor Mar 10, 2011 11:10:28 AM evgen $
 *
 */
public class GadgetEditor extends Extension
{

   /**
    * @see org.exoplatform.ide.client.framework.module.Extension#initialize()
    */
   @Override
   public void initialize()
   {
      CodeMirrorProducer producer = new CodeMirrorProducer(
         MimeType.GOOGLE_GADGET,
         "CodeMirror OpenSocial Gadget editor",
         "gadget",
         Images.INSTANCE.gadgetImage(),
         true,
         
         new CodeMirrorConfiguration().
            setGenericParsers("['parsegadgetxml.js', 'parsecss.js', 'tokenizejavascript.js', 'parsejavascript.js', 'parsehtmlmixed.js']").
            setGenericStyles("['" + CodeMirrorConfiguration.PATH + "css/xmlcolors.css', '" + CodeMirrorConfiguration.PATH
               + "css/jscolors.css', '" + CodeMirrorConfiguration.PATH + "css/csscolors.css']").
            setParser(new GoogleGadgetParser()).
            setCanBeOutlined(true).
            setAutocompleteHelper(new HtmlAutocompleteHelper()).
            setCodeAssistant(new HtmlCodeAssistant()).
            setCanHaveSeveralMimeTypes(true)
      );
      
      IDE.getInstance().addEditor(new CKEditorProducer(MimeType.GOOGLE_GADGET, "CKEditor OpenSocial Gadget editor", "gadget", Images.INSTANCE.gadgetImage(), false,
         new CKEditorConfiguration()));
      
      IDE.getInstance().addEditor(producer);

      IDE.getInstance().addControl(new NewItemControl("File/New/New OpenSocial Gadget", "OpenSocial Gadget",
         "Create New OpenSocial Gadget", Images.GOOGLE_GADGET, MimeType.GOOGLE_GADGET));
      
      IDE.getInstance().addOutlineItemCreator(MimeType.GOOGLE_GADGET, new HtmlOutlineItemCreator());
   }

}
