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

package org.exoplatform.ide.texteditor;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import elemental.html.Element;

import org.exoplatform.ide.AppContext;
import org.exoplatform.ide.json.JsonArray;
import org.exoplatform.ide.json.JsonCollections;
import org.exoplatform.ide.mvp.CompositeView;
import org.exoplatform.ide.mvp.UiComponent;
import org.exoplatform.ide.text.DocumentImpl;
import org.exoplatform.ide.text.Document;
import org.exoplatform.ide.text.store.DocumentModel;
import org.exoplatform.ide.text.store.LineInfo;
import org.exoplatform.ide.text.store.TextStoreMutator;
import org.exoplatform.ide.texteditor.Buffer.ScrollListener;
import org.exoplatform.ide.texteditor.api.BeforeTextListener;
import org.exoplatform.ide.texteditor.api.KeyListener;
import org.exoplatform.ide.texteditor.api.NativeKeyUpListener;
import org.exoplatform.ide.texteditor.api.TextEditorConfiguration;
import org.exoplatform.ide.texteditor.api.TextEditorPartDisplay;
import org.exoplatform.ide.texteditor.api.TextListener;
import org.exoplatform.ide.texteditor.gutter.Gutter;
import org.exoplatform.ide.texteditor.gutter.LeftGutterManager;
import org.exoplatform.ide.texteditor.input.InputController;
import org.exoplatform.ide.texteditor.linedimensions.LineDimensionsCalculator;
import org.exoplatform.ide.texteditor.linedimensions.LineDimensionsUtils;
import org.exoplatform.ide.texteditor.renderer.CurrentLineHighlighter;
import org.exoplatform.ide.texteditor.renderer.LineRenderer;
import org.exoplatform.ide.texteditor.renderer.RenderTimeExecutor;
import org.exoplatform.ide.texteditor.renderer.Renderer;
import org.exoplatform.ide.texteditor.selection.CursorView;
import org.exoplatform.ide.texteditor.selection.LocalCursorController;
import org.exoplatform.ide.texteditor.selection.SelectionManager;
import org.exoplatform.ide.texteditor.selection.SelectionModel;
import org.exoplatform.ide.util.CssUtils;
import org.exoplatform.ide.util.Elements;
import org.exoplatform.ide.util.ListenerManager;
import org.exoplatform.ide.util.ListenerManager.Dispatcher;
import org.exoplatform.ide.util.ListenerRegistrar;
import org.exoplatform.ide.util.dom.FontDimensionsCalculator;
import org.exoplatform.ide.util.dom.FontDimensionsCalculator.FontDimensions;
import org.exoplatform.ide.texteditor.selection.SelectionLineRenderer;

/**
 * The presenter for the editor.
 *
 *  This class composes many of the other classes that together form the editor.
 * For example, the area where the text is displayed, the {@link Buffer}, is a
 * nested presenter. Other components are not presenters, such as the input
 * mechanism which is handled by the {@link InputController}.
 *
 *  If an added element wants native browser selection, you must not inherit the
 * "user-select" CSS property. See
 * {@link CssUtils#setUserSelect(Element, boolean)}.
 */
public class Editor extends UiComponent<Editor.View> implements TextEditorPartDisplay
{

   /**
    * Animation CSS.
    */
   @CssResource.Shared
   public interface EditorSharedCss extends CssResource
   {
      String animationEnabled();

      String scrollable();
   }

   /**
    * CssResource for the editor.
    */
   public interface Css extends EditorSharedCss
   {
      String leftGutter();

      String leftGutterNotification();

      String editorFont();

      String root();

      String scrolled();

      String gutter();

      String lineRendererError();

      String leftGutterBase();

      String lineWarning();
   }

   /**
    * ClientBundle for the editor.
    */
   public interface Resources extends Buffer.Resources, CursorView.Resources, SelectionLineRenderer.Resources
   {
      @Source({"Editor.css", "constants.css"})
      Css workspaceEditorCss();

      @Source("squiggle.gif")
      ImageResource squiggle();

      @Source("squiggle-warning.png")
      ImageResource squiggleWarning();
   }

   /**
    * A listener that is called when the editor becomes or is no longer
    * read-only.
    */
   public interface ReadOnlyListener
   {
      void onReadOnlyChanged(boolean isReadOnly);
   }

   /**
    * The view for the editor, containing gutters and the buffer. This exposes
    * only the ability to enable or disable animations.
    */
   public static class View extends CompositeView<Void>
   {
      private final Element bufferElement;

      final Css css;

      final Resources res;

