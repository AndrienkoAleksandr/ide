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
package com.google.collide.client.editor.gutter;

import com.google.collide.client.ui.tooltip.Tooltip.TooltipRenderer;

import com.google.collide.client.Resources;
import com.google.collide.client.code.errorrenderer.ErrorReceiver.ErrorListener;
import com.google.collide.client.editor.Buffer;
import com.google.collide.client.editor.Editor;
import com.google.collide.client.editor.Editor.DocumentListener;
import com.google.collide.client.editor.gutter.Gutter.ClickListener;
import com.google.collide.client.ui.menu.PositionController.HorizontalAlign;
import com.google.collide.client.ui.menu.PositionController.Position;
import com.google.collide.client.ui.menu.PositionController.Positioner;
import com.google.collide.client.ui.menu.PositionController.PositionerBuilder;
import com.google.collide.client.ui.menu.PositionController.VerticalAlign;
import com.google.collide.client.ui.tooltip.Tooltip;
import com.google.collide.client.util.Elements;
import com.google.collide.client.util.JsIntegerMap;
import com.google.collide.dto.CodeError;
import com.google.collide.dto.FilePosition;
import com.google.collide.dto.client.DtoClientImpls.CodeErrorImpl;
import com.google.collide.dto.client.DtoClientImpls.FilePositionImpl;
import com.google.collide.json.client.JsoArray;
import com.google.collide.mvp.CompositeView;
import com.google.collide.shared.document.Document;
import com.google.collide.shared.document.LineInfo;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.event.shared.HandlerRegistration;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.Element;

import org.exoplatform.ide.editor.client.marking.Marker;
import org.exoplatform.ide.editor.client.marking.ProblemClickEvent;
import org.exoplatform.ide.editor.client.marking.ProblemClickHandler;
import org.exoplatform.ide.editor.shared.text.BadLocationException;
import org.exoplatform.ide.editor.shared.text.IDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 *
 */
public class NotificationManager implements DocumentListener
{
   /**
    * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
    * @version $Id:
    *
    */
   private final class ClickListenerImpl implements ClickListener
   {
      private final ProblemClickHandler handler;

      /**
       * @param handler
       */
      public ClickListenerImpl(ProblemClickHandler handler)
      {
         this.handler = handler;
      }

      @Override
      public void onClick(int y)
      {
         int lineNumber = buffer.convertYToLineNumber(y, true);
         JsoArray<Marker> jsoArray = markers.get(lineNumber);
         Marker[] arr = new Marker[jsoArray.size()];
         for (int i = 0; i < jsoArray.size(); i++)
         {
            arr[i] = jsoArray.get(i);
         }
         handler.onProblemClick(new ProblemClickEvent(arr));
      }
   }

   private class HtmlTooltipRenderer implements TooltipRenderer
   {

      private String message;

      /**
       * @see com.google.collide.client.ui.tooltip.Tooltip.TooltipRenderer#renderDom()
       */
      @Override
      public Element renderDom()
      {
         Element content = Elements.createDivElement();
         content.setInnerHTML(message);
         return content;
      }

      /**
       * @param message the message to set
       */
      public void setMessage(String message)
      {
         this.message = message;
      }

   }

   private Buffer buffer;

   private Gutter leftGutter;

   private JsIntegerMap<JsoArray<Marker>> markers = JsIntegerMap.<JsoArray<Marker>> create();

   private final JsoArray<Element> elements = JsoArray.create();

   private final Resources res;

   private final Editor editor;

   private IDocument document;

   private final Gutter overviewGutter;

   private NotificationMark bottomMark;

   private int errors, warnings;

   private JsoArray<NotificationMark> overviewMarks = JsoArray.<NotificationManager.NotificationMark> create();

   private PositionerBuilder rightPositioner;

   private PositionerBuilder leftPositioner;

   private ErrorListener errorListener;

   /**
    * @param buffer
    * @param gutter
    * @param overviewGutter 
    */
   public NotificationManager(Editor editor, Gutter gutter, Gutter overviewGutter, Resources res)
   {
      super();
      this.editor = editor;
      this.overviewGutter = overviewGutter;
      this.buffer = editor.getBuffer();
      this.leftGutter = gutter;
      this.res = res;
      rightPositioner =
         new Tooltip.TooltipPositionerBuilder().setHorizontalAlign(HorizontalAlign.RIGHT).setPosition(Position.OVERLAP)
            .setVerticalAlign(VerticalAlign.TOP);
      leftPositioner =
         new Tooltip.TooltipPositionerBuilder().setHorizontalAlign(HorizontalAlign.LEFT).setPosition(Position.OVERLAP)
            .setVerticalAlign(VerticalAlign.TOP);
   }

