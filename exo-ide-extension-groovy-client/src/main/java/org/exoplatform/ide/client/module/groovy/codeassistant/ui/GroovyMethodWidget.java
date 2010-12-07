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
package org.exoplatform.ide.client.module.groovy.codeassistant.ui;

import java.util.HashMap;

import org.exoplatform.ide.client.framework.codeassistant.ModifierHelper;
import org.exoplatform.ide.client.framework.codeassistant.TokenExt;
import org.exoplatform.ide.client.framework.codeassistant.TokenExtProperties;
import org.exoplatform.ide.client.framework.codeassistant.TokenExtType;
import org.exoplatform.ide.client.module.groovy.GroovyPluginImageBundle;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: Nov 29, 2010 10:06:35 AM evgen $
 *
 */
public class GroovyMethodWidget extends GroovyTokenWidgetBase
{

   private Grid grid;

   /**
    * @param token
    */
   public GroovyMethodWidget(TokenExt token)
   {
      super(token);
      grid = new Grid(1, 3);
      grid.setStyleName(GroovyPluginImageBundle.INSTANCE.css().item());
//      grid.setWidth("100%");
      
      Image i = getImage();
      i.setHeight("16px");
      grid.setWidget(0, 0, i);

      String name = token.getName() + token.getProperty(TokenExtProperties.PARAMETERTYPES);
      name += ":" + token.getProperty(TokenExtProperties.RETURNTYPE);
      
      Label nameLabel = new Label(name, false);
      nameLabel.getElement().setInnerHTML(getModifiers() + nameLabel.getElement().getInnerHTML());
      grid.setWidget(0, 1, nameLabel);

      String pack = token.getProperty(TokenExtProperties.DECLARINGCLASS);
      Label label = new Label("-" + pack, false);
      label.setStyleName(GroovyPluginImageBundle.INSTANCE.css().fqnStyle());
      grid.setWidget(0, 2, label);

      grid.getCellFormatter().setWidth(0, 0, "16px");
      grid.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
      grid.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);
      grid.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_LEFT);
      grid.getCellFormatter().setWidth(0, 2, "100%");

      initWidget(grid);
//      setWidth("100%");
   }

   private Image getImage()
   {
      Image i;
      if(ModifierHelper.isPrivate(modifieres))
      {
        i = new Image(GroovyPluginImageBundle.INSTANCE.publicMethod());
      }
      else if(ModifierHelper.isProtected(modifieres))
      {
         i = new Image(GroovyPluginImageBundle.INSTANCE.protectedMethod());
      }
      else if(ModifierHelper.isPublic(modifieres))
      {
         i = new Image(GroovyPluginImageBundle.INSTANCE.publicMethod());           
      }
      else
      {
         i = new Image(GroovyPluginImageBundle.INSTANCE.defaultMethod());
      }
      return i;
   }

   /**
    * @see org.exoplatform.ide.client.framework.codeassistant.TokenWidget#getTokenValue()
    */
   @Override
   public String getTokenValue()
   {
      return token.getName() + token.getProperty(TokenExtProperties.PARAMETERTYPES);
   }

}