      private View(Resources res, Element bufferElement, Element inputElement)
      {

         this.res = res;
         this.bufferElement = bufferElement;
         this.css = res.workspaceEditorCss();

         Element rootElement = Elements.createDivElement(css.root());
         rootElement.appendChild(bufferElement);
         rootElement.appendChild(inputElement);
         setElement(rootElement);
      }

      private void addGutter(Element gutterElement)
      {
         getElement().insertBefore(gutterElement, bufferElement);
      }

      private void removeGutter(Element gutterElement)
      {
         getElement().removeChild(gutterElement);
      }

      public void setAnimationEnabled(boolean enabled)
      {
         // TODO: Re-enable animations when they are stable.
         if (enabled)
         {
            // getElement().addClassName(css.animationEnabled());
         }
         else
         {
            // getElement().removeClassName(css.animationEnabled());
         }
      }

      public Resources getResources()
      {
         return res;
      }
   }

   public static final int ANIMATION_DURATION = 100;

   private static int idCounter = 0;

   private final AppContext appContext;

   private final Buffer buffer;

   private DocumentModel textStore;

   private final EditorTextStoreMutator editorDocumentMutator;

   private final FontDimensionsCalculator editorFontDimensionsCalculator;

   private UndoManager editorUndoManager;

   private final FocusManager focusManager;

   private final MouseHoverManager mouseHoverManager;

   private final int id = idCounter++;

   private final FontDimensionsCalculator.Callback fontDimensionsChangedCallback =
      new FontDimensionsCalculator.Callback()
      {
         @Override
         public void onFontDimensionsChanged(FontDimensions fontDimensions)
         {
            handleFontDimensionsChanged();
         }
      };

   private final JsonArray<Gutter> gutters = JsonCollections.createArray();

   private final InputController input;

   private final LeftGutterManager leftGutterManager;

   private LocalCursorController localCursorController;

   private final ListenerManager<ReadOnlyListener> readOnlyListenerManager = ListenerManager.create();

   private Renderer renderer;

   //  private SearchModel searchModel;
   private SelectionManager selectionManager;

   private final EditorActivityManager editorActivityManager;

   private ViewportModel viewport;

   private boolean isReadOnly;

   private final RenderTimeExecutor renderTimeExecutor;

   private Document document;

   public Editor(AppContext appContext)
   {
      this.appContext = appContext;
      editorFontDimensionsCalculator =
         FontDimensionsCalculator.get(appContext.getResources().workspaceEditorCss().editorFont());
      renderTimeExecutor = new RenderTimeExecutor();
      LineDimensionsCalculator lineDimensions = LineDimensionsCalculator.create(editorFontDimensionsCalculator);

      buffer =
         Buffer.create(appContext, editorFontDimensionsCalculator.getFontDimensions(), lineDimensions,
            renderTimeExecutor);
      input = new InputController();
      View view = new View(appContext.getResources(), buffer.getView().getElement(), input.getInputElement());
      setView(view);

      focusManager = new FocusManager(buffer, input.getInputElement());

      Gutter leftGutter =
         createGutter(false, Gutter.Position.LEFT, appContext.getResources().workspaceEditorCss().leftGutter());
      leftGutterManager = new LeftGutterManager(leftGutter, buffer);

      editorDocumentMutator = new EditorTextStoreMutator(this);
      mouseHoverManager = new MouseHoverManager(this);

      editorActivityManager =
         new EditorActivityManager(appContext.getUserActivityManager(), buffer.getScrollListenerRegistrar(),
            getKeyListenerRegistrar());

      // TODO: instantiate input from here
      input.initializeFromEditor(this, editorDocumentMutator);

      setAnimationEnabled(true);
      addBoxShadowOnScrollHandler();
      editorFontDimensionsCalculator.addCallback(fontDimensionsChangedCallback);
   }

   private void handleFontDimensionsChanged()
   {
      buffer.repositionAnchoredElementsWithColumn();
      if (renderer != null)
      {
         /*
          * TODO: think about a scheme where we don't have to rerender
          * the whole viewport (currently we do because of the right-side gap
          * fillers)
          */
         renderer.renderAll();
      }
   }

   /**
    * Adds a scroll handler to the buffer scrollableElement so that a drop shadow
    * can be added and removed when scrolled.
    */
   private void addBoxShadowOnScrollHandler()
   {
      if (true)
      {
         // TODO: investigate why this kills performance
         return;
      }

      this.buffer.getScrollListenerRegistrar().add(new ScrollListener()
      {

         @Override
         public void onScroll(Buffer buffer, int scrollTop)
         {
            if (scrollTop < 20)
            {
               getElement().removeClassName(getView().css.scrolled());
            }
            else
            {
               getElement().addClassName(getView().css.scrolled());
            }
         }
      });
   }

