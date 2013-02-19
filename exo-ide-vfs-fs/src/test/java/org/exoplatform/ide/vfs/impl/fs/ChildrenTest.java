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
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo.BasicPermissions;

public class ChildrenTest extends LocalFileSystemTest
{
   private Map<String, String[]> properties;
   private String fileId;
   private String folderPath;
   private String folderId;
   private String protectedFolderId;
   private Set<String> childrenNames;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      folderPath = createDirectory(testRootPath, "ChildrenTest_Folder");
      String file01 = createFile(folderPath, "FILE01", DEFAULT_CONTENT_BYTES);
      String file02 = createFile(folderPath, "FILE02", DEFAULT_CONTENT_BYTES);
      String folder01 = createDirectory(folderPath, "FOLDER01");
      String folder02 = createDirectory(folderPath, "FOLDER02");

      childrenNames = new HashSet<String>(4);
      childrenNames.add("FILE01");
      childrenNames.add("FILE02");
      childrenNames.add("FOLDER01");
      childrenNames.add("FOLDER02");

      properties = new HashMap<String, String[]>(2);
      properties.put("MyProperty01", new String[]{"hello world"});
      properties.put("MyProperty02", new String[]{"to be or not to be"});
      writeProperties(file01, properties);
      writeProperties(file02, properties);
      writeProperties(folder01, properties);
      writeProperties(folder02, properties);

      String filePath = createFile(testRootPath, "ChildrenTest_File", DEFAULT_CONTENT_BYTES);

      String protectedFolderPath = createDirectory(testRootPath, "ChildrenTest_ProtectedFolder");
      Map<String, Set<BasicPermissions>> permissions = new HashMap<String, Set<BasicPermissions>>(1);
      permissions.put("andrew", EnumSet.of(BasicPermissions.ALL));
      writePermissions(protectedFolderPath, permissions);

