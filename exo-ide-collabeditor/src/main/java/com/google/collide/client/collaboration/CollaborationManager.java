// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.collide.client.collaboration;

import com.codenvy.ide.users.UsersModel;
import com.google.collide.client.AppContext;
import com.google.collide.client.code.ParticipantModel;
import com.google.collide.client.communication.PushChannel;
import com.google.collide.client.document.DocumentManager;
import com.google.collide.client.document.DocumentManager.LifecycleListener;
import com.google.collide.client.document.DocumentMetadata;
import com.google.collide.client.editor.Editor;
import com.google.collide.dto.DocumentSelection;
import com.google.collide.dto.FileCollaboratorGone;
import com.google.collide.dto.FileContents;
import com.google.collide.dto.GetOpenedFilesInWorkspaceResponse;
import com.google.collide.dto.NewFileCollaborator;
import com.google.collide.dto.ParticipantUserDetails;
import com.google.collide.dto.RoutingTypes;
import com.google.collide.dto.UserDetails;
import com.google.collide.dto.UserLogInDto;
import com.google.collide.dto.client.DtoClientImpls.GetOpenendFilesInWorkspaceImpl;
import com.google.collide.shared.document.Document;

import org.exoplatform.ide.client.framework.websocket.FrontendApi.ApiCallback;
import org.exoplatform.ide.client.framework.websocket.MessageFilter;
import org.exoplatform.ide.client.framework.websocket.MessageFilter.MessageRecipient;
import org.exoplatform.ide.dtogen.shared.ServerError.FailureReason;
import org.exoplatform.ide.json.client.JsIntegerMap;
import org.exoplatform.ide.json.client.Jso;
import org.exoplatform.ide.json.shared.JsonArray;
import org.exoplatform.ide.json.shared.JsonCollections;
import org.exoplatform.ide.json.shared.JsonIntegerMap;
import org.exoplatform.ide.json.shared.JsonStringMap;
import org.exoplatform.ide.shared.util.ListenerManager;
import org.exoplatform.ide.shared.util.ListenerManager.Dispatcher;
import org.exoplatform.ide.shared.util.ListenerRegistrar.RemoverManager;

/**
 * A manager for real-time collaboration.
 *
 * This class listens for document lifecycle changes and creates or tears down individual
 * {@link DocumentCollaborationController}s.
 */
public class CollaborationManager
{

   public interface ParticipantsListener
   {
      void userOpenFile(String path, UserDetails user);

      void userCloseFile(String path, UserDetails user);
   }

   private final LifecycleListener lifecycleListener = new LifecycleListener()
   {
      @Override
      public void onDocumentCreated(Document document)
      {
      }

      @Override
      public void onDocumentGarbageCollected(Document document)
      {
      }

      @Override
      public void onDocumentOpened(Document document, Editor editor)
      {
         handleDocumentOpened(document, editor);
      }

      @Override
      public void onDocumentClosed(Document document, Editor editor)
      {
         handleDocumentClosed(document, editor);
      }

      @Override
      public void onDocumentLinkedToFile(Document document, FileContents fileContents)
      {
         JsonArray<DocumentSelection> selections = JsonCollections.createArray();
         JsonArray<String> serializedSelections = fileContents.getSelections();
         for (int i = 0, n = serializedSelections.size(); i < n; i++)
         {
            selections.add((DocumentSelection)Jso.deserialize(serializedSelections.get(i)));
         }

         handleDocumentLinkedToFile(document, selections);
      }

      @Override
      public void onDocumentUnlinkingFromFile(Document document)
      {
         handleDocumentUnlinkingFromFile(document);
      }
   };

   private final PushChannel.Listener pushChannelListener = new PushChannel.Listener()
   {
      @Override
      public void onReconnectedSuccessfully()
      {
         docCollabControllersByDocumentId.iterate(
            new JsonIntegerMap.IterationCallback<DocumentCollaborationController>()
            {
               @Override
               public void onIteration(
                  int documentId, DocumentCollaborationController collabController)
               {
                  collabController.handleTransportReconnectedSuccessfully();
               }
            });
      }
   };

