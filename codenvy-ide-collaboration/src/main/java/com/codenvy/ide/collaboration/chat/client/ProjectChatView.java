/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package com.codenvy.ide.collaboration.chat.client;

import com.codenvy.ide.client.util.Elements;
import com.codenvy.ide.collaboration.chat.client.ChatResources.ChatCss;
import com.codenvy.ide.collaboration.chat.client.ParticipantList.View;
import com.codenvy.ide.collaboration.chat.client.ProjectChatPresenter.Display;
import com.codenvy.ide.collaboration.chat.client.ProjectChatPresenter.MessageCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.ui.Widget;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.AnchorElement;
import elemental.html.DivElement;
import elemental.html.Element;
import elemental.html.SpanElement;
import elemental.html.TextAreaElement;

import org.exoplatform.ide.client.framework.ui.impl.ViewImpl;
import org.exoplatform.ide.client.framework.ui.impl.ViewType;
import org.exoplatform.ide.json.shared.JsonCollections;
import org.exoplatform.ide.json.shared.JsonStringMap;

import java.util.Date;

/**
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class ProjectChatView extends ViewImpl implements Display
{
   interface ProjectChatViewUiBinder extends UiBinder<Widget, ProjectChatView>
   {
   }

   private static ProjectChatViewUiBinder ourUiBinder = GWT.create(ProjectChatViewUiBinder.class);

   private DateTimeFormat timeFormat = DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM);

   @UiField
   ScrollPanel chatPanel;

   @UiField
   com.google.gwt.dom.client.TextAreaElement chatMessageInput;

   @UiField
   ScrollPanel participantsPanel;

   private String lastClientId;

   private Element lastMessageElement;

   private ChatCss css;

   private JsonStringMap<Element> currentUserMessages = JsonCollections.createMap();

   private final ParticipantList participantList;

   public ProjectChatView()
   {
      super(ID, ViewType.INFORMATION, "Collaboration", new Image(ChatExtension.resources.collaborators()));
      add(ourUiBinder.createAndBindUi(this));
      css = ChatExtension.resources.chatCss();
      View view = new View();
      participantList = ParticipantList.create(view);
      participantsPanel.getElement().appendChild((Node)view.getElement());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getChatMessage()
   {
      return chatMessageInput.getValue();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void addMessage(Participant participant, String message, long time)
   {
      addMessage(participant, message, time, null);
   }

   @Override
   public void addMessage(Participant participant, String message, long time, MessageCallback callback)
   {
      Date d = new Date(time);
      DivElement messageElement;
      if (participant.getClientId().equals(lastClientId))
      {
         messageElement = getMessageElement(message, "...", d, callback);
         lastMessageElement.appendChild(messageElement);
      }
      else
      {
         messageElement = getMessageElement(message, participant.getDisplayName() + ":", d, callback);
         chatPanel.getElement().appendChild((Node)messageElement);
         lastClientId = participant.getClientId();
         lastMessageElement = messageElement;
      }
      if (participant.isCurrentUser())
      {
         currentUserMessages.put(String.valueOf(d.getTime()), messageElement);
      }
      chatPanel.scrollToBottom();
   }

   private DivElement getMessageElement(String message, String name, Date d, MessageCallback callback)
   {
      DivElement messageElement = Elements.createDivElement();
      DivElement timeElement = getTimeElement(d);
      messageElement.appendChild(timeElement);

      SpanElement nameElement = getNameElement(name);
      messageElement.appendChild(nameElement);
      if (callback != null)
      {
         AnchorElement anchorElement = createAnchorElement(message, callback);
         messageElement.appendChild(anchorElement);
      }
      else
      {
         DivElement messageDiv = Elements.createDivElement(css.chatMessage());
         messageDiv.setInnerHTML(message);
         messageElement.appendChild(messageDiv);
      }
      return messageElement;
   }

   private SpanElement getNameElement(String name)
   {
      SpanElement nameElement = Elements.createSpanElement(css.chatName());
      nameElement.setInnerHTML(name + "&nbsp;");
      return nameElement;
   }

   private DivElement getTimeElement(Date d)
   {
      DivElement timeElement = Elements.createDivElement(css.chatTime());
      timeElement.setInnerHTML("[" + timeFormat.format(d) + "]");
      return timeElement;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void addListener(EventListener eventListener)
   {
      ((TextAreaElement)chatMessageInput).setOnKeyPress(eventListener);
      ((TextAreaElement)chatMessageInput).setOnKeyDown(eventListener);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void messageNotDelivered(String messageId)
   {
      if (currentUserMessages.containsKey(messageId))
      {
         Element messageElement = currentUserMessages.get(messageId);

         DivElement divElement = Elements.createDivElement(css.messageNotDelivered());
         divElement.setTitle("This message is not delivered yet.");
         messageElement.getFirstChildElement().getNextSiblingElement().appendChild(divElement);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void messageDelivered(String messageId)
   {
      if (currentUserMessages.containsKey(messageId))
      {
         Element messageElement = currentUserMessages.get(messageId);

         messageElement.getFirstChildElement().getNextSiblingElement().getFirstChildElement().removeFromParent();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void removeParticipant(Participant participant)
   {
      participantList.participantRemoved(participant);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void addParticipant(Participant participant)
   {
      participantList.participantAdded(participant);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void removeEditParticipant(String clientId)
   {
      participantList.removeEditParticipant(clientId);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void addEditParticipant(String clientId, String color)
   {
      participantList.setEditParticipant(clientId, color);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void clearEditParticipants()
   {
      participantList.clearEditParticipants();
   }

   @Override
   public void addNotificationMessage(String message)
   {
      DivElement messageElement = Elements.createDivElement(css.chatNotification());
      messageElement.appendChild(Elements.createTextNode(message));
      chatPanel.getElement().appendChild((Node)messageElement);
      lastClientId = "";
      chatPanel.scrollToBottom();
   }

   @Override
   public void addNotificationMessage(String message, String link, MessageCallback callback)
   {
      String[] split = message.split("\\{0\\}");
      DivElement messageElement = Elements.createDivElement(css.chatNotification());
      messageElement.appendChild(Elements.createTextNode(split[0]));
      messageElement.appendChild(createAnchorElement(link, callback));
      if (split.length > 1)
      {
         messageElement.appendChild(Elements.createTextNode(split[1]));
      }
      lastClientId = "";
      chatPanel.getElement().appendChild((Node)messageElement);
      chatPanel.scrollToBottom();
   }

   private AnchorElement createAnchorElement(final String message, final MessageCallback callback)
   {
      AnchorElement anchorElement = Elements.createAnchorElement(css.link());
      anchorElement.setHref("javascript:;");
      anchorElement.setTextContent(message);
      if (callback != null)
      {
         anchorElement.addEventListener(Event.CLICK, new EventListener()
         {
            @Override
            public void handleEvent(Event event)
            {
               callback.messageClicked();
            }
         }, false);
      }
      return anchorElement;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void clearMessage()
   {
      chatMessageInput.setValue("");
   }
}