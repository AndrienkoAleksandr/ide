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
package org.exoplatform.ide.client.project.create;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;

import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AsyncRequest;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.commons.rest.HTTPHeader;
import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.gwtframework.ui.client.api.ListGridItem;
import org.exoplatform.gwtframework.ui.client.dialog.Dialogs;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.framework.application.IDELoader;
import org.exoplatform.ide.client.framework.application.event.VfsChangedEvent;
import org.exoplatform.ide.client.framework.application.event.VfsChangedHandler;
import org.exoplatform.ide.client.framework.event.CreateProjectEvent;
import org.exoplatform.ide.client.framework.event.CreateProjectHandler;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedEvent;
import org.exoplatform.ide.client.framework.navigation.event.ItemsSelectedHandler;
import org.exoplatform.ide.client.framework.paas.DeployResultHandler;
import org.exoplatform.ide.client.framework.paas.PaaS;
import org.exoplatform.ide.client.framework.project.CreateModuleEvent;
import org.exoplatform.ide.client.framework.project.CreateModuleHandler;
import org.exoplatform.ide.client.framework.project.ProjectCreatedEvent;
import org.exoplatform.ide.client.framework.project.ProjectType;
import org.exoplatform.ide.client.framework.template.ProjectTemplate;
import org.exoplatform.ide.client.framework.template.TemplateService;
import org.exoplatform.ide.client.framework.template.marshal.ProjectTemplateListUnmarshaller;
import org.exoplatform.ide.client.framework.ui.api.IsView;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent;
import org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler;
import org.exoplatform.ide.client.framework.util.Utils;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.marshal.ChildrenUnmarshaller;
import org.exoplatform.ide.vfs.client.marshal.ProjectUnmarshaller;
import org.exoplatform.ide.vfs.client.model.ItemContext;
import org.exoplatform.ide.vfs.client.model.ItemWrapper;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.ItemType;
import org.exoplatform.ide.vfs.shared.PropertyImpl;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Jul 24, 2012 3:38:19 PM anya $
 *
 */