   /**
    * @see org.exoplatform.ide.texteditor.api.TextEditorPartDisplay#addLineRenderer(org.exoplatform.ide.texteditor.renderer.LineRenderer)
    */
   @Override
   public void addLineRenderer(LineRenderer lineRenderer)
   {
      /*
       * TODO: Because the line renderer is document-scoped, line
       * renderers have to re-add themselves whenever the document changes. This
       * is unexpected.
       */
      renderer.addLineRenderer(lineRenderer);
   }

   public Gutter createGutter(boolean overviewMode, Gutter.Position position, String cssClassName)
   {
      Gutter gutter = Gutter.create(overviewMode, position, cssClassName, buffer);
      if (viewport != null && renderer != null)
      {
         gutter.handleDocumentChanged(viewport, renderer);
      }

      gutters.add(gutter);

      gutter.getGutterElement().addClassName(getView().css.gutter());
      getView().addGutter(gutter.getGutterElement());
      return gutter;
   }

   public void removeGutter(Gutter gutter)
   {
      getView().removeGutter(gutter.getGutterElement());
      gutters.remove(gutter);
   }

   public void setAnimationEnabled(boolean enabled)
   {
      getView().setAnimationEnabled(enabled);
   }

   /**
    * @see org.exoplatform.ide.texteditor.api.TextEditorPartDisplay#getBeforeTextListenerRegistrar()
    */
   @Override
   public ListenerRegistrar<BeforeTextListener> getBeforeTextListenerRegistrar()
   {
      return editorDocumentMutator.getBeforeTextListenerRegistrar();
   }

   public Buffer getBuffer()
   {
      return buffer;
   }

   /*
    * TODO: if left gutter manager gets public API, expose that
    * instead of directly exposign the gutter. Or, if we don't want to expose
    * Gutter#setWidth publicly for the left gutter, make LeftGutterManager the
    * public API.
    */
   public Gutter getLeftGutter()
   {
      return leftGutterManager.getGutter();
   }

   public DocumentModel getTextStore()
   {
      return textStore;
   }

   /**
    * @see org.exoplatform.ide.texteditor.api.TextEditorPartDisplay#getEditorDocumentMutator()
    */
   @Override
   public TextStoreMutator getEditorDocumentMutator()
   {
      return editorDocumentMutator;
   }

   /**
    * @see org.exoplatform.ide.texteditor.api.TextEditorPartDisplay#getElement()
    */
   @Override
   public com.google.gwt.user.client.Element getElement()
   {
      return (com.google.gwt.user.client.Element)getView().getElement();
   }

   /**
    * @see org.exoplatform.ide.texteditor.api.TextEditorPartDisplay#getFocusManager()
    */
   @Override
   public FocusManager getFocusManager()
   {
      return focusManager;
   }

   public MouseHoverManager getMouseHoverManager()
   {
      return mouseHoverManager;
   }

   /**
    * @see org.exoplatform.ide.texteditor.api.TextEditorPartDisplay#getKeyListenerRegistrar()
    */
   @Override
   public ListenerRegistrar<KeyListener> getKeyListenerRegistrar()
   {
      return input.getKeyListenerRegistrar();
   }

   public ListenerRegistrar<NativeKeyUpListener> getNativeKeyUpListenerRegistrar()
   {
      return input.getNativeKeyUpListenerRegistrar();
   }

   /**
    * @see org.exoplatform.ide.texteditor.api.TextEditorPartDisplay#getRenderer()
    */
   @Override
   public Renderer getRenderer()
   {
      return renderer;
   }

   /**
    * @see org.exoplatform.ide.texteditor.api.TextEditorPartDisplay#getSelection()
    */
   @Override
   public SelectionModel getSelection()
   {
      return selectionManager.getSelectionModel();
   }

   public LocalCursorController getCursorController()
   {
      return localCursorController;
   }

   public ListenerRegistrar<TextListener> getTextListenerRegistrar()
   {
      return editorDocumentMutator.getTextListenerRegistrar();
   }

   // TODO: need a public interface and impl
   public ViewportModel getViewport()
   {
      return viewport;
   }

   public void removeLineRenderer(LineRenderer lineRenderer)
   {
      renderer.removeLineRenderer(lineRenderer);
   }

