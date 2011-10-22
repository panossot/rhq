/*
 * RHQ Management Platform
 * Copyright (C) 2011 Red Hat, Inc.
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

package org.rhq.enterprise.server.drift;

import static java.util.Arrays.asList;
import static org.rhq.core.domain.drift.DriftCategory.FILE_ADDED;
import static org.rhq.core.domain.drift.DriftChangeSetCategory.COVERAGE;
import static org.rhq.core.domain.drift.DriftChangeSetCategory.DRIFT;
import static org.rhq.core.domain.drift.DriftConfigurationDefinition.BaseDirValueContext.fileSystem;
import static org.rhq.core.domain.drift.DriftConfigurationDefinition.DriftHandlingMode.normal;
import static org.rhq.core.domain.drift.DriftDefinitionComparator.CompareMode.BOTH_BASE_INFO_AND_DIRECTORY_SPECIFICATIONS;
import static org.rhq.enterprise.server.util.LookupUtil.getDriftManager;
import static org.rhq.enterprise.server.util.LookupUtil.getDriftTemplateManager;
import static org.rhq.test.AssertUtils.assertCollectionMatchesNoOrder;
import static org.rhq.test.AssertUtils.assertPropertiesMatch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.criteria.DriftDefinitionCriteria;
import org.rhq.core.domain.criteria.DriftDefinitionTemplateCriteria;
import org.rhq.core.domain.criteria.GenericDriftChangeSetCriteria;
import org.rhq.core.domain.criteria.JPADriftChangeSetCriteria;
import org.rhq.core.domain.drift.Drift;
import org.rhq.core.domain.drift.DriftChangeSet;
import org.rhq.core.domain.drift.DriftDefinition;
import org.rhq.core.domain.drift.DriftDefinitionComparator;
import org.rhq.core.domain.drift.DriftDefinitionTemplate;
import org.rhq.core.domain.drift.JPADrift;
import org.rhq.core.domain.drift.JPADriftChangeSet;
import org.rhq.core.domain.drift.JPADriftFile;
import org.rhq.core.domain.drift.JPADriftSet;
import org.rhq.core.domain.resource.Resource;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.domain.util.PageList;
import org.rhq.test.TransactionCallback;

public class DriftTemplateManagerBeanTest extends DriftServerTest {

    private DriftTemplateManagerLocal templateMgr;

    private DriftManagerLocal driftMgr;

    private List<Resource> resources = new LinkedList<Resource>();

    private JPADrift drift1;
    private JPADrift drift2;
    private JPADriftFile driftFile1;
    private JPADriftFile driftFile2;

    @BeforeClass
    public void initClass() {
        templateMgr = getDriftTemplateManager();
        driftMgr = getDriftManager();
    }

    public void createNewTemplate() {
        final DriftDefinition definition = new DriftDefinition(new Configuration());
        definition.setName("test::createNewTemplate");
        definition.setEnabled(true);
        definition.setDriftHandlingMode(normal);
        definition.setInterval(2400L);
        definition.setBasedir(new DriftDefinition.BaseDirectory(fileSystem, "/foo/bar/test"));

        final DriftDefinitionTemplate newTemplate = templateMgr.createTemplate(getOverlord(), resourceType.getId(),
            true, definition);

        executeInTransaction(new TransactionCallback() {
            @Override
            public void execute() throws Exception {
                EntityManager em = getEntityManager();

                ResourceType updatedType = em.find(ResourceType.class, resourceType.getId());

                assertEquals("Failed to add new drift definition to resource type", 1, updatedType
                    .getDriftDefinitionTemplates().size());

                DriftDefinitionTemplate expectedTemplate = new DriftDefinitionTemplate();
                expectedTemplate.setTemplateDefinition(definition);
                expectedTemplate.setUserDefined(true);

                assertDriftTemplateEquals("Failed to save template", expectedTemplate, newTemplate);
                assertTrue("The template should have its id set", newTemplate.getId() > 0);
            }
        });
    }

    // Note: This test is going to change substantially in terms of the behavior that it
    // is verifying because it was written before the design has been fully flushed out.
//    public void updateTemplateNameAndDescription() {
//        // first create a template
//        final DriftDefinition definition = new DriftDefinition(new Configuration());
//        definition.setName("test::updateNameAndDescription");
//        definition.setDescription("testing updating template name and description");
//        definition.setEnabled(true);
//        definition.setDriftHandlingMode(normal);
//        definition.setInterval(2400L);
//        definition.setBasedir(new DriftDefinition.BaseDirectory(fileSystem, "/foo/bar/test"));
//
//        templateMgr.createTemplate(getOverlord(), resourceType.getId(), true, definition);
//
//        // perform the update
//        DriftDefinitionTemplate template = loadTemplate(definition.getName());
//        String updatedName = "UPDATED NAME";
//        template.setName(updatedName);
//        template.setDescription("UPDATED DESCRIPTION");
//
//        templateMgr.updateTemplate(getOverlord(), template, false);
//
//        // verify that the update was made
//        DriftDefinitionTemplate updatedTemplate = loadTemplate(updatedName);
//
//        assertDriftTemplateEquals("Failed to update template", template, updatedTemplate);
//    }

    // Note: This test is going to change substantially in terms of the behavior that it
    // is verifying because it was written before the design has been fully flushed out.
//    public void updateTemplateEnabledFlagAndIntervalAndApplyToDefs() {
//        // create a new template
//        final DriftDefinition definition = new DriftDefinition(new Configuration());
//        definition.setName("test::updateEnabledFlagAndInterval");
//        definition.setDescription("test updating enabled flag and interval");
//        definition.setEnabled(false);
//        definition.setDriftHandlingMode(normal);
//        definition.setInterval(2400L);
//        definition.setBasedir(new DriftDefinition.BaseDirectory(fileSystem, "/foo/bar/test"));
//
//        templateMgr.createTemplate(getOverlord(), resourceType.getId(), true, definition);
//
//        // create some definitions
//        DriftDefinitionTemplate template = loadTemplate(definition.getName());
//
//        final DriftDefinition def1 = template.createDefinition();
//        def1.setName("def 1");
//        def1.setTemplate(template);
//        def1.setResource(resource);
//
//        final DriftDefinition def2 = template.createDefinition();
//        def2.setName("def 2");
//        def2.setTemplate(template);
//        def2.setResource(resource);
//
//        driftMgr.updateDriftDefinition(getOverlord(), EntityContext.forResource(resource.getId()), def1);
//        driftMgr.updateDriftDefinition(getOverlord(), EntityContext.forResource(resource.getId()), def2);
//
//        // perform the update
//        template.getTemplateDefinition().setEnabled(false);
//        template.getTemplateDefinition().setInterval(3600L);
//        templateMgr.updateTemplate(getOverlord(), template, true);
//    }

    @SuppressWarnings("unchecked")
    public void pinTemplate() throws Exception {
        // First create the template
        final DriftDefinition templateDef = new DriftDefinition(new Configuration());
        templateDef.setName("test::pinTemplate");
        templateDef.setEnabled(true);
        templateDef.setDriftHandlingMode(normal);
        templateDef.setInterval(2400L);
        templateDef.setBasedir(new DriftDefinition.BaseDirectory(fileSystem, "/foo/bar/test"));

        final DriftDefinitionTemplate template = templateMgr.createTemplate(getOverlord(),
            resourceType.getId(), true, templateDef);

        // next create some resource level definitions
        final DriftDefinition attachedDef1 = createDefinition(template, "attachedDef1", true);
        final DriftDefinition attachedDef2 = createDefinition(template, "attachedDef2", true);
        final DriftDefinition detachedDef1 = createDefinition(template, "detachedDef1", false);
        final DriftDefinition detachedDef2 = createDefinition(template, "detachedDef2", false);

        // create initial change set from which the snapshot will be generated
        driftFile1 = new JPADriftFile("a1b2c3");
        drift1 = new JPADrift(null, "drift.1", FILE_ADDED, null, driftFile1);

        JPADriftSet driftSet = new JPADriftSet();
        driftSet.addDrift(drift1);

        final JPADriftChangeSet changeSet0 = new JPADriftChangeSet(resource, 0, COVERAGE, attachedDef1);
        changeSet0.setInitialDriftSet(driftSet);

        // create change set v1
        driftFile2 = new JPADriftFile("1a2b3c");
        final JPADriftChangeSet changeSet1 = new JPADriftChangeSet(resource, 1, DRIFT, attachedDef1);
        drift2 = new JPADrift(changeSet1, "drift.2", FILE_ADDED, null, driftFile2);

        executeInTransaction(new TransactionCallback() {
            @Override
            public void execute() throws Exception {
                EntityManager em = getEntityManager();
                em.persist(attachedDef1);
                em.persist(driftFile1);
                em.persist(driftFile2);
                em.persist(drift1);
                em.persist(drift2);
                em.persist(changeSet0);
                em.persist(changeSet1);

                em.persist(attachedDef2);
                em.persist(detachedDef1);
                em.persist(detachedDef2);
            }
        });

        // now we pin the snapshot to the template
        templateMgr.pinTemplate(getOverlord(), template.getId(), attachedDef1.getId(), 1);

        // verify that the template is now pinned
        DriftDefinitionTemplate updatedTemplate = loadTemplate(template.getName());
        assertTrue("Template should be marked pinned", updatedTemplate.isPinned());
    }

    @Test(dependsOnMethods = "pinTemplate")
    @InitDB(false)
    public void persistChangeSetWhenTemplateGetsPinned() throws Exception {
        DriftDefinitionTemplate template = loadTemplate("test::pinTemplate");

        GenericDriftChangeSetCriteria criteria = new GenericDriftChangeSetCriteria();
        criteria.addFilterId(template.getChangeSetId());

        PageList<? extends DriftChangeSet<?>> changeSets = driftMgr.findDriftChangeSetsByCriteria(getOverlord(),
            criteria);

        assertEquals("Expected to find change set for pinned template", 1, changeSets.size());

        JPADriftChangeSet expectedChangeSet = new JPADriftChangeSet(resource, 1, COVERAGE, null);
        List<? extends Drift> expectedDrifts = asList(
            new JPADrift(expectedChangeSet, "drift.1", FILE_ADDED, null, driftFile1),
            new JPADrift(expectedChangeSet, drift2.getPath(), FILE_ADDED, null, driftFile2));

        DriftChangeSet<?> actualChangeSet = changeSets.get(0);
        List<? extends Drift> actualDrifts = new ArrayList(actualChangeSet.getDrifts());

        assertCollectionMatchesNoOrder("Expected to find drifts from change sets 1 and 2 in the template change set",
            (List<Drift>)expectedDrifts, (List<Drift>)actualDrifts, "id", "ctime", "changeSet", "newDriftFile");

        // we need to compare the newDriftFile properties separately because
        // assertCollectionMatchesNoOrder compares properties via equals() and JPADriftFile
        // does not implement equals.
        assertPropertiesMatch(drift1.getNewDriftFile(), findDriftByPath(actualDrifts, "drift.1").getNewDriftFile(),
            "The newDriftFile property was not set correctly for " + drift1);
        assertPropertiesMatch(drift2.getNewDriftFile(), findDriftByPath(actualDrifts, "drift.2").getNewDriftFile(),
            "The newDriftFile property was not set correctly for " + drift1);
    }

    @Test(dependsOnMethods = "pinTemplate")
    @InitDB(false)
    public void updateAttachedDefinitionsWhenTemplateGetsPinned() throws Exception {
        DriftDefinitionTemplate template = loadTemplate("test::pinTemplate");

        // get the attached definitions
        List<DriftDefinition> attachedDefs = new LinkedList<DriftDefinition>();
        for (DriftDefinition d : template.getDriftDefinitions()) {
            if (d.isAttached() && (d.getName().equals("attachedDef1") || d.getName().equals("attachedDef2"))) {
                attachedDefs.add(d);
            }
        }
        assertEquals("Failed to get attached definitions for " + toString(template), 2, attachedDefs.size());
        assertDefinitionIsPinned(attachedDefs.get(0));
        assertDefinitionIsPinned(attachedDefs.get(1));
    }

    @Test(dependsOnMethods = "pinTemplate")
    @InitDB(false)
    public void doNotUpdateDetachedDefinitionsWhenTemplateGetsPinned() throws Exception {
        DriftDefinitionTemplate template = loadTemplate("test::pinTemplate");

        // get the detached definitions
        List<DriftDefinition> detachedDefs = new LinkedList<DriftDefinition>();
        for (DriftDefinition d : template.getDriftDefinitions()) {
            if (!d.isAttached() && (d.getName().equals("detachedDef1") || d.getName().equals("detachedDef2"))) {
                detachedDefs.add(d);
            }
        }
        assertEquals("Failed to get detached definitions for " + toString(template), 2, detachedDefs.size());
        assertDefinitionIsNotPinned(detachedDefs.get(0));
        assertDefinitionIsNotPinned(detachedDefs.get(1));
    }

    public void deleteUnpinnedTemplate() throws Exception {
        // first create the template
        final DriftDefinition templateDef = new DriftDefinition(new Configuration());
        templateDef.setName("test::pinTemplate");
        templateDef.setEnabled(true);
        templateDef.setDriftHandlingMode(normal);
        templateDef.setInterval(2400L);
        templateDef.setBasedir(new DriftDefinition.BaseDirectory(fileSystem, "/foo/bar/test"));

        final DriftDefinitionTemplate template = templateMgr.createTemplate(getOverlord(),
            resourceType.getId(), true, templateDef);

        // next create some resource level definitions
        final DriftDefinition attachedDef1 = createDefinition(template, "attachedDef1", true);
        final DriftDefinition attachedDef2 = createDefinition(template, "attachedDef2", true);
        final DriftDefinition detachedDef1 = createDefinition(template, "detachedDef1", false);
        final DriftDefinition detachedDef2 = createDefinition(template, "detachedDef2", false);

        // create some change sets
        driftFile1 = new JPADriftFile("a1b2c3");
        drift1 = new JPADrift(null, "drift.1", FILE_ADDED, null, driftFile1);

        JPADriftSet driftSet0 = new JPADriftSet();
        driftSet0.addDrift(drift1);

        final JPADriftChangeSet changeSet0 = new JPADriftChangeSet(resource, 0, COVERAGE, attachedDef1);
        changeSet0.setInitialDriftSet(driftSet0);


        driftFile2 = new JPADriftFile("1a2b3c");
        drift2 = new JPADrift(null, "drift.2", FILE_ADDED, null, driftFile2);

        JPADriftSet driftSet1 = new JPADriftSet();
        driftSet1.addDrift(drift2);

        final JPADriftChangeSet changeSet1 = new JPADriftChangeSet(resource, 0, DRIFT, detachedDef1);
        changeSet1.setInitialDriftSet(driftSet1);

        executeInTransaction(new TransactionCallback() {
            @Override
            public void execute() throws Exception {
                EntityManager em = getEntityManager();
                em.persist(attachedDef1);
                em.persist(attachedDef2);
                em.persist(detachedDef1);
                em.persist(detachedDef2);
                em.persist(driftFile1);
                em.persist(driftFile2);
                em.persist(drift1);
                em.persist(drift2);
                em.persist(changeSet0);
                em.persist(changeSet1);
            }
        });

        // delete the template
        templateMgr.deleteTemplate(getOverlord(), template.getId());

        // verify that attached definitions along with their change sets have
        // been deleted
        assertNull("Change sets belonging to attached definitions should be deleted", loadChangeSet(changeSet0.getId()));
        assertNull("Attached definition " + toString(attachedDef1) + " should be deleted",
            loadDefinition(attachedDef1.getId()));
        assertNull("Attached definition " + toString(attachedDef2) + " should be deleted",
            loadDefinition(attachedDef2.getId()));

        // verify that detached definitions along with their change sets have not been deleted
        assertNotNull("Change sets belonging to detached definitions should not be deleted",
            loadChangeSet(changeSet1.getId()));
        assertNotNull("Detached definition " + toString(detachedDef1) + " should not be deleted",
            loadDefinition(detachedDef1.getId()));
        assertNotNull("Detached definition " + toString(detachedDef2) + " should not be deleted",
            loadDefinition(detachedDef2.getId()));
    }

    private void assertDefinitionIsPinned(DriftDefinition definition) throws Exception {
        // verify that the definition is marked as pinned
        assertTrue("Expected " + toString(definition) + " to be pinned", definition.isPinned());

        // verify that the initial change set is generated for the definition
        JPADriftChangeSetCriteria criteria = new JPADriftChangeSetCriteria();
        criteria.addFilterDriftDefinitionId(definition.getId());
        criteria.addFilterCategory(COVERAGE);
        criteria.fetchDrifts(true);

        PageList<? extends DriftChangeSet<?>> changeSets = driftMgr.findDriftChangeSetsByCriteria(getOverlord(),
            criteria);
        assertEquals("Expected to find one change set", 1, changeSets.size());

        JPADriftChangeSet expectedChangeSet = new JPADriftChangeSet(resource, 1, COVERAGE, null);
        List<? extends Drift> expectedDrifts = asList(
            new JPADrift(expectedChangeSet, drift1.getPath(), FILE_ADDED, null, driftFile1),
            new JPADrift(expectedChangeSet, drift2.getPath(), FILE_ADDED, null, driftFile2));

        DriftChangeSet<?> actualChangeSet = changeSets.get(0);
        List<? extends Drift> actualDrifts = new ArrayList(actualChangeSet.getDrifts());

        assertCollectionMatchesNoOrder("Expected to find drifts from change sets 1 and 2 in the template change set",
            (List<Drift>)expectedDrifts, (List<Drift>)actualDrifts, "id", "ctime", "changeSet", "newDriftFile");

        // Finally make sure that there are no other change sets
        criteria = new JPADriftChangeSetCriteria();
        criteria.addFilterStartVersion(1);
        criteria.addFilterDriftDefinitionId(definition.getId());

        assertEquals("There should not be any drift change sets", 0,
            driftMgr.findDriftChangeSetsByCriteria(getOverlord(), criteria).size());
    }

    private void assertDefinitionIsNotPinned(DriftDefinition definition) throws Exception {
        // verify that the definition is not pinned
        assertFalse("Expected " + toString(definition) + " to be unpinned", definition.isPinned());

        // Note that this method assumes that the definition has no change sets
        // associated with it and therefore checks that there are no change sets.
        JPADriftChangeSetCriteria criteria = new JPADriftChangeSetCriteria();
        criteria.addFilterDriftDefinitionId(definition.getId());

        PageList<? extends DriftChangeSet<?>> changeSets = driftMgr.findDriftChangeSetsByCriteria(getOverlord(),
            criteria);
        assertEquals("Did not expect to find any change sets for " + toString(definition) + ". Note that this " +
            "assertion method assumes that the definition you are testing is not supposed to have any change sets.",
            0, changeSets.size());
    }

    private void assertDriftTemplateEquals(String msg, DriftDefinitionTemplate expected, DriftDefinitionTemplate actual) {
        assertPropertiesMatch(msg + ": basic drift definition template properties do not match", expected, actual,
            "id", "resourceType", "ctime", "templateDefinition");
        assertDriftDefEquals(msg + ": template definitions do not match", expected.getTemplateDefinition(), actual
            .getTemplateDefinition());
    }

    private void assertDriftDefEquals(String msg, DriftDefinition expected, DriftDefinition actual) {
        DriftDefinitionComparator comparator = new DriftDefinitionComparator(
            BOTH_BASE_INFO_AND_DIRECTORY_SPECIFICATIONS);
        assertEquals(msg, 0, comparator.compare(expected, actual));
    }

    private DriftDefinition createDefinition(DriftDefinitionTemplate template, String defName, boolean isAttached) {
        DriftDefinition def = template.createDefinition();
        def.setName(defName);
        def.setAttached(isAttached);
        def.setTemplate(template);
        def.setResource(resource);
        return def;
    }

    private DriftDefinitionTemplate loadTemplate(String name) {
        DriftDefinitionTemplateCriteria criteria = new DriftDefinitionTemplateCriteria();
        criteria.addFilterResourceTypeId(resourceType.getId());
        criteria.addFilterName(name);
        criteria.fetchDriftDefinitions(true);

        PageList<DriftDefinitionTemplate> templates = templateMgr.findTemplatesByCriteria(getOverlord(), criteria);
        assertEquals("Expected to find one template", 1, templates.size());

        return templates.get(0);
    }

    private DriftDefinition loadDefinition(int definitionId) {
        DriftDefinitionCriteria criteria = new DriftDefinitionCriteria();
        criteria.addFilterId(definitionId);
        PageList<DriftDefinition> definitions = driftMgr.findDriftDefinitionsByCriteria(getOverlord(), criteria);

        if (definitions.isEmpty()) {
            return null;
        }
        return definitions.get(0);
    }

    private DriftChangeSet<?> loadChangeSet(String id) throws Exception {
        GenericDriftChangeSetCriteria criteria = new GenericDriftChangeSetCriteria();
        criteria.addFilterId(id);
        PageList<? extends DriftChangeSet<?>> changeSets = driftMgr.findDriftChangeSetsByCriteria(getOverlord(),
            criteria);

        if (changeSets.isEmpty()) {
            return null;
        }
        return changeSets.get(0);
    }

}
