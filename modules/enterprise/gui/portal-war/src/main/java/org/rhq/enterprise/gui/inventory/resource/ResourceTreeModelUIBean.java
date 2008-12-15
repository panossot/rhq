/*
 * RHQ Management Platform
 * Copyright (C) 2005-2008 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.enterprise.gui.inventory.resource;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlOutputLink;
import javax.el.MethodExpression;
import javax.el.ExpressionFactory;

import org.richfaces.component.html.ContextMenu;
import org.richfaces.component.html.HtmlMenuGroup;
import org.richfaces.component.html.HtmlMenuItem;
import org.richfaces.component.html.HtmlMenuSeparator;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.component.UITree;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.model.TreeNode;

import org.rhq.core.domain.auth.Subject;
import org.rhq.core.domain.measurement.DataType;
import org.rhq.core.domain.measurement.MeasurementSchedule;
import org.rhq.core.domain.operation.OperationDefinition;
import org.rhq.core.domain.resource.Resource;
import org.rhq.core.domain.resource.composite.ResourceWithAvailability;
import org.rhq.core.domain.resource.group.composite.AutoGroupComposite;
import org.rhq.core.gui.util.FacesComponentUtility;
import org.rhq.enterprise.gui.util.EnterpriseFacesContextUtility;
import org.rhq.enterprise.server.measurement.MeasurementScheduleManagerLocal;
import org.rhq.enterprise.server.operation.OperationManagerLocal;
import org.rhq.enterprise.server.resource.ResourceManagerLocal;
import org.rhq.enterprise.server.resource.ResourceTypeManagerLocal;
import org.rhq.enterprise.server.util.LookupUtil;

public class ResourceTreeModelUIBean {

    private List<ResourceTreeNode> roots = new ArrayList<ResourceTreeNode>();
    private ResourceTreeNode rootNode = null;
    private List<ResourceTreeNode> children = new ArrayList<ResourceTreeNode>();

    private ResourceManagerLocal resourceManager = LookupUtil.getResourceManager();
    private ResourceTypeManagerLocal resourceTypeManager = LookupUtil.getResourceTypeManager();
    private OperationManagerLocal operationManager = LookupUtil.getOperationManager();
    private MeasurementScheduleManagerLocal measurementScheduleManager = LookupUtil.getMeasurementScheduleManager();

    private String nodeTitle;
    private static final String DATA_PATH = "/richfaces/tree/examples/simple-tree-data.properties";

    private ContextMenu resourceContextMenu;

    private void loadTree() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        Resource currentResource = EnterpriseFacesContextUtility.getResourceIfExists();


        Resource rootResource = resourceManager.getRootResourceForResource(currentResource.getId());

        rootNode = new ResourceTreeNode(rootResource);

        List<AutoGroupComposite> children =
                resourceManager.getChildrenAutoGroups(EnterpriseFacesContextUtility.getSubject(), rootResource.getId());

        for (AutoGroupComposite child : children) {
            ResourceTreeNode node = new ResourceTreeNode(child);
            this.children.add(node);
        }

    }


    public ResourceTreeNode getTreeNode() {
        if (rootNode == null) {
            loadTree();
        }

        return rootNode;
    }

    public List<ResourceTreeNode> getRoots() {
        if (roots.isEmpty()) {
            roots.add(getTreeNode());
        }
        return roots;
    }

    public List<ResourceTreeNode> getChildren() {
        return children;
    }


    public String getNodeTitle() {
        return nodeTitle;
    }

    public void setNodeTitle(String nodeTitle) {
        this.nodeTitle = nodeTitle;
    }

    public ContextMenu getMenu() {
        return resourceContextMenu;
    }


    public void processSelection(NodeSelectedEvent event) {
        UITree tree = (UITree) event.getComponent();


        try {

            Object node = tree.getRowData();
            ResourceTreeNode selectedNode = (ResourceTreeNode) node;

            Object data = selectedNode.getData();
            if (data instanceof ResourceWithAvailability) {
                FacesContext.getCurrentInstance();
                        
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMenu(ContextMenu menu) {
        this.resourceContextMenu = menu;

        this.resourceContextMenu.getChildren().clear();

        Subject subject = EnterpriseFacesContextUtility.getSubject();

        String resourceIdString = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("contextResourceId");
        String resourceTypeIdString = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("contextResourceTypeId");
        if (resourceTypeIdString != null) {
            int resourceId = Integer.parseInt(resourceIdString);
            int resourceTypeId =
                    Integer.parseInt(resourceTypeIdString);

            Resource res = resourceManager.getResourceById(EnterpriseFacesContextUtility.getSubject(), resourceId);
            Application app = FacesContext.getCurrentInstance().getApplication();
            // type = resourceTypeManager.getResourceTypeById(EnterpriseFacesContextUtility.getSubject(), resourceTypeId);
            
            HtmlMenuItem nameItem = new HtmlMenuItem();
            nameItem.setValue(res.getName());
            nameItem.setId("menu_res_" + res.getId());

            nameItem.getChildren().add(
                    FacesComponentUtility.addOutputLink(nameItem, null, "/rhq/resource/monitor/graphs.xhtml?id=" + resourceIdString));

            this.resourceContextMenu.getChildren().add(nameItem);
            this.resourceContextMenu.getChildren().add(new HtmlMenuSeparator());


            // *** Measurements menu
            List<MeasurementSchedule> scheds = measurementScheduleManager.getMeasurementSchedulesForResourceAndType(
                    subject, resourceId, DataType.MEASUREMENT, null, true);

            if (scheds != null) {
                HtmlMenuGroup measurementsMenu = new HtmlMenuGroup();
                measurementsMenu.setValue("Measurements");
                this.resourceContextMenu.getChildren().add(measurementsMenu);
                measurementsMenu.setDisabled(scheds.isEmpty());

                for (MeasurementSchedule sched : scheds) {
                    HtmlMenuItem menuItem = new HtmlMenuItem();
                    String subOption = sched.getDefinition().getDisplayName();
                    menuItem.setValue(subOption);
                    menuItem.setId("measurement_" + sched.getId());

                    // MethodExpression me = ExpressionFactory.newInstance().createMethodExpression();
                    MethodBinding binding = app.createMethodBinding("#{otherBean.action}", null);
                    menuItem.setAction(binding);

                    measurementsMenu.getChildren().add(menuItem);
                }
            }


            // **** Operations menugroup

            List<OperationDefinition> operations = operationManager.getSupportedResourceTypeOperations(subject, resourceTypeId);

            if (operations != null) {
                HtmlMenuGroup operationsMenu = new HtmlMenuGroup();
                operationsMenu.setValue("Operations");
                this.resourceContextMenu.getChildren().add(operationsMenu);
                operationsMenu.setDisabled(operations.isEmpty());

                for (OperationDefinition def : operations) {
                    HtmlMenuItem menuItem = new HtmlMenuItem();
                    String subOption = def.getDisplayName();
                    menuItem.setValue(subOption);
                    menuItem.setId("operation_" + def.getId());

                    // MethodExpression me = ExpressionFactory.newInstance().createMethodExpression();
                    MethodBinding binding = app.createMethodBinding("#{otherBean.action}", null);
                    menuItem.setAction(binding);

                    operationsMenu.getChildren().add(menuItem);
                }
            }
        }
    }

    /*public void setMenu(ContextMenu menu) {
        this.operationsMenu = menu;

         operationsMenu.getChildren().clear();

        String resourceTypeIdString = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("contextResourceTypeId");
        if (resourceTypeIdString != null) {
            int resourceTypeId = Integer.parseInt(resourceTypeIdString);

            ResourceType type = null;

            // type = resourceTypeManager.getResourceTypeById(EnterpriseFacesContextUtility.getSubject(), resourceTypeId);
            List<OperationDefinition> operations = operationManager.getSupportedResourceTypeOperations(EnterpriseFacesContextUtility.getSubject(), resourceTypeId);

            if (operations != null) {
                for (OperationDefinition def : operations) {
                    HtmlMenuItem menuItem = new HtmlMenuItem();
                    String subOption = def.getDisplayName();
                    menuItem.setValue(subOption);
                    menuItem.setId("operation_" + def.getId());

                    Application app = FacesContext.getCurrentInstance().getApplication();
                    // MethodExpression me = ExpressionFactory.newInstance().createMethodExpression();
                    MethodBinding mb = app.createMethodBinding("#{otherBean.action}", null);
                    menuItem.setAction(mb);

                    operationsMenu.getChildren().add(menuItem);
                }
            }
        }
    }*/


}