   /**
    * @see org.exoplatform.ide.texteditor.api.TextEditorPartDisplay#setDocument(org.exoplatform.ide.text.DocumentImpl)
    */
   @Override
   public void setDocument(final DocumentImpl document)
   {
      this.document = document;
      textStore = document.getTextStore();

      /*
       * TODO: dig into each component, figure out dependencies,
       * break apart components so we can reduce circular dependencies which
       * require the multiple stages of initialization
       */
      // Core editor components
      buffer.handleDocumentChanged(textStore);
      leftGutterManager.handleDocumentChanged(textStore);

      selectionManager = SelectionManager.create(textStore, buffer, focusManager, appContext.getResources());

      SelectionModel selection = selectionManager.getSelectionModel();
      viewport = ViewportModel.create(textStore, selection, buffer);
      input.handleDocumentChanged(textStore, selection, viewport);
      renderer =
         Renderer.create(textStore, viewport, buffer, getLeftGutter(), selection, focusManager, this,
            appContext.getResources(), renderTimeExecutor);
      if (editorUndoManager != null)
         editorUndoManager.connect(this);

      // Delayed core editor component initialization
      viewport.initialize();
      selection.initialize(viewport);
      selectionManager.initialize(renderer);
      buffer.handleComponentsInitialized(viewport, renderer);
      for (int i = 0, n = gutters.size(); i < n; i++)
      {
         gutters.get(i).handleDocumentChanged(viewport, renderer);
      }

      //    // Non-core editor components
      //    editorUndoManager = EditorUndoManager.create(this, document, selection);
      //    searchModel = SearchModel.create(appContext,
      //        document,
      //        renderer,
      //        viewport,
      //        selection,
      //        editorDocumentMutator);
      localCursorController = LocalCursorController.create(appContext, focusManager, selection, buffer, this);

      new CurrentLineHighlighter(buffer, selection, appContext.getResources());

   }

   /**
    * @see org.exoplatform.ide.texteditor.api.TextEditorPartDisplay#getDocument()
    */
   @Override
   public Document getDocument()
   {
      return document;
   }

   public void undo()
   {
      editorUndoManager.undo();
   }

   public void redo()
   {
      editorUndoManager.redo();
   }

   public void scrollTo(int lineNumber, int column)
   {
      if (textStore != null)
      {
         LineInfo lineInfo = textStore.getLineFinder().findLine(lineNumber);
         /*
          * TODO: the cursor will be the last line in the viewport,
          * fix this
          */
         SelectionModel selectionModel = getSelection();
         selectionModel.deselect();
         selectionModel.setCursorPosition(lineInfo, column);
      }
   }

   public void cleanup()
   {
      editorFontDimensionsCalculator.removeCallback(fontDimensionsChangedCallback);
      editorActivityManager.teardown();
   }

   public void setReadOnly(final boolean isReadOnly)
   {

      if (this.isReadOnly == isReadOnly)
      {
         return;
      }

      this.isReadOnly = isReadOnly;

      readOnlyListenerManager.dispatch(new Dispatcher<Editor.ReadOnlyListener>()
      {
         @Override
         public void dispatch(ReadOnlyListener listener)
         {
            listener.onReadOnlyChanged(isReadOnly);
         }
      });
   }

   public boolean isReadOnly()
   {
      return isReadOnly;
   }

   public ListenerRegistrar<ReadOnlyListener> getReadOnlyListenerRegistrar()
   {
      return readOnlyListenerManager;
   }

   public int getId()
   {
      return id;
   }

   public InputController getInput()
   {
      return input;
   }

   public void setLeftGutterVisible(boolean visible)
   {
      Element gutterElement = leftGutterManager.getGutter().getGutterElement();
      if (visible)
      {
         getView().addGutter(gutterElement);
      }
      else
      {
         getView().removeGutter(gutterElement);
      }
   }

   /**
    * @see org.exoplatform.ide.texteditor.api.TextEditorPartDisplay#setUndoManager(org.exoplatform.ide.texteditor.UndoManager)
    */
   @Override
   public void setUndoManager(UndoManager undoManager)
   {
      this.editorUndoManager = undoManager;
   }

   /**
    * @return the editorUndoManager
    */
   public UndoManager getUndoManager()
   {
      return editorUndoManager;
   }

   /**
    * @see org.exoplatform.ide.texteditor.api.TextEditorPartDisplay#configure(org.exoplatform.ide.texteditor.api.TextEditorConfiguration)
    */
   @Override
   public void configure(TextEditorConfiguration configuration)
   {
      setUndoManager(configuration.getUndoManager(this));
      LineDimensionsUtils.setTabSpaceEquivalence(configuration.getTabWidth(this));
   }

}