public class CreateProjectPresenter implements CreateProjectHandler, CreateModuleHandler, VfsChangedHandler, ViewClosedHandler,
   DeployResultHandler, ItemsSelectedHandler
{
   interface Display extends IsView
   {
      
      void switchToCreateModule();
      
      HasValue<String> getNameField();

      HasValue<String> getErrorLabel();

      void setProjectTypes(List<ProjectType> projectTypeList);

      void setTargets(List<PaaS> targetList);

      List<ToggleButton> getProjectTypeButtons();

      List<ToggleButton> getTargetButtons();

      /**
       * Returns {@link ProjectType} for the appropriate button.
       *
       * @param button {@link ToggleButton}
       * @return {@link ProjectType}
       */
      ProjectType getProjectTypeByButton(ToggleButton button);

      /**
       * Returns {@link PaaS} for the appropriate button.
       *
       * @param button {@link ToggleButton}
       * @return {@link PaaS}
       */
      PaaS getTargetByButton(ToggleButton button);

      void enableButtonsForSupportedTargets(List<PaaS> list);

      /**
       * Toggle up all buttons from the <code>buttonsList</code> except the <code>currentButton</code>.
       */
      void toggleUpAllButtons(List<ToggleButton> buttonsList, ToggleButton currentButton);

      void selectTarget(PaaS target);

      void setJRebelPanelVisibility(boolean isVisible);

      ListGridItem<ProjectTemplate> getTemplatesGrid();

      void selectTemplate(ProjectTemplate projectTemplate);

      HasValue<Boolean> getUseJRebelPlugin();

      HasClickHandlers getBackButton();

      HasClickHandlers getNextButton();

      HasClickHandlers getFinishButton();

      HasClickHandlers getCancelButton();

      HasValue<String> getJRebelFirstNameField();

      HasValue<String> getJRebelLastNameField();

      HasValue<String> getJRebelPhoneNumberField();

      void enableNextButton(boolean enabled);

      void enableFinishButton(boolean enabled);

      void showCreateProjectStep();

      void showChooseTemlateStep();

      void showDeployProjectStep();

      void setDeployView(Composite deployView);

      void setJRebelErrorFillingMessageLabel(String message);

      void setJRebelProfileFieldsVisible(boolean visible);
   }

   private Display display;

   private boolean isChooseTemplateStep;

   private boolean isDeployStep;

   private VirtualFileSystemInfo vfsInfo;

   private ProjectType selectedProjectType;

   private ProjectTemplate selectedTemplate;

   private List<ProjectTemplate> allProjectTemplates;

   private List<ProjectTemplate> availableProjectTemplates = new ArrayList<ProjectTemplate>();

   private PaaS currentPaaS;

   private PaaS selectedTarget;

   private final PaaS noneTarget = new NoneTarget();

   /**
    * Name of the property for using JRebel.
    */
   private static final String JREBEL = "jrebel";

   /**
    * Comparator for ordering project types.
    */
   private static final Comparator<ProjectType> PROJECT_TYPES_COMPARATOR = new ProjectTypesComparator();

   private static final Comparator<PaaS> PAAS_COMPARATOR = new PaaSComparator();
   
   private boolean createModule = false;
   
   private ProjectModel parentProject;   

   private class NoneTarget extends PaaS
   {
      public NoneTarget()
      {
         super("none", "None", new Image(IDEImageBundle.INSTANCE.noneTarget()), new Image(
            IDEImageBundle.INSTANCE.noneTarget()), new ArrayList<ProjectType>());
      }
   }

   public CreateProjectPresenter()
   {
      IDE.getInstance().addControl(new CreateProjectControl());
      IDE.getInstance().addControl(new CreateModuleControl());

      IDE.addHandler(CreateProjectEvent.TYPE, this);
      IDE.addHandler(CreateModuleEvent.TYPE, this);
      IDE.addHandler(VfsChangedEvent.TYPE, this);
      IDE.addHandler(ViewClosedEvent.TYPE, this);
      IDE.addHandler(ItemsSelectedEvent.TYPE, this);
   }

   public void bindDisplay()
   {
      display.getUseJRebelPlugin().setValue(true);
      display.getNameField().addValueChangeHandler(new ValueChangeHandler<String>()
      {

         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            updateNavigationButtonsState();
         }
      });

      display.getTemplatesGrid().addSelectionHandler(new SelectionHandler<ProjectTemplate>()
      {

         @Override
         public void onSelection(SelectionEvent<ProjectTemplate> event)
         {
            selectedTemplate = event.getSelectedItem();
            updateNavigationButtonsState();
         }
      });

      display.getBackButton().addClickHandler(new ClickHandler()
      {

         @Override
         public void onClick(ClickEvent event)
         {
            goBack();
         }
      });

      display.getNextButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            if (!isChooseTemplateStep && !isDeployStep)
            {
               if (selectedProjectType == null)
               {
                  Dialogs.getInstance().showInfo(org.exoplatform.ide.client.IDE.TEMPLATE_CONSTANT.noTechnologyTitle(),
                     org.exoplatform.ide.client.IDE.TEMPLATE_CONSTANT.noTechnologyMessage());
               }
               else
               {
                  validateProjectName(display.getNameField().getValue());
               }
            }
            else
            {
               if (display.getUseJRebelPlugin().getValue() && (selectedProjectType == ProjectType.JSP || selectedProjectType == ProjectType.SPRING))
               {
                  if (!checkJRebelFieldFill())
                  {
                     return;
                  }
               }
               goNext();
            }
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

      display.getFinishButton().addClickHandler(new ClickHandler()
      {

         @Override
         public void onClick(ClickEvent event)
         {
            if (display.getUseJRebelPlugin().getValue() && (selectedProjectType == ProjectType.JSP || selectedProjectType == ProjectType.SPRING))
            {
               if (!checkJRebelFieldFill())
               {
                  return;
               }
               sendProfileInfoToZeroTurnaround();
            }
            if (isDeployStep)
            {
               doDeploy((availableProjectTemplates.size() == 1) ? availableProjectTemplates.get(0) : selectedTemplate);
            }
            else if (isChooseTemplateStep)
            {
               createProject(selectedTemplate);
            }
            else if (selectedProjectType == null)
            {
               Dialogs.getInstance().showInfo(org.exoplatform.ide.client.IDE.TEMPLATE_CONSTANT.noTechnologyTitle(),
                  org.exoplatform.ide.client.IDE.TEMPLATE_CONSTANT.noTechnologyMessage());
            }
            else if (availableProjectTemplates.size() == 1)
            {
               createProject(availableProjectTemplates.get(0));
            }
            else
            {
               createProject(null);
            }
         }
      });

      display.getUseJRebelPlugin().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            if (event.getValue())
            {
               display.setJRebelProfileFieldsVisible(true);
            }
            else
            {
               display.setJRebelProfileFieldsVisible(false);
            }
         }
      });

      display.getJRebelFirstNameField().addValueChangeHandler(new ValueChangeHandler<String>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            checkJRebelFieldFill();
         }
      });

      display.getJRebelLastNameField().addValueChangeHandler(new ValueChangeHandler<String>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            checkJRebelFieldFill();
         }
      });

      display.getJRebelPhoneNumberField().addValueChangeHandler(new ValueChangeHandler<String>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            checkJRebelFieldFill();
         }
      });
   }

   /**
    * @see org.exoplatform.ide.client.framework.event.CreateProjectHandler#onCreateProject(org.exoplatform.ide.client.framework.event.CreateProjectEvent)
    */
   @Override
   public void onCreateProject(CreateProjectEvent event)
   {
      openCreateProjectView(false);      
   }
      
   @Override
   public void onCreateModule(CreateModuleEvent event)
   {
      if (MavenModuleCreationCallback.getInstance().isPomXMLOpened(parentProject))
      {
         Dialogs.getInstance().showError("First close pom.xml.");
         return;
      }
      
      openCreateProjectView(true);
   }
   
   private void openCreateProjectView(boolean createModule)
   {
      this.createModule = createModule;
      
      if (display == null)
      {
         display = GWT.create(Display.class);
         IDE.getInstance().openView(display.asView());
         if (createModule)
         {
            display.switchToCreateModule();
         }
         
         bindDisplay();
      }

      selectedProjectType = null;
      availableProjectTemplates.clear();

      display.showCreateProjectStep();
      isDeployStep = false;
      isChooseTemplateStep = false;
      getProjectTemplates();
      setTargets(IDE.getInstance().getPaaSes());
      updateNavigationButtonsState();      
   }

   /**
    * @see org.exoplatform.ide.client.framework.ui.api.event.ViewClosedHandler#onViewClosed(org.exoplatform.ide.client.framework.ui.api.event.ViewClosedEvent)
    */
   @Override
   public void onViewClosed(ViewClosedEvent event)
   {
      if (event.getView() instanceof Display)
      {
         display = null;
      }
   }

   /**
    * Update the enabled/disabled state of the navigation buttons.
    */
   private void updateNavigationButtonsState()
   {
      boolean firstStepIsOK =
         display.getNameField().getValue() != null && !display.getNameField().getValue().isEmpty()
            && selectedProjectType != null;
      boolean noneDeploy = (selectedTarget == null || selectedTarget instanceof NoneTarget);

      if (isChooseTemplateStep)
      {
         display.enableFinishButton(firstStepIsOK && noneDeploy && selectedTemplate != null);
         display.enableNextButton(firstStepIsOK && !noneDeploy && selectedTemplate != null);
      }
      else if (isDeployStep)
      {
         display.enableFinishButton(true);
      }
      else
      {
         display.enableFinishButton(false);
         display.enableNextButton(true);
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.application.event.VfsChangedHandler#onVfsChanged(org.exoplatform.ide.client.framework.application.event.VfsChangedEvent)
    */
   @Override
   public void onVfsChanged(VfsChangedEvent event)
   {
      this.vfsInfo = event.getVfsInfo();
   }
   
   /**
    * Get the list of available project templates.
    */
   private void getProjectTemplates()
   {
      try
      {
         TemplateService.getInstance().getProjectTemplateList(
            new AsyncRequestCallback<List<ProjectTemplate>>(new ProjectTemplateListUnmarshaller(
               new ArrayList<ProjectTemplate>()))
            {
               @Override
               protected void onSuccess(List<ProjectTemplate> templates)
               {                  
                  if (createModule)
                  {
                     allProjectTemplates = new ArrayList<ProjectTemplate>();
                     for (ProjectTemplate template : templates)
                     {
                        if (AvailableModluleTypes.contains(template.getType()))
                        {
                           allProjectTemplates.add(template);
                        }
                     }                     
                  }
                  else
                  {
                     allProjectTemplates = templates;
                  }
                  
                  List<ProjectType> list = getProjectTypesFromTemplates(allProjectTemplates);
                  setProjectTypes(list);
                  if (display.getNameField().getValue() == null || display.getNameField().getValue().isEmpty())
                  {
                     display.getNameField().setValue("untitled");
                  }
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  IDE.fireEvent(new ExceptionThrownEvent(exception));
               }

            });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   /**
    * Sets the available project types.
    *
    * @param list a list of the available project types
    */
   private void setProjectTypes(List<ProjectType> list)
   {
      Collections.sort(list, PROJECT_TYPES_COMPARATOR);
      display.setProjectTypes(list);

      for (final ToggleButton toggleButton : display.getProjectTypeButtons())
      {
         toggleButton.addValueChangeHandler(new ValueChangeHandler<Boolean>()
         {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event)
            {
               if (event.getValue())
               {
                  display.toggleUpAllButtons(display.getProjectTypeButtons(), toggleButton);
                  selectedProjectType = display.getProjectTypeByButton(toggleButton);

                  display.enableButtonsForSupportedTargets(getAvailableTargets(selectedProjectType));
                  display.selectTarget(noneTarget);
                  updateNavigationButtonsState();
               }
               else
               {
                  // do not allow toggle up
                  toggleButton.setDown(true);
               }
            }
         });
      }
   }

   /**
    * Sets the deployment targets.
    *
    * @param targetsList a list of the available deployment targets
    */
   private void setTargets(List<PaaS> targetsList)
   {
      List<PaaS> list = new ArrayList<PaaS>();
      list.addAll(targetsList);
      Collections.sort(list, PAAS_COMPARATOR);
      list.add(noneTarget);
      display.setTargets(list);

      for (final ToggleButton toggleButton : display.getTargetButtons())
      {
         toggleButton.addValueChangeHandler(new ValueChangeHandler<Boolean>()
         {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event)
            {
               if (event.getValue())
               {
                  display.toggleUpAllButtons(display.getTargetButtons(), toggleButton);
                  selectedTarget = display.getTargetByButton(toggleButton);
                  availableProjectTemplates = getProjectTemplates(selectedProjectType, selectedTarget);
                  updateNavigationButtonsState();
               }
               else
               {
                  // do not allow toggle up
                  toggleButton.setDown(true);
               }
            }
         });
      }
   }

   /**
    * Go to previous step.
    */
   private void goBack()
   {
      if (isDeployStep)
      {
         isDeployStep = false;
         goToTemplatesStep();
      }
      else if (isChooseTemplateStep)
      {
         isChooseTemplateStep = false;
         goToProjectStep();
      }
      updateNavigationButtonsState();
      display.setJRebelProfileFieldsVisible(display.getUseJRebelPlugin().getValue());
   }

   /**
    * Go to next step.
    */
   private void goNext()
   {
      if (isChooseTemplateStep)
      {
         isChooseTemplateStep = false;
         goToDeployStep();
      }
      else
      // create project step
      {
         goToTemplatesStep();
         if (!availableProjectTemplates.contains(selectedTemplate))
         {
            selectedTemplate = null;
         }
      }
      updateNavigationButtonsState();
      display.setJRebelProfileFieldsVisible(display.getUseJRebelPlugin().getValue());
   }

   /**
    * Move to project's data step.
    */
   private void goToProjectStep()
   {
      updateJRebelPanelVisibility();
      display.showCreateProjectStep();
   }

   /**
    * Move to choosing project template step.
    */
   private void goToTemplatesStep()
   {
      isChooseTemplateStep = true;
      display.getTemplatesGrid().setValue(availableProjectTemplates);
      updateJRebelPanelVisibility();
      display.showChooseTemlateStep();
   }

   /**
    * Move to deploy project step.
    */
   private void goToDeployStep()
   {
      isDeployStep = true;

      String projectName = display.getNameField().getValue();
      for (PaaS paas : IDE.getInstance().getPaaSes())
      {
         if (paas.getId().equals(selectedTarget.getId()))
         {
            currentPaaS = paas;
            if (paas.getPaaSActions() != null)
            {
               updateJRebelPanelVisibility();
               display.showDeployProjectStep();
               isDeployStep = true;
               display.setDeployView(paas.getPaaSActions().getDeployView(projectName, selectedProjectType));
               display.enableFinishButton(true);
            }
            else
            {
               Dialogs.getInstance().showError(
                  org.exoplatform.ide.client.IDE.TEMPLATE_CONSTANT.noRegistedDeployAction(paas.getTitle()));
            }
            return;
         }
      }
   }

   /**
    * Get the list of targets, where project with pointed project type can be deployed.
    *
    * @param projectType the project type
    * @return {@link List} of {@link PaaS}
    */
   private List<PaaS> getAvailableTargets(ProjectType projectType)
   {
      List<PaaS> values = new ArrayList<PaaS>();
      values.add(noneTarget);
      for (PaaS paas : IDE.getInstance().getPaaSes())
      {
         if (paas.getSupportedProjectTypes().contains(projectType))
         {
            values.add(paas);
         }
      }
      ;
      return values;
   }

   private void createProject(ProjectTemplate projectTemplate)
   {
      if (projectTemplate == null)
      {
         Dialogs.getInstance().showError(org.exoplatform.ide.client.IDE.TEMPLATE_CONSTANT.noProjectTempate());
         return;
      }

      if (vfsInfo == null || vfsInfo.getRoot() == null)
      {
         Dialogs.getInstance().showError(
            org.exoplatform.ide.client.IDE.ERRORS_CONSTANT.createProjectErrorVFSInfoNotSets());
         return;
      }

      try
      {
         String parentId = vfsInfo.getRoot().getId();

         if (createModule && parentProject != null)
         {
            parentId = parentProject.getId();
         }
         
         String projectName = display.getNameField().getValue();
         
         IDELoader.getInstance().setMessage(org.exoplatform.ide.client.IDE.TEMPLATE_CONSTANT.creatingProject());
         IDELoader.getInstance().show();
         TemplateService.getInstance().createProjectFromTemplate(vfsInfo.getId(), parentId, projectName,
            projectTemplate.getName(),
            new AsyncRequestCallback<ProjectModel>(new ProjectUnmarshaller(new ProjectModel()))
            {
               @Override
               protected void onSuccess(final ProjectModel result)
               {
                  if ((selectedProjectType == ProjectType.JSP || selectedProjectType == ProjectType.SPRING)
                     && display.getUseJRebelPlugin().getValue() == true)
                  {
                     writeUseJRebelProperty(result);
                  }

                  IDELoader.getInstance().hide();
                  IDE.getInstance().closeView(display.asView().getId());
                  
                  if (createModule)
                  {
                     MavenModuleCreationCallback.getInstance().moduleCreated(parentProject, result);
                  }
                  else
                  {                     
                     IDE.fireEvent(new ProjectCreatedEvent(result));
                  }
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  IDELoader.getInstance().hide();
                  IDE.fireEvent(new ExceptionThrownEvent(exception));
               }
            });
      }
      catch (RequestException e)
      {
         IDELoader.getInstance().hide();
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   /**
    * Writes 'jrebel' property to the project properties.
    *
    * @param project {@link ProjectModel}
    */
   private void writeUseJRebelProperty(ProjectModel project)
   {
      project.getProperties().add(new PropertyImpl(JREBEL, "true"));
      try
      {
         VirtualFileSystem.getInstance().updateItem(project, null, new AsyncRequestCallback<ItemWrapper>()
         {

            @Override
            protected void onSuccess(ItemWrapper result)
            {
               // nothing to do
            }

            @Override
            protected void onFailure(Throwable ignore)
            {
               // ignore this exception
            }
         });
      }
      catch (RequestException e)
      {
         // ignore this exception
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.paas.DeployResultHandler#onDeployFinished(boolean)
    */
   @Override
   public void onDeployFinished(boolean success)
   {
      if (success && display != null)
      {
         IDE.getInstance().closeView(display.asView().getId());
      }
   }

   private void doDeploy(ProjectTemplate projectTemplate)
   {
      if (currentPaaS != null)
      {
         if (projectTemplate != null || currentPaaS.isProvidesTemplate())
         {
            currentPaaS.getPaaSActions().deploy(projectTemplate, this);
         }
         else
         {
            Dialogs.getInstance().showError(
               org.exoplatform.ide.client.IDE.TEMPLATE_CONSTANT.noProjectTemplateForTarget(currentPaaS.getTitle()));
         }
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.paas.DeployResultHandler#onProjectCreated(org.exoplatform.ide.vfs.client.model.ProjectModel)
    */
   @Override
   public void onProjectCreated(ProjectModel project)
   {
      if ((selectedProjectType == ProjectType.JSP || selectedProjectType == ProjectType.SPRING)
         && display.getUseJRebelPlugin().getValue() == true)
      {
         writeUseJRebelProperty(project);
      }

      IDE.fireEvent(new ProjectCreatedEvent(project));
      if (display != null)
      {
         IDE.getInstance().closeView(display.asView().getId());
      }
   }

   /**
    * Prepare project type list to be displayed.
    *
    * @param projectTemplates available project templates
    * @return {@link List}
    */
   private List<ProjectType> getProjectTypesFromTemplates(List<ProjectTemplate> projectTemplates)
   {
      List<ProjectType> projectTypes = new ArrayList<ProjectType>();
      for (ProjectTemplate projectTemplate : projectTemplates)
      {
         ProjectType projectType = ProjectType.fromValue(projectTemplate.getType());
         if (!projectTypes.contains(projectType))
         {
            projectTypes.add(projectType);
         }
      }
      return projectTypes;
   }

   /**
    * Get the list of project templates, that are suitable to pointed project type and deploy target.
    *
    * @param projectType project's type
    * @param target deploy target
    * @return {@link List} list of {@link ProjectTemplate}
    */
   private List<ProjectTemplate> getProjectTemplates(ProjectType projectType, PaaS target)
   {
      List<ProjectTemplate> templates = new ArrayList<ProjectTemplate>();

      // Get templates by project's type:
      if (target instanceof NoneTarget)
      {
         for (ProjectTemplate projectTemplate : allProjectTemplates)
         {
            if (projectTemplate.getType().equals(projectType.value()))
            {
               templates.add(projectTemplate);
            }
         }
         return templates;
      }

      // Get templates by project type and it's deploy target:
      for (ProjectTemplate projectTemplate : allProjectTemplates)
      {
         if (projectTemplate.getType().equals(projectType.value())
            && (projectTemplate.getTargets() == null || projectTemplate.getTargets().contains(target.getId())))
         {
            templates.add(projectTemplate);
         }
      }
      return templates;
   }

   /**
    * Validates project name for existence.
    *
    * @param projectName project's name
    */
   private void validateProjectName(final String projectName)
   {
      try
      {
         Folder parent = VirtualFileSystem.getInstance().getInfo().getRoot();
         if (createModule)
         {
            parent = parentProject;
         }

         VirtualFileSystem.getInstance().getChildren(parent,
            ItemType.PROJECT, new AsyncRequestCallback<List<Item>>(new ChildrenUnmarshaller(new ArrayList<Item>()))
         {
            @Override
            protected void onSuccess(List<Item> result)
            {
               for (Item item : result)
               {
                  if (projectName.equals(item.getName()))
                  {
                     display.getErrorLabel().setValue(
                        org.exoplatform.ide.client.IDE.TEMPLATE_CONSTANT
                           .createProjectFromTemplateProjectExists(projectName));
                     return;
                  }
               }
               display.getErrorLabel().setValue("");
               goNext();
            }

            @Override
            protected void onFailure(Throwable exception)
            {
               IDE.fireEvent(new ExceptionThrownEvent(exception, "Searching of projects failed."));
            }
         });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e, "Searching of projects failed."));
      }

   }

   /**
    * Set the visibility state of a panel with JRebel setting.
    */
   private void updateJRebelPanelVisibility()
   {
      boolean visible =
         (isChooseTemplateStep && (selectedProjectType == ProjectType.JSP || selectedProjectType == ProjectType.SPRING));
      display.setJRebelPanelVisibility(visible);
   }

   private void sendProfileInfoToZeroTurnaround()
   {
      String url = Utils.getRestContext() + "/ide/jrebel/profile/info";

      JSONObject json = new JSONObject();
      json.put("first_name", new JSONString(display.getJRebelFirstNameField().getValue()));
      json.put("last_name", new JSONString(display.getJRebelLastNameField().getValue()));
      json.put("phone", new JSONString(display.getJRebelPhoneNumberField().getValue()));

      try
      {
         AsyncRequest.build(RequestBuilder.POST, url)
            .header(HTTPHeader.CONTENTTYPE, MimeType.APPLICATION_JSON)
            .data(json.toString())
            .send(new AsyncRequestCallback<Void>()
            {
               @Override
               protected void onSuccess(Void result)
               {
                  //success
               }

               @Override
               protected void onFailure(Throwable exception)
               {
               }
            });
      }
      catch (RequestException e)
      {
         IDE.fireEvent(new ExceptionThrownEvent(e));
      }
   }

   private boolean checkJRebelFieldFill()
   {
      if (display.getUseJRebelPlugin().getValue())
      {
         if (!display.getJRebelFirstNameField().getValue().isEmpty()
            && !display.getJRebelLastNameField().getValue().isEmpty()
            && !display.getJRebelPhoneNumberField().getValue().isEmpty())
         {
            String phone = display.getJRebelPhoneNumberField().getValue();

            boolean phoneMatched = phone.matches("\\+\\d{2}\\s?-?\\s?[(]?\\d{3}[)]?\\s?-?\\s?\\d{3}\\s?-?\\s?\\d{4}");
            if (!phoneMatched)
            {
               display.setJRebelErrorFillingMessageLabel("Phone must be: +xx-(xxx)-xxxxxxx");
            }
            else
            {
               display.setJRebelErrorFillingMessageLabel("");
            }
            return phoneMatched;
         }
         display.setJRebelErrorFillingMessageLabel("All field are required!");
      }
      return false;
   }
   
   @Override
   public void onItemsSelected(ItemsSelectedEvent event)
   {
      if (event.getSelectedItems() == null || 
               event.getSelectedItems().size() != 1 || 
               !(event.getSelectedItems().get(0) instanceof ItemContext))
      {
         parentProject = null;
         return;
      }
      
      ItemContext context = (ItemContext)event.getSelectedItems().get(0);
      parentProject = context.getProject();
   }

}
