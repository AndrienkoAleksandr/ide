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
package com.google.collide.client;

import com.google.collide.client.bootstrap.BootstrapSession;
import com.google.collide.client.collaboration.CollaborationManager;
import com.google.collide.client.collaboration.DocOpsSavedNotifier;
import com.google.collide.client.collaboration.IncomingDocOpDemultiplexer;
import com.google.collide.client.communication.VertxBus;
import com.google.collide.client.communication.VertxBusWebsoketImpl;
import com.google.collide.client.document.DocumentManager;
import com.google.collide.client.util.ClientImplementationsInjector;
import com.google.collide.client.util.Elements;
import com.google.collide.codemirror2.CodeMirror2;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.DOM;
import elemental.dom.Node;

import org.exoplatform.ide.client.framework.module.Extension;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.userinfo.event.UserInfoReceivedEvent;
import org.exoplatform.ide.client.framework.userinfo.event.UserInfoReceivedHandler;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:  7/18/12 evgen $
 */
public class CollabEditorExtension extends Extension implements UserInfoReceivedHandler
{

   private static CollabEditorExtension instance;

   private AppContext context;

   private DocumentManager documentManager;

   public static CollabEditorExtension get()
   {
      return instance;
   }

   /**
    * @see org.exoplatform.ide.client.framework.module.Extension#initialize()
    */
   @Override
   public void initialize()
   {
      instance = this;
      ClientImplementationsInjector.inject();

      IDE.addHandler(UserInfoReceivedEvent.TYPE, this);
      context = AppContext.create();
      documentManager = DocumentManager.create(context);

      new ParticipantsPresenter();
   }

   public AppContext getContext()
   {
      return context;
   }

