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
package org.exoplatform.ide.extension.samples.client.inviting.github;

import org.exoplatform.gwtframework.ui.client.command.SimpleControl;
import org.exoplatform.ide.client.framework.annotation.RolesAllowed;
import org.exoplatform.ide.client.framework.control.IDEControl;
import org.exoplatform.ide.extension.samples.client.SamplesClientBundle;

/**
 * @deprecated class uses only for testing
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Guluy</a>
 * @version $
 * 
 */
@RolesAllowed({"administrators", "developers"})
public class InviteGitHubCollaboratorsControl extends SimpleControl implements IDEControl
{

   private static final String ID = "Share/Invite GitHub Collaborators...";

   private static final String TITLE = "Invite GitHub Collaborators...";

   private static final String PROMPT = "Invite GitHub Collaborators...";

   public InviteGitHubCollaboratorsControl()
   {
      super(ID);
      setTitle(TITLE);
      setPrompt(PROMPT);
      setImages(SamplesClientBundle.INSTANCE.welcome(), SamplesClientBundle.INSTANCE.welcomeDisabled());
      setEvent(new InviteGitHubCollaboratorsEvent());
      setHotKey("F2");
      setEnabled(true);
      setVisible(true);
   }

   /**
    * @see org.exoplatform.ide.client.framework.control.IDEControl#initialize()
    */
   @Override
   public void initialize()
   {
   }

}