   private final MessageFilter.MessageRecipient<NewFileCollaborator> newFileCollaboratorMessageRecipient = new MessageFilter.MessageRecipient<NewFileCollaborator>()
   {
      @Override
      public void onMessageReceived(NewFileCollaborator message)
      {
         addNewCollaborator(message);
      }
   };

   private final MessageFilter.MessageRecipient<FileCollaboratorGone> fileCollaboratorGoneMessageRecipient = new MessageFilter.MessageRecipient<FileCollaboratorGone>()
   {
      @Override
      public void onMessageReceived(FileCollaboratorGone message)
      {
          removeCollaborator(message);
      }
   };

   private final MessageRecipient<UserLogInDto> userLogInDtoMessageRecipient = new MessageRecipient<UserLogInDto>()
   {
      @Override
      public void onMessageReceived(UserLogInDto message)
      {

      }
   };

   private final AppContext appContext;

   private final RemoverManager removerManager = new RemoverManager();

   private final JsIntegerMap<DocumentCollaborationController> docCollabControllersByDocumentId =
      JsIntegerMap.create();

   private DocumentManager documentManager;

   private final IncomingDocOpDemultiplexer docOpRecipient;

   private UsersModel usersModel;

   //   private JsonIntegerMap<ParticipantList.View> participantsViews = JsonCollections.createIntegerMap();

   private JsonStringMap<JsonArray<ParticipantUserDetails>> openedFilesInWorkspace = JsonCollections.createMap();

   private final ListenerManager<ParticipantsListener> participantsListenerManager = ListenerManager.create();

   private final JsonStringMap<String> path2sessionId = JsonCollections.createMap();

   private CollaborationManager(AppContext appContext, DocumentManager documentManager,
                                IncomingDocOpDemultiplexer docOpRecipient, UsersModel usersModel)
   {
      this.appContext = appContext;
      this.documentManager = documentManager;
      this.docOpRecipient = docOpRecipient;
      this.usersModel = usersModel;
      removerManager.track(documentManager.getLifecycleListenerRegistrar().add(lifecycleListener));
      removerManager.track(
         appContext.getPushChannel().getListenerRegistrar().add(pushChannelListener));
      appContext.getMessageFilter().registerMessageRecipient(RoutingTypes.NEWFILECOLLABORATOR, newFileCollaboratorMessageRecipient);
      appContext.getMessageFilter().registerMessageRecipient(RoutingTypes.FILECOLLABORATORGONE, fileCollaboratorGoneMessageRecipient);
      appContext.getFrontendApi().GET_ALL_FILES.send(GetOpenendFilesInWorkspaceImpl.make(),new ApiCallback<GetOpenedFilesInWorkspaceResponse>()
      {
         @Override
         public void onFail(FailureReason reason)
         {
            //do nothing
         }

         @Override
         public void onMessageReceived(GetOpenedFilesInWorkspaceResponse message)
         {
            openedFilesInWorkspace.putAll(message.getOpenedFiles());
         }
      });

   }

   public static CollaborationManager create(AppContext appContext, DocumentManager documentManager,
                                             IncomingDocOpDemultiplexer docOpRecipient, UsersModel usersModel)
   {
    /*
     * Ideally this whole stack wouldn't be stuck on passing around a workspace id but it is too
     * much work right now to refactor it out so here it stays.
     */
      return new CollaborationManager(appContext, documentManager,
         docOpRecipient, usersModel);
   }

   public void cleanup()
   {
      docOpRecipient.teardown();
      removerManager.remove();
   }

   DocumentCollaborationController getDocumentCollaborationController(int documentId)
   {
      return docCollabControllersByDocumentId.get(documentId);
   }

   private void handleDocumentLinkedToFile(
      Document document, JsonArray<DocumentSelection> selections)
   {

      String fileEditSessionKey = DocumentMetadata.getFileEditSessionKey(document);
      ParticipantModel participantModel = ParticipantModel.create(appContext.getFrontendApi(), appContext.getMessageFilter(), usersModel, fileEditSessionKey);
//      ParticipantList.View view = new ParticipantList.View(appContext.getResources());
//      ParticipantList.create(view, appContext.getResources(), participantModel);
//      participantsViews.put(document.getId(),view);
      DocumentCollaborationController docCollabController = new DocumentCollaborationController(
         appContext, participantModel, docOpRecipient, document, selections);
      docCollabController.initialize(fileEditSessionKey,
         DocumentMetadata.getBeginCcRevision(document));

      docCollabControllersByDocumentId.put(document.getId(), docCollabController);
   }

