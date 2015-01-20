/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.wizard.project.main;

import com.codenvy.api.project.shared.dto.ProjectTemplateDescriptor;
import com.codenvy.api.project.shared.dto.ProjectTypeDefinition;
import com.codenvy.ide.Resources;
import com.codenvy.ide.api.icon.Icon;
import com.codenvy.ide.api.icon.IconRegistry;
import com.codenvy.ide.ui.Styles;
import com.codenvy.ide.ui.list.CategoriesList;
import com.codenvy.ide.ui.list.Category;
import com.codenvy.ide.ui.list.CategoryRenderer;
import com.codenvy.ide.wizard.project.ProjectWizardResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Evgen Vidolob
 * @author Oleksii Orel
 */
public class MainPageViewImpl implements MainPageView {

    private static MainPageViewImplUiBinder ourUiBinder = GWT.create(MainPageViewImplUiBinder.class);
    private final DockLayoutPanel rootElement;
    private final Category.CategoryEventDelegate<ProjectTemplateDescriptor> projectTemplateDelegate =
            new Category.CategoryEventDelegate<ProjectTemplateDescriptor>() {
                @Override
                public void onListItemClicked(Element listItemBase, ProjectTemplateDescriptor itemData) {
                    selectNextWizardType(itemData);
                }
            };
    private final Category.CategoryEventDelegate<ProjectTypeDefinition>     projectTypeDelegate     =
            new Category.CategoryEventDelegate<ProjectTypeDefinition>() {
                @Override
                public void onListItemClicked(Element listItemBase, ProjectTypeDefinition itemData) {
                    selectNextWizardType(itemData);
                }
            };
    private final CategoryRenderer<ProjectTypeDefinition>                            projectTypeRenderer     =
            new CategoryRenderer<ProjectTypeDefinition>() {
                @Override
                public void renderElement(com.google.gwt.dom.client.Element element, ProjectTypeDefinition data) {
                    element.setInnerText(data.getDisplayName());
                }

                @Override
                public com.google.gwt.dom.client.SpanElement renderCategory(Category<ProjectTypeDefinition> category) {
                    return renderCategoryWithIcon(category.getTitle());
                }
            };
    private final CategoryRenderer<ProjectTemplateDescriptor>               projectTemplateRenderer =
            new CategoryRenderer<ProjectTemplateDescriptor>() {
                @Override
                public void renderElement(com.google.gwt.dom.client.Element element, ProjectTemplateDescriptor data) {
                    element.setInnerText(data.getDisplayName());
                }

                @Override
                public com.google.gwt.dom.client.SpanElement renderCategory(Category<ProjectTemplateDescriptor> category) {
                    return renderCategoryWithIcon(category.getTitle());
                }
            };
    private final IconRegistry iconRegistry;

    @UiField(provided = true)
    Style       style;
    @UiField
    SimplePanel categoriesPanel;
    @UiField
    HTMLPanel   descriptionArea;
    @UiField
    Label       configurationAreaText;
    @UiField
    HTMLPanel   configurationArea;
    @UiField
    Label       projectType;
    @UiField
    TextBox     projectName;
    @UiField
    TextArea    projectDescription;
    @UiField
    RadioButton projectPrivate;
    @UiField
    RadioButton projectPublic;

    private ActionDelegate                              delegate;
    private Map<String, Set<ProjectTypeDefinition>>              categories;
    private Map<String, Set<ProjectTemplateDescriptor>> samples;
    private Resources                                   resources;
    private CategoriesList                              list;
    private List<ProjectTypeDefinition>                          availableProjectTypes;

    @Inject
    public MainPageViewImpl(Resources resources, IconRegistry iconRegistry, ProjectWizardResources wizardResources) {
        this.resources = resources;
        style = wizardResources.mainPageStyle();
        style.ensureInjected();
        this.iconRegistry = iconRegistry;
        rootElement = ourUiBinder.createAndBindUi(this);
        reset();
        projectName.getElement().setAttribute("placeholder", "Define the name of your project...");
        projectName.getElement().setAttribute("maxlength", "128");
        projectName.getElement().setAttribute("spellcheck", "false");
        projectDescription.getElement().setAttribute("placeholder", "Add a description to your project...");
        projectDescription.getElement().setAttribute("maxlength", "256");
        setConfigOptions(null);
    }

