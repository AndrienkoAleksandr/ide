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
package org.exoplatform.ide.extension.groovy.client.handlers;

import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.exception.ServerException;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage;
import org.exoplatform.ide.extension.groovy.client.event.UndeployGroovyScriptEvent;
import org.exoplatform.ide.extension.groovy.client.event.UndeployGroovyScriptHandler;
import org.exoplatform.ide.extension.groovy.client.event.UndeployGroovyScriptSandboxEvent;
import org.exoplatform.ide.extension.groovy.client.event.UndeployGroovyScriptSandboxHandler;
import org.exoplatform.ide.extension.groovy.client.service.groovy.GroovyService;
import org.exoplatform.ide.extension.groovy.client.service.groovy.event.GroovyUndeployResultReceivedEvent;
import org.exoplatform.ide.extension.groovy.client.service.groovy.event.GroovyUndeployResultReceivedHandler;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.model.FileModel;

/**
 * 
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class UndeployGroovyCommandHandler implements EditorActiveFileChangedHandler, UndeployGroovyScriptHandler,
   UndeployGroovyScriptSandboxHandler, GroovyUndeployResultReceivedHandler
{

   private FileModel activeFile;

   public UndeployGroovyCommandHandler()
   {
      IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);
      IDE.addHandler(UndeployGroovyScriptEvent.TYPE, this);
      IDE.addHandler(UndeployGroovyScriptSandboxEvent.TYPE, this);
      IDE.addHandler(GroovyUndeployResultReceivedEvent.TYPE, this);
   }

   /**
    * @see org.exoplatform.ide.plugin.groovy.event.UndeployGroovyScriptHandler#onUndeployGroovyScript(org.exoplatform.ide.plugin.groovy.event.UndeployGroovyScriptEvent)
    */
   public void onUndeployGroovyScript(UndeployGroovyScriptEvent event)
   {
      try
      {
         GroovyService.getInstance().undeploy(activeFile.getId(), VirtualFileSystem.getInstance().getInfo().getId(),
            activeFile.getProject().getId(), new AsyncRequestCallback<String>()
            {

               @Override
               protected void onSuccess(String result)
               {
                  undeploySuccess(activeFile.getPath());
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  undeployFail(exception, activeFile.getPath());
               }
            });
      }
      catch (RequestException e)
      {
         undeployFail(e, activeFile.getPath());
      }
   }

   private void undeploySuccess(String href)
   {

      String outputContent = "<b>" + URL.decodePathSegment(href) + "</b> undeployed successfully.";
      IDE.fireEvent(new OutputEvent(outputContent, OutputMessage.Type.INFO));
      // eventBus.fireEvent(new GroovyUndeployResultReceivedEvent(href));
   }

   private void undeployFail(Throwable exc, String path)
   {
      if (exc instanceof ServerException)
      {
         ServerException exception = (ServerException)exc;
         String outputContent = "<b>" + URL.decodePathSegment(path) + "</b> undeploy failed.&nbsp;";
         outputContent += "Error (<i>" + exception.getHTTPStatus() + "</i>: <i>" + exception.getStatusText() + "</i>)";
         if (!exception.getMessage().equals(""))
         {
            outputContent += "<br />" + exception.getMessage().replace("\n", "<br />"); // replace "end of line" symbols on
                                                                                        // "<br />"
         }
      }
      else
      {
         IDE.fireEvent(new ExceptionThrownEvent(exc));
      }

      GroovyUndeployResultReceivedEvent event = new GroovyUndeployResultReceivedEvent(path);
      event.setException(exc);
      IDE.fireEvent(event);
   }

   /**
    * {@inheritDoc}
    */
   public void onUndeployGroovyScriptSandbox(UndeployGroovyScriptSandboxEvent event)
   {
      try
      {
         GroovyService.getInstance().undeploySandbox(activeFile.getId(),
            VirtualFileSystem.getInstance().getInfo().getId(), activeFile.getProject().getId(),
            new AsyncRequestCallback<String>()
            {

               @Override
               protected void onSuccess(String result)
               {
                  undeploySuccess(activeFile.getPath());
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  undeployFail(exception, activeFile.getPath());
               }
            });
      }
      catch (RequestException e)
      {
         undeployFail(e, activeFile.getPath());
      }
   }

   /**
    * @see org.exoplatform.ide.groovy.event.GroovyUndeployResultReceivedHandler#onGroovyUndeployResultReceived(org.exoplatform.ide.groovy.event.GroovyUndeployResultReceivedEvent)
    */
   public void onGroovyUndeployResultReceived(GroovyUndeployResultReceivedEvent event)
   {
      if (event.getException() == null)
      {
         /*
          * Undeploy successfully
          */
         String outputContent = "<b>" + URL.decodePathSegment(event.getPath()) + "</b> undeployed successfully.";
         IDE.fireEvent(new OutputEvent(outputContent, OutputMessage.Type.INFO));
      }
      else
      {
         /*
          * Undeploy failed
          */
         ServerException exception = (ServerException)event.getException();

         String outputContent = "<b>" + URL.decodePathSegment(event.getPath()) + "</b> undeploy failed.&nbsp;";
         outputContent += "Error (<i>" + exception.getHTTPStatus() + "</i>: <i>" + exception.getStatusText() + "</i>)";
         if (!exception.getMessage().equals(""))
         {
            outputContent += "<br />" + exception.getMessage().replace("\n", "<br />"); // replace "end of line" symbols on
                                                                                        // "<br />"
         }
         IDE.fireEvent(new OutputEvent(outputContent, OutputMessage.Type.ERROR));
      }
   }

   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      activeFile = event.getFile();
   }

}