   public DocumentManager getManager()
   {
      return documentManager;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void onUserInfoReceived(UserInfoReceivedEvent event)
   {
      init();

      //TODO refactor this
      //This code use new socket connection for receiving user session id.
      final VertxBus bus = VertxBusWebsoketImpl.create();
      bus.setOnOpenCallback(new VertxBus.ConnectionListener()
      {
         @Override
         public void onOpen()
         {
            bus.send("ide/collab_editor/participants/add", "{}", new VertxBus.ReplyHandler()
            {
               @Override
               public void onReply(String message)
               {
                  JSONObject object = JSONParser.parseLenient(message).isObject();
                  BootstrapSession.getBootstrapSession().setUserId(object.get("userId").isString().stringValue());
                  BootstrapSession.getBootstrapSession().setActiveClientId(object.get("activeClientId").isString().stringValue());
                  context.initializeCollaboration();
//                  ParticipantModel participantModel = ParticipantModel.create(context.getFrontendApi(), context.getMessageFilter());
                  IncomingDocOpDemultiplexer docOpRecipient = IncomingDocOpDemultiplexer.create(context.getMessageFilter());
                  CollaborationManager collaborationManager =
                     CollaborationManager.create(context, documentManager, docOpRecipient);

                  DocOpsSavedNotifier docOpSavedNotifier = new DocOpsSavedNotifier(documentManager, collaborationManager);
                  bus.close();
               }
            });
         }

         @Override
         public void onClose()
         {
         }
      });

   }

   /**
    *
    */
   private void init()
   {
      com.google.collide.client.Resources resources = context.getResources();
      com.google.gwt.user.client.Element div = DOM.createDiv();
      div.setId(AppContext.GWT_ROOT);
      Elements.getBody().appendChild((Node)div);
//
//    // TODO: Figure out why when we use the + operator to concat,
//    // these Strings don't at compile time converge to a single String literal.
//    // In theory they should. For now we use a StringBuilder.
//
//    // Make sure you call getText() on your CssResource!
      StringBuilder styleBuilder = new StringBuilder();
      styleBuilder.append(resources.appCss().getText());
      styleBuilder.append(resources.baseCss().getText());
//    styleBuilder.append(resources.workspaceHeaderCss().getText());
//    styleBuilder.append(resources.editorToolBarCss().getText());
      styleBuilder.append(resources.defaultSimpleListCss().getText());
//    styleBuilder.append(resources.workspaceShellCss().getText());
      styleBuilder.append(resources.workspaceEditorCss().getText());
      styleBuilder.append(resources.workspaceEditorBufferCss().getText());
      styleBuilder.append(resources.workspaceEditorCursorCss().getText());
//    styleBuilder.append(resources.workspaceEditorConsoleViewCss().getText());
//    styleBuilder.append(resources.workspaceEditorDebuggingModelCss().getText());
//    styleBuilder.append(resources.workspaceEditorDebuggingSidebarCss().getText());
//    styleBuilder.append(resources.workspaceEditorDebuggingSidebarBreakpointsPaneCss().getText());
//    styleBuilder.append(resources.workspaceEditorDebuggingSidebarCallStackPaneCss().getText());
//    styleBuilder.append(resources.workspaceEditorDebuggingSidebarControlsPaneCss().getText());
//    styleBuilder.append(resources.workspaceEditorDebuggingSidebarHeaderCss().getText());
//    styleBuilder.append(resources.workspaceEditorDebuggingSidebarNoApiPaneCss().getText());
//    styleBuilder.append(resources.workspaceEditorDebuggingSidebarScopeVariablesPaneCss().getText());
//    styleBuilder.append(resources.workspaceEditorDomInspectorCss().getText());
//    styleBuilder.append(
//        resources.workspaceEditorDebuggingSidebarWatchExpressionsPaneCss().getText());
//    styleBuilder.append(resources.remoteObjectTreeCss().getText());
//    styleBuilder.append(resources.remoteObjectNodeRendererCss().getText());
//    styleBuilder.append(resources.editorDiffContainerCss().getText());
//    styleBuilder.append(resources.evaluationPopupControllerCss().getText());
//    styleBuilder.append(resources.goToDefinitionCss().getText());
//    styleBuilder.append(resources.treeCss().getText());
//    styleBuilder.append(resources.workspaceNavigationCss().getText());
//    styleBuilder.append(resources.workspaceNavigationFileTreeSectionCss().getText());
//    styleBuilder.append(resources.workspaceNavigationShareWorkspacePaneCss().getText());
//    styleBuilder.append(resources.workspaceNavigationToolBarCss().getText());
//    styleBuilder.append(resources.workspaceNavigationFileTreeNodeRendererCss().getText());
//    styleBuilder.append(resources.workspaceNavigationOutlineNodeRendererCss().getText());
      styleBuilder.append(resources.workspaceNavigationParticipantListCss().getText());
//    styleBuilder.append(resources.searchContainerCss().getText());
//    styleBuilder.append(resources.statusPresenterCss().getText());
//    styleBuilder.append(resources.noFileSelectedPanelCss().getText());
//    styleBuilder.append(resources.diffRendererCss().getText());
//    styleBuilder.append(resources.deltaInfoBarCss().getText());
//    styleBuilder.append(resources.codePerspectiveCss().getText());
//    styleBuilder.append(resources.unauthorizedUserCss().getText());
      styleBuilder.append(resources.syntaxHighlighterRendererCss().getText());
      styleBuilder.append(resources.lineNumberRendererCss().getText());
//    styleBuilder.append(resources.uneditableDisplayCss().getText());
      styleBuilder.append(resources.editorSelectionLineRendererCss().getText());
//    styleBuilder.append(resources.fileHistoryCss().getText());
//    styleBuilder.append(resources.timelineCss().getText());
//    styleBuilder.append(resources.timelineNodeCss().getText());
      styleBuilder.append(resources.popupCss().getText());
      styleBuilder.append(resources.tooltipCss().getText());
//    styleBuilder.append(resources.sliderCss().getText());
      styleBuilder.append(resources.editableContentAreaCss().getText());
//    styleBuilder.append(resources.workspaceLocationBreadcrumbsCss().getText());
//    styleBuilder.append(resources.awesomeBoxCss().getText());
//    styleBuilder.append(resources.awesomeBoxSectionCss().getText());
//    styleBuilder.append(resources.centerPanelCss().getText());
      styleBuilder.append(resources.autocompleteComponentCss().getText());
//    styleBuilder.append(resources.runButtonTargetPopupCss().getText());
//    styleBuilder.append(resources.popupBlockedInstructionalPopupCss().getText());
//    styleBuilder.append(resources.dropdownWidgetsCss().getText());
      styleBuilder.append(resources.parenMatchHighlighterCss().getText());
//    styleBuilder.append(resources.awesomeBoxHostCss().getText());
//    styleBuilder.append(resources.awesomeBoxComponentCss().getText());
      styleBuilder.append(resources.coachmarkCss().getText());
//    styleBuilder.append(resources.sidebarListCss().getText());
//
//    /*
//     * workspaceNavigationSectionCss, animationController, and
//     * resizeControllerCss must come last because they overwrite the CSS
//     * properties from previous CSS rules.
//     */
//    styleBuilder.append(resources.workspaceNavigationSectionCss().getText());
//    styleBuilder.append(resources.resizeControllerCss().getText());
//
      styleBuilder.append(resources.searchMatchRendererCss().getText());
      styleBuilder.append(resources.notificationCss().getText());
      StyleInjector.inject(styleBuilder.toString());
      Elements.injectJs(CodeMirror2.getJs());
   }

}