    @UiHandler("projectName")
    void onProjectNameChanged(KeyUpEvent event) {
        if (projectName.getValue() != null && projectName.getValue().indexOf(" ") >= 0) {
            String tmp = projectName.getValue();
            while (tmp.indexOf(" ") >= 0) {
                tmp = tmp.replaceAll(" ", "-");
            }
            projectName.setValue(tmp);
        }

        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            return;
        }

        delegate.projectNameChanged(projectName.getText());
    }

    @UiHandler("projectDescription")
    void onProjectDescriptionChanged(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            return;
        }

        delegate.projectDescriptionChanged(projectDescription.getValue());
    }

    @UiHandler({"projectPublic", "projectPrivate"})
    void visibilityHandler(ValueChangeEvent<Boolean> event) {
        delegate.projectVisibilityChanged(projectPublic.getValue());
    }

    private void selectNextWizardType(Object itemData) {
        if (itemData instanceof ProjectTemplateDescriptor) {
            delegate.projectTemplateSelected((ProjectTemplateDescriptor)itemData);
            descriptionArea.getElement().setInnerText(((ProjectTemplateDescriptor)itemData).getDescription());
            projectType.setText(((ProjectTemplateDescriptor)itemData).getDisplayName());
        } else if (itemData instanceof ProjectTypeDefinition) {
            delegate.projectTypeSelected((ProjectTypeDefinition)itemData);
            descriptionArea.getElement().setInnerText(((ProjectTypeDefinition)itemData).getDisplayName());
            projectType.setText(((ProjectTypeDefinition)itemData).getDisplayName());
        } else {
            descriptionArea.getElement().setInnerText("");
            resetConfigOptions();
            projectType.setText("");
        }
    }

    private void resetConfigOptions() {
        configurationArea.getElement().setInnerText("");
    }

    private void changeEnabledState(boolean enabled) {
        projectName.setEnabled(enabled);
        if (enabled) {
            projectName.setFocus(true);
        }
        changeEnabledStateAll(enabled);
    }

    private void changeEnabledStateAll(boolean enabled) {
        projectDescription.setEnabled(enabled);
        projectPublic.setEnabled(enabled);
        projectPrivate.setEnabled(enabled);
    }

    private SpanElement renderCategoryWithIcon(String category) {
        SpanElement textElement = Document.get().createSpanElement();
        textElement.setClassName(resources.defaultCategoriesListCss().headerText());
        textElement.setInnerText(category.toUpperCase());
        Icon icon = iconRegistry.getIconIfExist(category + ".samples.category.icon");
        if (icon != null) {
            Element iconElement = null;
            if (icon.getSVGImage() != null) {
                iconElement = icon.getSVGImage().getElement();
                iconElement.setAttribute("class", resources.defaultCategoriesListCss().headerIcon());
            } else if (icon.getImage() != null) {
                iconElement = icon.getImage().getElement();
                iconElement.setClassName(resources.defaultCategoriesListCss().headerIcon());
            }
            if (iconElement != null) {
                SpanElement spanElement = Document.get().createSpanElement();
                spanElement.appendChild(iconElement);
                spanElement.appendChild(textElement);
                return spanElement;
            }
        }
        return textElement;
    }

    @Override
    public void setConfigOptions(List<String> options) {
        StringBuilder optionsHTMLBuilder = new StringBuilder();
        if (options != null) {
            for (String option : options) {
                if (option != null && option.length() > 0) {
                    optionsHTMLBuilder.append("<p>");
                    optionsHTMLBuilder.append("- " + option);
                    optionsHTMLBuilder.append("</p>\n");
                }
            }
        }
        if (optionsHTMLBuilder.length() > 0) {
            configurationArea.getElement().setInnerHTML(optionsHTMLBuilder.toString());
            configurationAreaText.setVisible(true);
            configurationArea.setVisible(true);
        } else {
            configurationAreaText.setVisible(false);
            configurationArea.setVisible(false);
            configurationArea.getElement().setInnerText("");
        }
    }

    @Override
    public void resetName() {
        projectName.setText("");
        projectDescription.setText("");
        projectPublic.setValue(true);
        projectPrivate.setValue(false);
        changeEnabledState(true);
    }

    @Override
    public void setName(String name) {
        projectName.setValue(name, true);
    }

    /** {@inheritDoc} */
    @Override
    public void setDescription(String description) {
        projectDescription.setValue(description);
    }

    @Override
    public void setVisibility(boolean visible) {
        projectPublic.setValue(visible, false);
        projectPrivate.setValue(!visible, false);
    }

    @Override
    public void removeNameError() {
        projectName.removeStyleName(style.inputError());
    }

    @Override
    public void showNameError() {
        projectName.addStyleName(style.inputError());
    }

    @Override
    public void focusOnName() {
        new Timer() {
            @Override
            public void run() {
                projectName.setFocus(true);
            }
        }.schedule(300);
    }

    @Override
    public void disableAllExceptName() {
        changeEnabledStateAll(false);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    @Override
    public void selectProjectType(final String projectTypeId) {

        ProjectTypeDefinition typeDescriptor = null;
        for (String category : categories.keySet()) {
            for (ProjectTypeDefinition descriptor : categories.get(category)) {
                if (descriptor.getId().equals(projectTypeId)) {
                    typeDescriptor = descriptor;
                    break;
                }
            }
            if (typeDescriptor != null) {
                break;
            }
        }
        if (typeDescriptor != null) {
            for (ProjectTypeDefinition existingProjectTypeDescriptor : availableProjectTypes) {
                if (existingProjectTypeDescriptor.getId().equals(typeDescriptor.getId())) {
                    list.selectElement(typeDescriptor);
                    selectNextWizardType(typeDescriptor);
                }
            }
        }
        projectName.setFocus(true);
    }

    @Override
    public void setAvailableProjectTypeDescriptors(List<ProjectTypeDefinition> availableProjectTypes) {
        this.availableProjectTypes = availableProjectTypes;
    }

    @Override
    public void setProjectTypeCategories(Map<String, Set<ProjectTypeDefinition>> categories,
                                         Map<String, Set<ProjectTemplateDescriptor>> samples) {
        this.categories = categories;
        this.samples = samples;

        List<Category<?>> categoriesList = new ArrayList<>();
        for (String s : categories.keySet()) {
            Category<ProjectTypeDefinition> category = new Category<>(s, projectTypeRenderer, categories.get(s), projectTypeDelegate);
            categoriesList.add(category);
        }

        for (String s : samples.keySet()) {
            List<ProjectTemplateDescriptor> projectTemplateDescriptors = new ArrayList<>();
            projectTemplateDescriptors.addAll(samples.get(s));
            Collections.sort(projectTemplateDescriptors, new ProjectTemplateDescriptorComparator());
            Category<ProjectTemplateDescriptor> category = new Category<>(s, projectTemplateRenderer, projectTemplateDescriptors,
                                                                          projectTemplateDelegate);
            categoriesList.add(category);
        }
        list.render(categoriesList);
    }

    @Override
    public void reset() {
        resetName();
        categoriesPanel.clear();
        list = new CategoriesList(resources);
        categoriesPanel.add(list);
        com.google.gwt.dom.client.Style style = list.getElement().getStyle();
        style.setWidth(100, com.google.gwt.dom.client.Style.Unit.PCT);
        style.setHeight(100, com.google.gwt.dom.client.Style.Unit.PCT);
        style.setPosition(com.google.gwt.dom.client.Style.Position.RELATIVE);
        descriptionArea.getElement().setInnerHTML("");
        configurationArea.getElement().setInnerText("");
    }

    interface MainPageViewImplUiBinder
            extends UiBinder<DockLayoutPanel, MainPageViewImpl> {
    }

    public interface Style extends Styles {
        String mainPanel();

        String leftPart();

        String rightPart();

        String namePanel();

        String labelPosition();

        String inputFieldPosition();

        String radioButtonPosition();

        String categories();

        String description();

        String configuration();

        String label();

        String horizontalLine();

        String labelTitle();
    }
}