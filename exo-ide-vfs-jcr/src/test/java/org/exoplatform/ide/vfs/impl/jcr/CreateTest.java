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
package org.exoplatform.ide.vfs.impl.jcr;

import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class CreateTest extends JcrFileSystemTest
{
   private String CREATE_TEST_PATH;

   private Node createTestNode;

   /**
    * @see org.exoplatform.ide.vfs.impl.jcr.JcrFileSystemTest#setUp()
    */
   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      String name = getClass().getName();
      createTestNode = testRoot.addNode(name, "nt:unstructured");
      session.save();
      CREATE_TEST_PATH = "/" + TEST_ROOT_NAME + "/" + name;
   }

   public void testCreateFile() throws Exception
   {
      String name = "testCreateFile";
      String content = "test create file";
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("file") //
         .append(CREATE_TEST_PATH) //
         .append("?") //
         .append("name=") //
         .append(name) //
         .append("&") //
         .append("mediaType=") //
         .append("text/plain;charset%3Dutf8").toString();
      ContainerResponse response = launcher.service("POST", path, BASE_URI, null, content.getBytes(), null);
      
      assertEquals(201, response.getStatus());
      String expectedPath = CREATE_TEST_PATH + "/" + name;
      String expectedLocation = SERVICE_URI + "item" + expectedPath;
      String location = response.getHttpHeaders().getFirst("Location").toString();
      
      assertEquals(expectedLocation, location);
      assertTrue("File was not created in expected location. ", session.itemExists(expectedPath));
      Node file = (Node)session.getItem(expectedPath);
      assertEquals("text/plain", file.getNode("jcr:content").getProperty("jcr:mimeType").getString());
      assertEquals("utf8", file.getNode("jcr:content").getProperty("jcr:encoding").getString());
      assertEquals(content, file.getNode("jcr:content").getProperty("jcr:data").getString());
   }

   public void testCreateFileNoContent() throws Exception
   {
      String name = "testCreateFileNoContent";
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("file") //
         .append(CREATE_TEST_PATH) //
         .append("?") //
         .append("name=") //
         .append(name).toString();
      ContainerResponse response = launcher.service("POST", path, BASE_URI, null, null, null);

      assertEquals(201, response.getStatus());
      String expectedPath = CREATE_TEST_PATH + "/" + name;
      String expectedLocation = SERVICE_URI + "item" + expectedPath;
      String location = response.getHttpHeaders().getFirst("Location").toString();
      assertEquals(expectedLocation, location);

      assertTrue("File was not created in expected location. ", session.itemExists(expectedPath));
      Node file = (Node)session.getItem(expectedPath);
      assertEquals(MediaType.APPLICATION_OCTET_STREAM, file.getNode("jcr:content").getProperty("jcr:mimeType")
         .getString());
      assertFalse(file.getNode("jcr:content").hasProperty("jcr:encoding"));
      assertEquals("", file.getNode("jcr:content").getProperty("jcr:data").getString());
   }

   public void testCreateFileNoMediaType() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String name = "testCreateFileNoMediaType";
      String content = "test create file without media type";
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("file") //
         .append(CREATE_TEST_PATH) //
         .append("?") //
         .append("name=") //
         .append(name).toString();
      ContainerResponse response = launcher.service("POST", path, BASE_URI, null, content.getBytes(), writer, null);

      assertEquals(201, response.getStatus());
      String expectedPath = CREATE_TEST_PATH + "/" + name;
      String expectedLocation = SERVICE_URI + "item" + expectedPath;
      String location = response.getHttpHeaders().getFirst("Location").toString();
      assertEquals(expectedLocation, location);

      assertTrue("File was not created in expected location. ", session.itemExists(expectedPath));
      Node file = (Node)session.getItem(expectedPath);
      assertEquals(MediaType.APPLICATION_OCTET_STREAM, file.getNode("jcr:content").getProperty("jcr:mimeType")
         .getString());
      assertFalse(file.getNode("jcr:content").hasProperty("jcr:encoding"));
      assertEquals(content, file.getNode("jcr:content").getProperty("jcr:data").getString());
   }

   public void testCreateFileNoName() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("file") //
         .append(CREATE_TEST_PATH).toString();
      ContainerResponse response =
         launcher.service("POST", path, BASE_URI, null, DEFAULT_CONTENT.getBytes(), writer, null);
      assertEquals(400, response.getStatus());
      log.info(new String(writer.getBody()));
   }

   public void testCreateFileNoPermissions() throws Exception
   {
      Node parent = createTestNode.addNode("testCreateFileNoPermissions_PARENT", "nt:folder");
      parent.addMixin("exo:privilegeable");
      Map<String, String[]> permissions = new HashMap<String, String[]>(1);
      permissions.put("root", PermissionType.ALL);
      ((ExtendedNode)parent).setPermissions(permissions);
      String parentPath = parent.getPath();
      session.save();

      String name = "testCreateFileNoPermissions";
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("file") //
         .append(parentPath) //
         .append("?") //
         .append("name=") //
         .append(name).toString();
      ContainerResponse response =
         launcher.service("POST", path, BASE_URI, null, DEFAULT_CONTENT.getBytes(), writer, null);
      assertEquals(403, response.getStatus());
      log.info(new String(writer.getBody()));
   }

   public void testCreateFileWrongParent() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String name = "testCreateFileWrongParent";
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("file") //
         .append(CREATE_TEST_PATH + "_WRONG_PATH") //
         .append("?") //
         .append("name=") //
         .append(name).toString();
      ContainerResponse response =
         launcher.service("POST", path, BASE_URI, null, DEFAULT_CONTENT.getBytes(), writer, null);
      assertEquals(404, response.getStatus());
      log.info(new String(writer.getBody()));
   }

   public void testCreateFolder() throws Exception
   {
      String name = "testCreateFolder";
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("folder") //
         .append(CREATE_TEST_PATH) //
         .append("?") //
         .append("name=") //
         .append(name).toString();
      ContainerResponse response = launcher.service("POST", path, BASE_URI, null, null, null);

      assertEquals(201, response.getStatus());
      String expectedPath = CREATE_TEST_PATH + "/" + name;
      String expectedLocation = SERVICE_URI + "item" + expectedPath;
      String location = response.getHttpHeaders().getFirst("Location").toString();
      assertEquals(expectedLocation, location);

      assertTrue("Folder was not created in expected location. ", session.itemExists(expectedPath));
      Node folder = (Node)session.getItem(expectedPath);
      assertTrue("nt:folder node type expected", folder.getPrimaryNodeType().isNodeType("nt:folder"));
   }

   public void testCreateFolderNoName() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("folder") //
         .append(CREATE_TEST_PATH).toString();
      ContainerResponse response = launcher.service("POST", path, BASE_URI, null, null, writer, null);
      assertEquals(400, response.getStatus());
      log.info(new String(writer.getBody()));
   }

   public void testCreateFolderNoPermissions() throws Exception
   {
      Node parent = createTestNode.addNode("testCreateFolderNoPermissions_PARENT", "nt:folder");
      parent.addMixin("exo:privilegeable");
      Map<String, String[]> permissions = new HashMap<String, String[]>(1);
      permissions.put("root", PermissionType.ALL);
      ((ExtendedNode)parent).setPermissions(permissions);
      String parentPath = parent.getPath();
      session.save();

      String name = "testCreateFolderNoPermissions";
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("folder") //
         .append(parentPath) //
         .append("?") //
         .append("name=") //
         .append(name).toString();
      ContainerResponse response = launcher.service("POST", path, BASE_URI, null, null, writer, null);
      assertEquals(403, response.getStatus());
      log.info(new String(writer.getBody()));
   }

   public void testCreateFolderWrongParent() throws Exception
   {
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String name = "testCreateFolderWrongParent";
      String path = new StringBuilder() //
         .append(SERVICE_URI) //
         .append("folder") //
         .append(CREATE_TEST_PATH + "_WRONG_PATH") //
         .append("?") //
         .append("name=") //
         .append(name).toString();
      ContainerResponse response = launcher.service("POST", path, BASE_URI, null, null, writer, null);
      assertEquals(404, response.getStatus());
      log.info(new String(writer.getBody()));
   }
}