   /**
    * @param problem
    */
   public void addProblem(Marker problem)
   {
      int lineNumber = problem.getLineNumber() - 1;
      if (!markers.hasKey(lineNumber))
         markers.put(lineNumber, JsoArray.<Marker> create());
      markers.get(lineNumber).add(problem);
      StringBuilder message = new StringBuilder();
      JsoArray<Marker> problemList = markers.get(lineNumber);
      boolean hasError = fillMessages(problemList, message);

      NotificationMark m = new NotificationMark(message.toString(), res, leftPositioner, new HtmlTooltipRenderer());
      m.setTopPosition(buffer.calculateLineTop(lineNumber), "px");
      m.setStyleName(getStyleForLine(problemList, hasError));
      elements.add(m.getElement());
      leftGutter.addUnmanagedElement(m.getElement());

      addOverviewMark(problem, message.toString());

   }

   /**
    * @param problem
    * @param string
    */
   private void addOverviewMark(Marker problem, String string)
   {
      NotificationMark mark =
         new NotificationMark(problem, string, res, editor, rightPositioner, new HtmlTooltipRenderer());
      mark.setTopPosition((100 * problem.getLineNumber()) / document.getNumberOfLines(), "%");
      overviewGutter.addUnmanagedElement(mark.getElement());
      overviewMarks.add(mark);
      if (problem.isError())
      {
         errors++;
      }

      if (problem.isWarning())
      {
         warnings++;
      }

      if (errors != 0)
      {
         bottomMark.setMessage("Errors: " + errors);
         bottomMark.setStyleName(res.notificationCss().overviewBottomMarkError());
      }
      else if (warnings != 0)
      {
         bottomMark.setMessage("Warnings: " + warnings);
         bottomMark.setStyleName(res.notificationCss().overviewBottomMarkWarning());
      }
   }

   /**
    * @param markerList
    * @param hasError
    * @return
    */
   private String getStyleForLine(JsoArray<Marker> markerList, boolean hasError)
   {
      String markStyle = null;
      if (hasError)
      {
         markStyle = res.notificationCss().markError();
      }
      else
      {
         markStyle = "";
         for (Marker p : markerList.asIterable())
         {
            if (p.isWarning())
            {
               markStyle = res.notificationCss().markWarning();
            }
            else
               markStyle = res.notificationCss().markTask();
         }
      }
      return markStyle;
   }

   private boolean fillMessages(JsoArray<Marker> markers, StringBuilder message)
   {
      boolean hasError = false;
      List<String> messages = new ArrayList<String>();

      for (Marker p : markers.asIterable())
      {
         messages.add(p.getMessage());
         if (!hasError && p.isError())
         {
            hasError = true;
         }
      }

      if (messages.size() == 1)
      {
         message.append(markers.get(0).getMessage());
      }
      else
      {
         message.append("Multiple markers at this line<br>");
         for (String m : messages)
         {
            message.append("&nbsp;&nbsp;&nbsp;-&nbsp;").append(m).append("<br>");
         }
      }

      return hasError;
   }

   /**
    * @param problem
    */
   public void unmarkProblem(Marker problem)
   {
      //TODO
      throw new UnsupportedOperationException();
   }

   /**
    * 
    */
   public void clear()
   {
      for (int i = 0, n = elements.size(); i < n; i++)
      {
         leftGutter.removeUnmanagedElement(elements.get(i));
      }

      JsArrayNumber keys = markers.getKeys();
      for (int i = 0; i < keys.length(); i++)
      {
         double line = keys.get(i);
         markers.erase((int)line);
      }
      errorListener.onErrorsChanged(JsoArray.<CodeError>create());
      markers = JsIntegerMap.<JsoArray<Marker>> create();
      errorListener.onErrorsChanged(JsoArray.<CodeError>create());
      elements.clear();
      errors = 0;
      warnings = 0;
      for (NotificationMark m : overviewMarks.asIterable())
      {
         overviewGutter.removeUnmanagedElement(m.getElement());
      }
      bottomMark.getElement().removeAttribute("class");
      bottomMark.getElement().removeAttribute("title");
   }

   /**
    * @return the gutter
    */
   public Gutter getLeftGutter()
   {
      return leftGutter;
   }

   /**
    * @param handler
    * @return
    */
   public HandlerRegistration addProblemClickHandler(ProblemClickHandler handler)
   {
      final ClickListenerImpl listener = new ClickListenerImpl(handler);
      leftGutter.getClickListenerRegistrar().add(listener);
      return new HandlerRegistration()
      {

         @Override
         public void removeHandler()
         {
            leftGutter.getClickListenerRegistrar().remove(listener);
         }
      };

   }

