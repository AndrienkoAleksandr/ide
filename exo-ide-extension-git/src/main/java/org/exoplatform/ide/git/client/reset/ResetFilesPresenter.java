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
package org.exoplatform.ide.git.client.reset;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.RequestException;

import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.commons.rest.AutoBeanUnmarshaller;
import org.exoplatform.gwtframework.ui.client.component.ListGrid;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.framework.event.RefreshBrowserEvent;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage.Type;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.git.client.GitClientService;
import org.exoplatform.ide.git.client.GitExtension;
import org.exoplatform.ide.git.client.GitPresenter;
import org.exoplatform.ide.git.shared.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter for view for reseting files from index. The view must be pointed in Views.gwt.xml file.
 * 
 * When user tries to reset files from index: 1. Find Git work directory by selected item in browser tree. 2. Get status for found
 * work directory. 3. Display files ready for commit in grid. (Checked items will be reseted from index).
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Apr 13, 2011 4:52:42 PM anya $
 * 
 */
public class ResetFilesPresenter extends GitPresenter implements ResetFilesHandler
{

   interface Display extends IsView
   {
      /**
       * Get reset files button click handler.
       * 
       * @return {@link HasClickHandlers} click handler
       */
      HasClickHandlers getResetButton();

      /**
       * Get cancel button click handler.
       * 
       * @return {@link HasClickHandlers} click handler
       */
      HasClickHandlers getCancelButton();

      /**
       * Get grid with Git files.
       * 
       * @return {@link ListGrid}
       */
      ListGrid<IndexFile> getIndexFilesGrid();
   }

   /**
    * Presenter' display.
    */
   private Display display;

   /**
    * @param eventBus events handler
    */
   public ResetFilesPresenter()
   {
      IDE.addHandler(ResetFilesEvent.TYPE, this);
   }

   /**
    * Bind pointed display with presenter.
    * 
    * @param d display
    */
   public void bindDisplay(Display d)
   {
      this.display = d;

      display.getResetButton().addClickHandler(new ClickHandler()
      {

         @Override
         public void onClick(ClickEvent event)
         {
            doReset();
         }
      });

      display.getCancelButton().addClickHandler(new ClickHandler()
      {

         @Override
         public void onClick(ClickEvent event)
         {
            IDE.getInstance().closeView(display.asView().getId());
         }
      });
   }

   /**
    * @see org.exoplatform.ide.git.client.reset.ResetFilesHandler#onResetFiles(org.exoplatform.ide.git.client.reset.ResetFilesEvent)
    */
   @Override
   public void onResetFiles(ResetFilesEvent event)
   {
      if (makeSelectionCheck())
      {
         getStatus(getSelectedProject().getId());
      }
   }

   /**
    * Get the information about git files' states.
    * 
    * @param workDir
    */
   private void getStatus(final String projectId)
   {
      try
      {
         GitClientService.getInstance().status(vfs.getId(), projectId,
            new AsyncRequestCallback<Status>(new AutoBeanUnmarshaller<Status>(GitExtension.AUTO_BEAN_FACTORY.status()))
            {

               @Override
               protected void onSuccess(Status result)
               {

                  if (result.isClean())
                  {
                     Dialogs.getInstance().showInfo(GitExtension.MESSAGES.nothingChanged());
                     return;
                  }

                  Display d = GWT.create(Display.class);
                  IDE.getInstance().openView(d.asView());
                  bindDisplay(d);

                  List<IndexFile> values = new ArrayList<IndexFile>();
                  List<String> valuesTmp = new ArrayList<String>();

                  valuesTmp.addAll(result.getAdded());
                  valuesTmp.addAll(result.getChanged());
                  valuesTmp.addAll(result.getRemoved());

                  for (String file : valuesTmp)
                  {
                     values.add(new IndexFile(file, true));
                  }

                  display.getIndexFilesGrid().setValue(values);
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  String errorMassage =
                     (exception.getMessage() != null) ? exception.getMessage() : GitExtension.MESSAGES.statusFailed();
                  IDE.fireEvent(new OutputEvent(errorMassage, Type.GIT));
               }
            });
      }
      catch (RequestException e)
      {
         String errorMassage = (e.getMessage() != null) ? e.getMessage() : GitExtension.MESSAGES.statusFailed();
         IDE.fireEvent(new OutputEvent(errorMassage, Type.GIT));
      }
   }

   /**
    * Reset checked files from index.
    */
   private void doReset()
   {
      List<String> files = new ArrayList<String>();
      for (IndexFile file : display.getIndexFilesGrid().getValue())
      {
         if (!file.isIndexed())
         {
            files.add(file.getPath());
         }
      }

      String projectId = getSelectedProject().getId();
      try
      {
         GitClientService.getInstance().reset(vfs.getId(), projectId, files.toArray(new String[files.size()]), "HEAD",
            null, new AsyncRequestCallback<String>()
            {

               @Override
               protected void onSuccess(String result)
               {
                  IDE.getInstance().closeView(display.asView().getId());
                  IDE.fireEvent(new OutputEvent(GitExtension.MESSAGES.resetFilesSuccessfully(), Type.GIT));
                  IDE.fireEvent(new RefreshBrowserEvent());
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  String errorMassage =
                     (exception.getMessage() != null) ? exception.getMessage() : GitExtension.MESSAGES
                        .resetFilesFailed();
                  IDE.fireEvent(new OutputEvent(errorMassage, Type.GIT));
               }
            });
      }
      catch (RequestException e)
      {
         String errorMassage = (e.getMessage() != null) ? e.getMessage() : GitExtension.MESSAGES.resetFilesFailed();
         IDE.fireEvent(new OutputEvent(errorMassage, Type.GIT));
      }
   }
}