      fileId = pathToId(filePath);
      folderId = pathToId(folderPath);
      protectedFolderId = pathToId(protectedFolderPath);
   }

   public void testGetChildren() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String requestPath = SERVICE_URI + "children/" + folderId;
      ContainerResponse response = launcher.service("GET", requestPath, BASE_URI, null, null, writer, null);
      assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
      log.info(new String(writer.getBody()));
      @SuppressWarnings("unchecked")
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      List<String> list = new ArrayList<String>(4);
      for (Item i : children.getItems())
      {
         validateLinks(i);
         list.add(i.getName());
      }

      assertEquals(4, list.size());
      childrenNames.removeAll(list);
      if (!childrenNames.isEmpty())
      {
         fail("Expected items " + childrenNames + " missed in response. ");
      }
   }

   public void testGetChildren_File() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String requestPath = SERVICE_URI + "children/" + fileId;
      ContainerResponse response = launcher.service("GET", requestPath, BASE_URI, null, null, writer, null);
      assertEquals(400, response.getStatus());
      log.info(new String(writer.getBody()));
   }

   public void testGetChildrenHavePermissions() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String requestPath = SERVICE_URI + "children/" + protectedFolderId;
      // Replace default principal by principal who has read permission.
      ConversationState user = new ConversationState(new Identity("andrew"));
      user.setAttribute("currentTenant", ConversationState.getCurrent().getAttribute("currentTenant"));
      ConversationState.setCurrent(user);
      // ---
      ContainerResponse response = launcher.service("GET", requestPath, BASE_URI, null, null, writer, null);
      assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
      log.info(new String(writer.getBody()));
      @SuppressWarnings("unchecked")
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      assertTrue(children.getItems().isEmpty()); // folder is empty
      assertEquals(0, children.getNumItems());
   }

   public void testGetChildrenNoPermissions() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String requestPath = SERVICE_URI + "children/" + protectedFolderId;
      ContainerResponse response = launcher.service("GET", requestPath, BASE_URI, null, null, writer, null);
      assertEquals(403, response.getStatus());
      log.info(new String(writer.getBody()));
   }

   public void testGetChildrenNoPermissions2() throws Exception
   {
      // Have permission for read folder but have not permission to read one of its child.
      String protectedItemPath = folderPath + '/' + childrenNames.iterator().next();
      Map<String, Set<BasicPermissions>> permissions = new HashMap<String, Set<BasicPermissions>>(1);
      permissions.put("andrew", EnumSet.of(BasicPermissions.ALL));
      writePermissions(protectedItemPath, permissions);

      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String requestPath = SERVICE_URI + "children/" + folderId;
      ContainerResponse response = launcher.service("GET", requestPath, BASE_URI, null, null, writer, null);
      assertEquals(403, response.getStatus());
      log.info(new String(writer.getBody()));
   }

   public void testGetChildrenPagingSkipCount() throws Exception
   {
      // Get all children.
      String requestPath = SERVICE_URI + "children/" + folderId;
      ContainerResponse response = launcher.service("GET", requestPath, BASE_URI, null, null, null);
      assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
      @SuppressWarnings("unchecked")
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      List<Object> all = new ArrayList<Object>(4);
      for (Item i : children.getItems())
      {
         all.add(i.getName());
      }

      // Remove first name from the list.
      Iterator<Object> iteratorAll = all.iterator();
      iteratorAll.next();
      iteratorAll.remove();

      // Skip first item in result.
      requestPath = SERVICE_URI + "children/" + folderId + '?' + "skipCount=" + 1;
      checkPage(requestPath, "GET", ItemImpl.class.getMethod("getName"), all);
   }

   public void testGetChildrenPagingMaxItems() throws Exception
   {
      // Get all children.
      String requestPath = SERVICE_URI + "children/" + folderId;
      ContainerResponse response = launcher.service("GET", requestPath, BASE_URI, null, null, null);
      assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
      @SuppressWarnings("unchecked")
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      List<Object> all = new ArrayList<Object>(4);
      for (Item i : children.getItems())
      {
         all.add(i.getName());
      }

      all.remove(3);

      // Exclude last item from result.
      requestPath = SERVICE_URI + "children/" + folderId + '?' + "maxItems=" + 3;
      checkPage(requestPath, "GET", ItemImpl.class.getMethod("getName"), all);
   }

   public void testGetChildrenNoPropertyFilter() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      // Get children without filter.
      String requestPath = SERVICE_URI + "children/" + folderId;
      ContainerResponse response = launcher.service("GET", requestPath, BASE_URI, null, null, writer, null);
      log.info(new String(writer.getBody()));
      assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
      @SuppressWarnings("unchecked")
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      assertEquals(4, children.getItems().size());
      for (Item i : children.getItems())
      {
         // No properties without filter. 'none' filter is used if nothing set by client.
         assertNull(i.getPropertyValue("MyProperty01"));
         assertNull(i.getPropertyValue("MyProperty02"));
      }
   }

   public void testGetChildrenPropertyFilter() throws Exception
   {
      Iterator<Map.Entry<String,String[]>> iter = properties.entrySet().iterator();
      Map.Entry<String, String[]> e1 = iter.next();
      Map.Entry<String, String[]> e2 = iter.next();
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      // Get children and apply filter for properties.
      String requestPath = SERVICE_URI + "children/" + folderId + '?' + "propertyFilter=" + e1.getKey();
      ContainerResponse response = launcher.service("GET", requestPath, BASE_URI, null, null, writer, null);
      log.info(new String(writer.getBody()));
      assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
      @SuppressWarnings("unchecked")
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      assertEquals(4, children.getItems().size());
      for (Item i : children.getItems())
      {
         assertNull(i.getPropertyValue(e2.getKey()));
         assertEquals(e1.getValue()[0], i.getPropertyValue(e1.getKey()));
      }
   }

   public void testGetChildrenTypeFilter() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String requestPath = SERVICE_URI + "children/" + folderId + '?' + "itemType=" + "folder";
      ContainerResponse response = launcher.service("GET", requestPath, BASE_URI, null, null, writer, null);
      log.info(new String(writer.getBody()));
      assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
      @SuppressWarnings("unchecked")
      ItemList<Item> children = (ItemList<Item>)response.getEntity();
      assertEquals(2, children.getItems().size());
      for (Item i : children.getItems())
      {
         assertTrue(i.getItemType() == ItemType.FOLDER);
      }
   }
}