   /**
    * @return the markers
    */
   public JsIntegerMap<JsoArray<Marker>> getMarkers()
   {
      return markers;
   }

   /**
    * @see com.google.collide.client.editor.Editor.DocumentListener#onDocumentChanged(com.google.collide.shared.document.Document, com.google.collide.shared.document.Document)
    */
   @Override
   public void onDocumentChanged(Document oldDocument, Document newDocument)
   {
      document = newDocument.<IDocument> getTag("IDocument");
      bottomMark = new NotificationMark("", res, rightPositioner, new HtmlTooltipRenderer());
      bottomMark.getElement().getStyle().setBottom(2, "px");
      overviewGutter.addUnmanagedElement(bottomMark.getElement());
   }

   private static class NotificationMark extends CompositeView<Marker> implements EventListener
   {
      private final GutterNotificationResources res;

      private Editor editor;

      private Tooltip tooltip;

      private final PositionerBuilder positionerBuilder;

      private final HtmlTooltipRenderer renderer;

      /**
       * @param message 
       * @param tooltipRenderer 
       * 
       */
      public NotificationMark(String message, GutterNotificationResources res, PositionerBuilder positionerBuilder,
         HtmlTooltipRenderer tooltipRenderer)
      {
         this.res = res;
         this.positionerBuilder = positionerBuilder;
         this.renderer = tooltipRenderer;
         Element element = Elements.createDivElement();
         setElement(element);
         element.addEventListener(Event.MOUSEDOWN, this, false);
         Positioner p = positionerBuilder.buildAnchorPositioner(getElement());
         tooltipRenderer.setMessage(message);
         tooltip = new Tooltip.Builder(res, getElement(), p).setTooltipRenderer(tooltipRenderer).build();
      }

      /**
       * 
       */
      public NotificationMark(Marker marker, String message, GutterNotificationResources res, Editor editor,
         PositionerBuilder positionerBuilder, HtmlTooltipRenderer tooltipRenderer)
      {
         this(message, res, positionerBuilder, tooltipRenderer);
         this.editor = editor;
         setStyleName(getStyleName(marker));
         setDelegate(marker);
      }

      public void setStyleName(String style)
      {
         getElement().setAttribute("class", style);
      }

      /**
       * @param message
       */
      public void setMessage(String message)
      {
         tooltip.destroy();
         Positioner p = positionerBuilder.buildAnchorPositioner(getElement());
         renderer.setMessage(message);
         tooltip = new Tooltip.Builder(res, getElement(), p).setTooltipRenderer(renderer).build();
      }

      /**
       * @see elemental.events.EventListener#handleEvent(elemental.events.Event)
       */
      @Override
      public void handleEvent(Event evt)
      {
         if (evt.getType().equals(Event.MOUSEDOWN))
         {
            if (getDelegate() != null)
            {
               LineInfo lineInfo = editor.getDocument().getLineFinder().findLine(getDelegate().getLineNumber() - 1);
               editor.getSelection().setCursorPosition(lineInfo, 0);
            }
         }

      }

      /**
       * @param problem
       * @return
       */
      private String getStyleName(Marker problem)
      {
         if (problem.isError())
         {
            return res.notificationCss().overviewMarkError();
         }

         if (problem.isWarning())
         {
            return res.notificationCss().overviewMarkWarning();
         }

         // default
         return res.notificationCss().overviewMarkTask();
      }

      public void setTopPosition(int top, String unit)
      {
         getElement().getStyle().setTop(top, unit);
      }
   }

   /**
    * @param problems
    */
   public void addProblems(Marker[] problems)
   {
      JsoArray<CodeError> errors = JsoArray.create();
      for (Marker m : problems)
      {
         if (m.isError() || m.isWarning())
         {
            CodeErrorImpl error = CodeErrorImpl.make();
            error.setMessage(m.getMessage());
            error.setErrorEnd(getFilePosition(m.getEnd()));
            error.setErrorStart(getFilePosition(m.getStart()));
            error.setError(m.isError());
            errors.add(error);
         }
         addProblem(m);
      }
      errorListener.onErrorsChanged(errors);

   }

   /**
    * @param offset
    * @return
    */
   private FilePosition getFilePosition(int offset)
   {
      FilePositionImpl position = FilePositionImpl.make();
      int lineNumber;
      try
      {
         lineNumber = document.getLineOfOffset(offset);
         position.setLineNumber(lineNumber);
         position.setColumn(offset - document.getLineOffset(lineNumber));
      }
      catch (BadLocationException e)
      {
         e.printStackTrace();
      }
      return position;
   }

   /**
    * @param errorListener
    */
   public void setErrorListener(ErrorListener errorListener)
   {
      this.errorListener = errorListener;
   }
}
