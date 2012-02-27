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

package org.exoplatform.ide.client.marking;

import org.exoplatform.gwtframework.ui.client.command.SimpleControl;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.framework.control.IDEControl;

/**
 * 
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class MarkErrorControl extends SimpleControl implements IDEControl
{
   
   public MarkErrorControl() {
      super("Help/Debug/Mark Error");
      setTitle("Mark Error");
      setPrompt("Mark Error");
      setImages(IDEImageBundle.INSTANCE.add(), IDEImageBundle.INSTANCE.addDisabled());
      setEvent(new MarkProblemEvent(MarkProblemEvent.ProblemType.ERROR));
      
      setEnabled(true);
      setVisible(true);
   }

   @Override
   public void initialize()
   {
   }

}