   private void handleDocumentUnlinkingFromFile(Document document)
   {
      DocumentCollaborationController docCollabController =
         docCollabControllersByDocumentId.remove(document.getId());
      if (docCollabController != null)
      {
         docCollabController.teardown();
      }
   }

   private void handleDocumentOpened(Document document, Editor editor)
   {
      DocumentCollaborationController docCollabController =
         docCollabControllersByDocumentId.get(document.getId());
      if (docCollabController != null)
      {
         docCollabController.attachToEditor(editor);
//         editor.getBuffer().addUnmanagedElement(participantsViews.get(document.getId()).getElement());
      }
   }

   private void handleDocumentClosed(Document document, Editor editor)
   {
      DocumentCollaborationController docCollabController =
         docCollabControllersByDocumentId.get(document.getId());
      if (docCollabController != null)
      {
         docCollabController.detachFromEditor();
      }
//      participantsViews.erase(document.getId());
   }

   private void addNewCollaborator(final NewFileCollaborator message)
   {
      Document document = documentManager.getDocumentByFilePath(message.getPath());
      if(document != null)
      {
         DocumentCollaborationController collaborationController = docCollabControllersByDocumentId.get(document.getId());
         collaborationController.getParticipantModel().addParticipant(message.getParticipant());
      }

      if(!openedFilesInWorkspace.containsKey(message.getPath()))
      {
         openedFilesInWorkspace.put(message.getPath(),JsonCollections.<ParticipantUserDetails>createArray());
      }
      openedFilesInWorkspace.get(message.getPath()).add(message.getParticipant());
      path2sessionId.put(message.getPath(), message.getEditSessionId());
      participantsListenerManager.dispatch(new Dispatcher<ParticipantsListener>()
      {
         @Override
         public void dispatch(ParticipantsListener listener)
         {
            listener.userOpenFile(message.getPath(), message.getParticipant().getUserDetails());
         }
      });
   }

   private void removeCollaborator(final FileCollaboratorGone message)
   {
      Document document = documentManager.getDocumentByFilePath(message.getPath());
      if(document != null)
      {
         DocumentCollaborationController collaborationController = docCollabControllersByDocumentId.get(document.getId());
         collaborationController.getParticipantModel().removeParticipant(message.getParticipant());
      }

      JsonArray<ParticipantUserDetails> participants = openedFilesInWorkspace.get(message.getPath());
      if(participants == null)
         return;

      ParticipantUserDetails toRemove = null;
      for(ParticipantUserDetails p : participants.asIterable())
      {
         if(p.getParticipant().getId().equals(message.getParticipant().getParticipant().getId()))
         {
            toRemove = p;
            break;
         }
      }

      if(toRemove != null)
      {
         participants.remove(toRemove);
      }

      if(participants.isEmpty())
      {
         openedFilesInWorkspace.remove(message.getPath());
      }
      path2sessionId.remove(message.getPath());
      participantsListenerManager.dispatch(new Dispatcher<ParticipantsListener>()
      {
         @Override
         public void dispatch(ParticipantsListener listener)
         {
            listener.userCloseFile(message.getPath(), message.getParticipant().getUserDetails());
         }
      });
   }

   public boolean isFileOpened(String path)
   {
      return openedFilesInWorkspace.containsKey(path);
   }

   public JsonArray<ParticipantUserDetails> getParticipantsForFile(String path)
   {
      return openedFilesInWorkspace.get(path);
   }


   public JsonArray<String> getOpenedFiles()
   {
      return openedFilesInWorkspace.getKeys();
   }

   public ListenerManager<ParticipantsListener> getParticipantsListenerManager()
   {
      return participantsListenerManager;
   }

   public String getEditSessionId(String path)
   {
      return path2sessionId.get(path);
   }


}
