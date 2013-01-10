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
package org.exoplatform.ide.vfs.impl.fs;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.ItemImpl;
import org.exoplatform.ide.vfs.shared.ItemList;
import org.exoplatform.ide.vfs.shared.ItemType;
import org.exoplatform.ide.vfs.shared.Property;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChildrenTest extends PlainFileSystemTest
{
//   private Node childrenTestNode;
   private String folderId;
   private String propertyFilter = "PropertyA";

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
//      String name = getClass().getName();
//      childrenTestNode = testRoot.addNode(name, "nt:unstructured");
//      childrenTestNode.addMixin("exo:privilegeable");
//
//      Node folderNode = childrenTestNode.addNode("ChildrenTest_FOLDER", "nt:folder");
//
//      Node childFileNode = folderNode.addNode("ChildrenTest_FILE01", "nt:file");
//      childFileNode.addMixin("exo:unstructuredMixin");
//      childFileNode.setProperty("PropertyA", "A");
//      childFileNode.setProperty("PropertyB", "B");
//      Node childContentNode = childFileNode.addNode("jcr:content", "nt:resource");
//      childContentNode.setProperty("jcr:mimeType", "text/plain");
//      childContentNode.setProperty("jcr:lastModified", Calendar.getInstance());
//      childContentNode.setProperty("jcr:data", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
//
//      Node childFolderNode1 = folderNode.addNode("ChildrenTest_FOLDER01", "nt:folder");
//      childFolderNode1.addMixin("exo:unstructuredMixin");
//      childFolderNode1.setProperty("PropertyA", "A");
//      childFolderNode1.setProperty("PropertyB", "B");
//
//      Node childFolderNode2 = folderNode.addNode("ChildrenTest_FOLDER02", "nt:folder");
//      childFolderNode2.addMixin("exo:unstructuredMixin");
//      childFolderNode2.setProperty("PropertyA", "A");
//      childFolderNode2.setProperty("PropertyB", "B");
//
//      session.save();
//
//      folderId = ((ExtendedNode)folderNode).getIdentifier();
      folderId = "@root";
   }

   public void testGetChildren() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String path = SERVICE_URI + "children/" + folderId;
      ContainerResponse response = launcher.service("GET", path, BASE_URI, null, null, writer, null);
      //assertEquals(200, response.getStatus());
      log.info(new String(writer.getBody()));
      @SuppressWarnings("unchecked")
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      List<String> list = new ArrayList<String>(3);
      for (Item i : children.getItems()){
         validateLinks(i);
         list.add(i.getName());
         System.out.println(i.getName());
         System.out.println(i.getPath());
      }

//      assertEquals(3, list.size());
//      assertTrue(list.contains("ChildrenTest_FOLDER01"));
//      assertTrue(list.contains("ChildrenTest_FOLDER02"));
//      assertTrue(list.contains("ChildrenTest_FILE01"));
   }

   public void _testGetChildrenNoPermissions() throws Exception
   {
      // TODO
//      Map<String, String[]> permissions = new HashMap<String, String[]>(1);
//      permissions.put("root", PermissionType.ALL);
//      ((ExtendedNode)childrenTestNode).setPermissions(permissions);
//      session.save();
//
//      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
//      String path = new StringBuilder() //
//         .append(SERVICE_URI) //
//         .append("children/") //
//         .append(folderId).toString();
//      ContainerResponse response = launcher.service("GET", path, BASE_URI, null, null, writer, null);
//      assertEquals(403, response.getStatus());
//      log.info(new String(writer.getBody()));
   }

   @SuppressWarnings("unchecked")
   public void _testGetChildrenPagingSkipCount() throws Exception
   {
      // Get all children.
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("children/") //
         .append(folderId).toString();
      ContainerResponse response = launcher.service("GET", path, BASE_URI, null, null, null);
      assertEquals(200, response.getStatus());
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      List<Object> all = new ArrayList<Object>(3);
      for (Item i : children.getItems())
         all.add(i.getName());

      Iterator<Object> iteratorAll = all.iterator();
      iteratorAll.next();
      iteratorAll.remove();

      // Skip first item in result.
      path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("children/") //
         .append(folderId) //
         .append("?") //
         .append("skipCount=") //
         .append("1") //
         .toString();
      checkPage(path, "GET", ItemImpl.class.getMethod("getName"), all);
   }

   @SuppressWarnings("unchecked")
   public void _testGetChildrenPagingMaxItems() throws Exception
   {
      // Get all children.
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("children/") //
         .append(folderId).toString();
      ContainerResponse response = launcher.service("GET", path, BASE_URI, null, null, null);
      assertEquals(200, response.getStatus());
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      List<Object> all = new ArrayList<Object>(3);
      for (Item i : children.getItems())
         all.add(i.getName());

      // Exclude last item from result.
      path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("children/") //
         .append(folderId) //
         .append("?") //
         .append("maxItems=") //
         .append("2") //
         .toString();
      all.remove(2);
      checkPage(path, "GET", ItemImpl.class.getMethod("getName"), all);
   }

   @SuppressWarnings("unchecked")
   public void _testGetChildrenNoPropertyFilter() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      // Get children without filter.
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("children/") //
         .append(folderId) //
         .toString();
      ContainerResponse response = launcher.service("GET", path, BASE_URI, null, null, writer, null);
      //log.info(new String(writer.getBody()));
      assertEquals(200, response.getStatus());
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      assertEquals(3, children.getItems().size());
      for (Item i : children.getItems())
      {
         // No properties without filter. 'none' filter is used if nothing set by client.
         assertFalse(hasProperty(i, "PropertyA"));
         assertFalse(hasProperty(i, "PropertyB"));
      }
   }

   @SuppressWarnings("unchecked")
   public void _testGetChildrenPropertyFilter() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      // Get children and apply filter for properties.
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("children/") //
         .append(folderId) //
         .append("?") //
         .append("propertyFilter=") //
         .append(propertyFilter) //
         .toString();
      ContainerResponse response = launcher.service("GET", path, BASE_URI, null, null, writer, null);
      //log.info(new String(writer.getBody()));
      assertEquals(200, response.getStatus());
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      assertEquals(3, children.getItems().size());
      for (Item i : children.getItems())
      {
         assertTrue(hasProperty(i, "PropertyA"));
         assertFalse(hasProperty(i, "PropertyB")); // must be excluded
      }
   }

   public void _testGetChildrenTypeFilter() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      // Get children and apply filter for properties.
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("children/") //
         .append(folderId) //
         .append("?") //
         .append("itemType=") //
         .append("folder") //
         .toString();
      ContainerResponse response = launcher.service("GET", path, BASE_URI, null, null, writer, null);
      //log.info(new String(writer.getBody()));
      assertEquals(200, response.getStatus());
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      assertEquals(2, children.getItems().size());
      for (Item i : children.getItems())
      {
         assertTrue(i.getItemType() == ItemType.FOLDER);
      }
   }

   @SuppressWarnings("rawtypes")
   private boolean hasProperty(Item i, String propertyName)
   {
      List<Property> properties = i.getProperties();
      if (properties.size() == 0)
         return false;
      for (Property p : properties)
         if (p.getName().equals(propertyName))
            return true;
      return false;
   }
}
