/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ide.java.client.perspective;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.exoplatform.ide.java.client.stackview.StackViewPartPresenter;
import org.exoplatform.ide.outline.OutlinePartPrenter;
import org.exoplatform.ide.part.EditorPartStackPresenter;
import org.exoplatform.ide.part.PartStackPresenter;
import org.exoplatform.ide.part.console.ConsolePartPresenter;
import org.exoplatform.ide.part.projectexplorer.ProjectExplorerPartPresenter;
import org.exoplatform.ide.perspective.PerspectivePresenter;

/**
 * Java Debug Perspective Demo
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a> 
 */
@Singleton
public class DebugPerspectivePresenter extends PerspectivePresenter
{
   /**
    * Setups and initializes Java Debug Perspective
    * 
    * @param view
    * @param editorPartStackPresenter
    * @param partStackProvider
    * @param outlinePresenter
    * @param consolePartPresenter
    * @param stackViewPartPresenter
    */
   @Inject
   public DebugPerspectivePresenter(DebugPerspectiveView view, EditorPartStackPresenter editorPartStackPresenter,
      Provider<PartStackPresenter> partStackProvider, OutlinePartPrenter outlinePresenter,
      ConsolePartPresenter consolePartPresenter, StackViewPartPresenter stackViewPartPresenter,
      ProjectExplorerPartPresenter projectExplorerPart)
   {
      super(view, editorPartStackPresenter, partStackProvider);
      // add Parts
      openPart(projectExplorerPart, PartStackType.NAVIGATION);
      openPart(stackViewPartPresenter, PartStackType.NAVIGATION);
      openPart(outlinePresenter, PartStackType.TOOLING);
      openPart(consolePartPresenter, PartStackType.INFORMATION);
   }
}