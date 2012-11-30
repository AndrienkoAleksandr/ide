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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.input.CountingInputStream;
import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonGenerator;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.JsonWriter;
import org.everrest.core.impl.provider.json.ObjectBuilder;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.ide.vfs.server.ContentStream;
import org.exoplatform.ide.vfs.server.LazyIterator;
import org.exoplatform.ide.vfs.server.VirtualFileSystem;
import org.exoplatform.ide.vfs.server.VirtualFileSystemFactory;
import org.exoplatform.ide.vfs.server.exceptions.ConstraintException;
import org.exoplatform.ide.vfs.server.exceptions.InvalidArgumentException;
import org.exoplatform.ide.vfs.server.exceptions.ItemAlreadyExistException;
import org.exoplatform.ide.vfs.server.exceptions.ItemNotFoundException;
import org.exoplatform.ide.vfs.server.exceptions.LockException;
import org.exoplatform.ide.vfs.server.exceptions.NotSupportedException;
import org.exoplatform.ide.vfs.server.exceptions.PermissionDeniedException;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemRuntimeException;
import org.exoplatform.ide.vfs.server.observation.ChangeEvent;
import org.exoplatform.ide.vfs.server.observation.EventListenerList;
import org.exoplatform.ide.vfs.shared.AccessControlEntry;
import org.exoplatform.ide.vfs.shared.ExitCodes;
import org.exoplatform.ide.vfs.shared.File;
import org.exoplatform.ide.vfs.shared.FileImpl;
import org.exoplatform.ide.vfs.shared.FolderImpl;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.ItemList;
import org.exoplatform.ide.vfs.shared.ItemListImpl;
import org.exoplatform.ide.vfs.shared.ItemNode;
import org.exoplatform.ide.vfs.shared.ItemNodeImpl;
import org.exoplatform.ide.vfs.shared.ItemType;
import org.exoplatform.ide.vfs.shared.Link;
import org.exoplatform.ide.vfs.shared.LinkImpl;
import org.exoplatform.ide.vfs.shared.LockToken;
import org.exoplatform.ide.vfs.shared.LockTokenImpl;
import org.exoplatform.ide.vfs.shared.Project;
import org.exoplatform.ide.vfs.shared.ProjectImpl;
import org.exoplatform.ide.vfs.shared.Property;
import org.exoplatform.ide.vfs.shared.PropertyFilter;
import org.exoplatform.ide.vfs.shared.PropertyImpl;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo.ACLCapability;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo.BasicPermissions;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo.QueryCapability;
import org.exoplatform.ide.vfs.shared.VirtualFileSystemInfoImpl;
import org.exoplatform.services.jcr.core.ExtendedSession;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: JcrFileSystem.java 80595 2012-03-27 09:12:25Z azatsarynnyy $
 */
public class JcrFileSystem implements VirtualFileSystem
{
   enum Resolver
   {
      INSTANCE;
      /*=====================================*/
      final MimeTypeResolver resolver;

      private Resolver()
      {
         resolver = new MimeTypeResolver();
         resolver.setDefaultMimeType("text/plain");
      }

      public MediaType getMediaType(String filename)
      {
         return MediaType.valueOf(resolver.getMimeType(filename));
      }
   }

   static final Set<String> SKIPPED_QUERY_PROPERTIES = new HashSet<String>(Arrays.asList("jcr:path", "jcr:score"));
   
   private static final Log LOG_JREBEL_PROP = ExoLogger.getLogger("JRebel");

   protected final Repository repository;
   protected final String workspaceName;
   protected final MediaType2NodeTypeResolver mediaType2NodeTypeResolver;
   protected final URI baseUri;
   protected final EventListenerList listeners;
   private final String rootNodePath;

   private VirtualFileSystemInfo vfsInfo;
   private final String vfsID;

   public JcrFileSystem(Repository repository,
                        String workspaceName,
                        String rootNodePath,
                        String vfsID,
                        MediaType2NodeTypeResolver mediaType2NodeTypeResolver,
                        URI baseUri,
                        EventListenerList listeners)
   {
      this.repository = repository;
      this.workspaceName = workspaceName;
      if (rootNodePath != null && rootNodePath.length() > 1 && rootNodePath.endsWith("/"))
      {
         rootNodePath = rootNodePath.substring(0, rootNodePath.length() - 1);
      }
      this.rootNodePath = rootNodePath;
      this.vfsID = vfsID;
      this.mediaType2NodeTypeResolver = mediaType2NodeTypeResolver;
      this.baseUri = baseUri;
      this.listeners = listeners;
   }

   public JcrFileSystem(Repository repository,
                        String workspaceName,
                        String rootNodePath,
                        String vfsID,
                        MediaType2NodeTypeResolver mediaType2NodeTypeResolver,
                        URI baseUri)
   {
      this(repository, workspaceName, rootNodePath, vfsID, mediaType2NodeTypeResolver, baseUri, null);
   }

   public JcrFileSystem(Repository repository,
                        String workspaceName,
                        String rootNodePath,
                        String vfsID,
                        MediaType2NodeTypeResolver mediaType2NodeTypeResolver)
   {
      this(repository, workspaceName, rootNodePath, vfsID, mediaType2NodeTypeResolver, URI.create(""));
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#copy(java.lang.String, java.lang.String) */
   @Path("copy/{id}")
   @Override
   public Item copy(@PathParam("id") String id, //
                    @QueryParam("parentId") String parentId //
   ) throws ItemNotFoundException, ConstraintException, ItemAlreadyExistException, PermissionDeniedException,
      VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData object = getItemData(session, id);
         ItemData parent = getItemData(session, parentId);
         if (!(ItemType.PROJECT == parent.getType() || ItemType.FOLDER == parent.getType()))
         {
            throw new InvalidArgumentException("Unable copy. Item specified as parent is not a folder or project. ");
         }
         if (ItemType.PROJECT == parent.getType() && ItemType.PROJECT == object.getType())
         {
            throw new ConstraintException("Unable copy. Item specified as parent is a project. "
               + "Project cannot contains another project.");
         }
         ItemData newObject = object.copyTo((FolderData)parent);
         Item copy = fromItemData(newObject, PropertyFilter.ALL_FILTER);
         notifyListeners(new ChangeEvent(this, //
            copy.getId(), //
            copy.getPath(), //
            copy.getMimeType(), //
            ChangeEvent.ChangeType.CREATED)
         );
         return copy;
      }
      finally
      {
         session.logout();
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.server.VirtualFileSystem#createFile(String, String,
    *      javax.ws.rs.core.MediaType, java.io.InputStream)
    */
   @Path("file/{parentId}")
   @Override
   public File createFile(@PathParam("parentId") String parentId, //
                          @QueryParam("name") String name, //
                          @DefaultValue(MediaType.APPLICATION_OCTET_STREAM) @HeaderParam("Content-Type") MediaType mediaType, //
                          InputStream content //
   ) throws ItemNotFoundException, InvalidArgumentException, ItemAlreadyExistException, PermissionDeniedException,
      VirtualFileSystemException
   {
      checkName(name);
      Session session = session();
      try
      {
         ItemData parentData = getItemData(session, parentId);
         if (!(ItemType.FOLDER == parentData.getType() || ItemType.PROJECT == parentData.getType()))
         {
            throw new InvalidArgumentException("Unable create file. Item specified as parent is not a folder or project. ");
         }
         FileData newFile = ((FolderData)parentData).createFile(name, //
            mediaType2NodeTypeResolver.getFileNodeType(mediaType), //
            mediaType2NodeTypeResolver.getFileContentNodeType(mediaType), //
            mediaType, //
            mediaType2NodeTypeResolver.getFileMixins(mediaType), //
            null, //
            content);
         File file = (File)fromItemData(newFile, PropertyFilter.ALL_FILTER);
         notifyListeners(new ChangeEvent(this, //
            file.getId(), //
            file.getPath(), //
            file.getMimeType(), //
            ChangeEvent.ChangeType.CREATED)
         );
         return file;
      }
      finally
      {
         session.logout();
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#createFolder(String, String) */
   @Path("folder/{parentId}")
   @Override
   public FolderImpl createFolder(@PathParam("parentId") String parentId, //
                              @QueryParam("name") String name //
   ) throws ItemNotFoundException, InvalidArgumentException, PermissionDeniedException, VirtualFileSystemException
   {
      checkName(name);
      Session session = session();
      try
      {
         ItemData parentData = getItemData(session, parentId);
         if (!(ItemType.FOLDER == parentData.getType() || ItemType.PROJECT == parentData.getType()))
         {
            throw new InvalidArgumentException("Unable create folder. Item specified as parent is not a folder or project. ");
         }
         FolderData newFolder = ((FolderData)parentData).createFolder(name, //
            mediaType2NodeTypeResolver.getFolderNodeType((String)null), //
            mediaType2NodeTypeResolver.getFolderMixins((String)null), //
            null);
         FolderImpl folder = (FolderImpl)fromItemData(newFolder, PropertyFilter.ALL_FILTER);
         notifyListeners(new ChangeEvent(this, //
            folder.getId(), //
            folder.getPath(), //
            folder.getMimeType(), //
            ChangeEvent.ChangeType.CREATED) //
         );
         return folder;
      }
      finally
      {
         session.logout();
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.server.VirtualFileSystem#createProject(String, String,
    *      String, java.util.List)
    */
   @Path("project/{parentId}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Override
   public Project createProject(@PathParam("parentId") String parentId, //
                                @QueryParam("name") String name, //
                                @QueryParam("type") String type, //
                                List<Property> properties //
   ) throws ItemNotFoundException, InvalidArgumentException, ItemAlreadyExistException, PermissionDeniedException,
      VirtualFileSystemException
   {
      checkName(name);
      Session session = session();
      try
      {
         ItemData parentData = getItemData(session, parentId);
//         if (ItemType.PROJECT == parentData.getType())
//         {
//            throw new ConstraintException("Unable create project. Item specified as parent is a project. "
//               + "Project cannot contains another project.");
//         }
//         if (ItemType.FOLDER != parentData.getType())
//         {
//            throw new InvalidArgumentException("Unable create project. Item specified as parent is not a folder. ");
//         }
         if (properties == null)
         {
            properties = new ArrayList<Property>(2);
         }
         if (type != null)
         {
            properties.add(new PropertyImpl("vfs:projectType", type));
         }
         properties.add(new PropertyImpl("vfs:mimeType", Project.PROJECT_MIME_TYPE));

         FolderData newProject = ((FolderData)parentData).createFolder(name, //
            mediaType2NodeTypeResolver.getFolderNodeType((String)null), //
            mediaType2NodeTypeResolver.getFolderMixins(Project.PROJECT_MIME_TYPE), //
            properties);
         ProjectImpl project = (ProjectImpl)fromItemData(newProject, PropertyFilter.ALL_FILTER);
         notifyListeners(new ChangeEvent(this, //
            project.getId(), //
            project.getPath(), //
            project.getMimeType(), //
            ChangeEvent.ChangeType.CREATED)
         );
         return project;
      }
      finally
      {
         session.logout();
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#delete(java.lang.String, java.lang.String) */
   @Path("delete/{id}")
   @Override
   public void delete(@PathParam("id") String id, //
                      @QueryParam("lockToken") String lockToken //
   ) throws ItemNotFoundException, ConstraintException, LockException, PermissionDeniedException,
      VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemData(session, id);
         String path = data.getPath();
         MediaType mediaType = data.getMediaType();
         if (listeners != null && data.getType() == ItemType.PROJECT)
         {
            listeners.removeEventListener(ProjectUpdateEventFilter.newFilter(this, (ProjectData)data), new ProjectUpdateListener(id));
         }
         data.delete(lockToken);
         notifyListeners(new ChangeEvent(this, //
            id, //
            path, //
            mediaType != null ? mediaType.toString() : null, //
            ChangeEvent.ChangeType.DELETED)
         );
      }
      finally
      {
         session.logout();
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#getACL(java.lang.String) */
   @Path("acl/{id}")
   @Override
   public List<AccessControlEntry> getACL(@PathParam("id") String id) throws NotSupportedException,
      ItemNotFoundException, PermissionDeniedException, VirtualFileSystemException
   {
      Session session = session();
      try
      {
         return getItemData(session, id).getACL();
      }
      finally
      {
         session.logout();
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.server.VirtualFileSystem#getChildren(java.lang.String, int, int, String,
    *      org.exoplatform.ide.vfs.shared.PropertyFilter)
    */
   @Path("children/{id}")
   @Override
   public ItemList<Item> getChildren(@PathParam("id") String folderId, //
                                     @DefaultValue("-1") @QueryParam("maxItems") int maxItems, //
                                     @QueryParam("skipCount") int skipCount, //
                                     @QueryParam("itemType") String itemType, //
                                     @DefaultValue(PropertyFilter.NONE) @QueryParam("propertyFilter") PropertyFilter propertyFilter //
   ) throws ItemNotFoundException, InvalidArgumentException, PermissionDeniedException, VirtualFileSystemException
   {
      if (skipCount < 0)
      {
         throw new InvalidArgumentException("'skipCount' parameter is negative. ");
      }

      ItemType itemTypeType;
      if (itemType != null)
      {
         try
         {
            itemTypeType = ItemType.fromValue(itemType);
         }
         catch (IllegalArgumentException e)
         {
            throw new InvalidArgumentException("Unknown type: " + itemType);
         }
      }
      else
      {
         itemTypeType = null;
      }

      Session session = session();
      try
      {
         ItemData data = getItemData(session, folderId);
         if (!(ItemType.FOLDER == data.getType() || ItemType.PROJECT == data.getType()))
         {
            throw new InvalidArgumentException("Unable get children. Item " + data.getName()
               + " is not a folder or project. ");
         }

         FolderData folderData = (FolderData)data;
         LazyIterator<ItemData> children = folderData.getChildren(itemTypeType);
         try
         {
            if (skipCount > 0)
            {
               children.skip(skipCount);
            }
         }
         catch (NoSuchElementException nse)
         {
            throw new InvalidArgumentException("'skipCount' parameter is greater then total number of items. ");
         }

         List<Item> l = new ArrayList<Item>();
         for (int count = 0; children.hasNext() && (maxItems < 0 || count < maxItems); count++)
         {
            l.add(fromItemData(children.next(), propertyFilter));
         }

         ItemList<Item> il = new ItemListImpl<Item>(l);
         il.setNumItems(children.size());
         il.setHasMoreItems(children.hasNext());

         return il;
      }
      finally
      {
         session.logout();
      }
   }

   @GET
   @Path("tree/{id}")
   @Produces({MediaType.APPLICATION_JSON})
   public ItemNode getTree(@PathParam("id") String folderId,
                           @DefaultValue("-1") @QueryParam("depth") int depth,
                           @DefaultValue(PropertyFilter.NONE) @QueryParam("propertyFilter") PropertyFilter propertyFilter)
      throws ItemNotFoundException, InvalidArgumentException, PermissionDeniedException, VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemData(session, folderId);
         if (!(ItemType.FOLDER == data.getType() || ItemType.PROJECT == data.getType()))
         {
            throw new InvalidArgumentException("Unable get tree. Item " + data.getName()
               + " is not a folder or project. ");
         }
         return new ItemNodeImpl(fromItemData(data, propertyFilter), getTreeLevel(data, depth, propertyFilter));
      }
      finally
      {
         session.logout();
      }
   }

   private List<ItemNode> getTreeLevel(ItemData data, int depth, PropertyFilter propertyFilter)
      throws VirtualFileSystemException
   {
      if (depth == 0 || data.getType() == ItemType.FILE)
      {
         return null;
      }
      FolderData folder = (FolderData)data;
      LazyIterator<ItemData> children = folder.getChildren(null);
      List<ItemNode> level = new ArrayList<ItemNode>();
      while (children.hasNext())
      {
         ItemData c = children.next();
         level.add(new ItemNodeImpl(fromItemData(c, propertyFilter), getTreeLevel(c, depth - 1, propertyFilter)));
      }
      return level;
   }


   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#getContent(java.lang.String) */
   @Path("content/{id}")
   @Override
   public ContentStream getContent(@PathParam("id") String id) throws ItemNotFoundException, InvalidArgumentException,
      PermissionDeniedException, VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemData(session, id);
         if (ItemType.FILE != data.getType())
         {
            throw new InvalidArgumentException("Unable get content. Item " + data.getName() + " is not a file. ");
         }
         FileData fileData = (FileData)data;
         return new ContentStream(fileData.getName(), fileData.getContent(), fileData.getMediaType().toString(),
            fileData.getContentLength(), new Date(fileData.getLastModificationDate()));
      }
      finally
      {
         session.logout();
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#getContent(java.lang.String, java.lang.String) */
   @Override
   @Path("contentbypath/{path:.*}")
   public ContentStream getContent(@PathParam("path") String path, //
                                   @QueryParam("versionId") String versionId //
   ) throws ItemNotFoundException, InvalidArgumentException, PermissionDeniedException, VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemDataByPath(session, path);
         if (ItemType.FILE != data.getType())
         {
            throw new InvalidArgumentException("Unable get content. Item " + data.getName() + " is not a file. ");
         }
         FileData fileData = (FileData)data;
         ContentStream stream;
         if (versionId != null)
         {
            FileData version = fileData.getVersion(versionId);
            stream =
               new ContentStream(version.getName(), version.getContent(), version.getMediaType().toString(),
                  version.getContentLength(), new java.util.Date(version.getLastModificationDate()));
         }
         else
         {
            stream =
               new ContentStream(fileData.getName(), fileData.getContent(), fileData.getMediaType().toString(),
                  fileData.getContentLength(), new java.util.Date(fileData.getLastModificationDate()));
         }
         return stream;
      }
      finally
      {
         session.logout();
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#getInfo() */
   @Override
   public VirtualFileSystemInfo getInfo() throws VirtualFileSystemException
   {
      if (vfsInfo == null)
      {
         BasicPermissions[] basicPermissions = BasicPermissions.values();
         List<String> permissions = new ArrayList<String>(basicPermissions.length);
         for (BasicPermissions bp : basicPermissions)
         {
            permissions.add(bp.value());
         }
         Session session = session();
         FolderImpl root;
         try
         {
            root = (FolderImpl)fromItemData(getItemDataByPath(session, "/"), PropertyFilter.valueOf(PropertyFilter.ALL));
         }
         finally
         {
            session.logout();
         }
         vfsInfo =
            new VirtualFileSystemInfoImpl(this.vfsID, true, true,
               org.exoplatform.services.security.IdentityConstants.ANONIM,
               org.exoplatform.services.security.IdentityConstants.ANY, permissions, ACLCapability.MANAGE,
               QueryCapability.BOTHCOMBINED, createUrlTemplates(), root);
      }
      return vfsInfo;
   }

   private Map<String, Link> createUrlTemplates()
   {
      Map<String, Link> templates = new HashMap<String, Link>();

      templates.put(Link.REL_ITEM, //
         new LinkImpl(createURI("item", "[id]"), Link.REL_ITEM, MediaType.APPLICATION_JSON));

      templates.put(Link.REL_ITEM_BY_PATH, //
         new LinkImpl(createURI("itembypath", "[path]"), Link.REL_ITEM_BY_PATH, MediaType.APPLICATION_JSON));
      
      templates.put(Link.REL_TREE, //
         new LinkImpl(createURI("tree", "[id]"), Link.REL_TREE, MediaType.APPLICATION_JSON));

      templates.put(Link.REL_CREATE_FILE, //
         new LinkImpl(createURI("file", "[parentId]", "name", "[name]"), //
            Link.REL_CREATE_FILE, MediaType.APPLICATION_JSON));

      templates.put(Link.REL_CREATE_FOLDER, //
         new LinkImpl(createURI("folder", "[parentId]", "name", "[name]"), //
            Link.REL_CREATE_FOLDER, MediaType.APPLICATION_JSON));

      templates.put(Link.REL_CREATE_PROJECT, //
         new LinkImpl(createURI("project", "[parentId]", "name", "[name]", "type", "[type]"), //
            Link.REL_CREATE_PROJECT, MediaType.APPLICATION_JSON));

      templates.put(Link.REL_COPY, //
         new LinkImpl(createURI("copy", "[id]", "parentId", "[parentId]"), //
            Link.REL_COPY, MediaType.APPLICATION_JSON));

      templates.put(Link.REL_MOVE, //
         new LinkImpl(createURI("move", "[id]", "parentId", "[parentId]", "lockToken", "[lockToken]"), //
            Link.REL_MOVE, MediaType.APPLICATION_JSON));

      templates.put(Link.REL_LOCK, //
         new LinkImpl(createURI("lock", "[id]"), //
            Link.REL_LOCK, MediaType.APPLICATION_JSON));

      templates.put(Link.REL_UNLOCK, //
         new LinkImpl(createURI("unlock", "[id]", "lockToken", "[lockToken]"), //
            Link.REL_UNLOCK, null));

      templates.put(
         Link.REL_SEARCH_FORM, //
         new LinkImpl(createURI("search", null, "maxItems", "[maxItems]", "skipCount", "[skipCount]", "propertyFilter",
            "[propertyFilter]"), //
            Link.REL_SEARCH_FORM, MediaType.APPLICATION_JSON));

      templates.put(
         Link.REL_SEARCH, //
         new LinkImpl(createURI("search", null, "statement", "[statement]", "maxItems", "[maxItems]", "skipCount",
            "[skipCount]"), //
            Link.REL_SEARCH, MediaType.APPLICATION_JSON));

      return templates;
   }

   /**
    * @see org.exoplatform.ide.vfs.server.VirtualFileSystem#getItem(java.lang.String,
    *      org.exoplatform.ide.vfs.shared.PropertyFilter)
    */
   @Path("item/{id}")
   @Override
   public Item getItem(@PathParam("id") String id, //
                       @DefaultValue("*") @QueryParam("propertyFilter") PropertyFilter propertyFilter //
   ) throws ItemNotFoundException, PermissionDeniedException, VirtualFileSystemException
   {
      Session session = session();
      try
      {
         return fromItemData(getItemData(session, id), propertyFilter);
      }
      finally
      {
         session.logout();
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.server.VirtualFileSystem#getItemByPath(java.lang.String, java.lang.String,
    *      org.exoplatform.ide.vfs.shared.PropertyFilter)
    */
   @Path("itembypath/{path:.*}")
   @Override
   public Item getItemByPath(
      @PathParam("path") String path, //
      @QueryParam("versionId") String versionId,
      @DefaultValue("*") @QueryParam("propertyFilter") PropertyFilter propertyFilter) throws ItemNotFoundException,
      PermissionDeniedException, VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemDataByPath(session, path);
         if (versionId != null && ItemType.FILE == data.getType())
         {
            FileData version = ((FileData)data).getVersion(versionId);
            return fromItemData(version, propertyFilter);
         }
         else if (versionId != null)
         {
            throw new InvalidArgumentException("Object " + path + " is not a file. Version ID must not be set. ");
         }
         return fromItemData(data, propertyFilter);
      }
      finally
      {
         session.logout();
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.server.VirtualFileSystem#getVersion(java.lang.String,
    *      java.lang.String)
    */
   @Path("version/{id}/{versionId}")
   @Override
   public ContentStream getVersion(@PathParam("id") String id, //
                                   @PathParam("versionId") String versionId //
   ) throws ItemNotFoundException, InvalidArgumentException, PermissionDeniedException, VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemData(session, id);
         if (ItemType.FILE != data.getType())
         {
            throw new InvalidArgumentException("Object " + data.getName() + " is not a file. ");
         }
         FileData versionData = ((FileData)data).getVersion(versionId);
         return new ContentStream(versionData.getName(), versionData.getContent(), versionData.getMediaType().toString(),
            versionData.getContentLength(), new Date(versionData.getLastModificationDate()));
      }
      finally
      {
         session.logout();
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.server.VirtualFileSystem#getVersions(java.lang.String, int, int,
    *      org.exoplatform.ide.vfs.shared.PropertyFilter)
    */
   @Path("version-history/{id}")
   @Override
   public ItemList<File> getVersions(@PathParam("id") String id, //
                                     @DefaultValue("-1") @QueryParam("maxItems") int maxItems, //
                                     @QueryParam("skipCount") int skipCount, //
                                     @DefaultValue("*") @QueryParam("propertyFilter") PropertyFilter propertyFilter //
   ) throws ItemNotFoundException, InvalidArgumentException, PermissionDeniedException, VirtualFileSystemException
   {
      if (skipCount < 0)
      {
         throw new InvalidArgumentException("'skipCount' parameter is negative. ");
      }

      Session session = session();
      try
      {
         ItemData data = getItemData(session, id);
         if (ItemType.FILE != data.getType())
         {
            throw new InvalidArgumentException("Object " + data.getName() + " is not a file. ");
         }

         FileData fileData = (FileData)data;
         LazyIterator<FileData> versions = fileData.getAllVersions();
         try
         {
            if (skipCount > 0)
            {
               versions.skip(skipCount);
            }
         }
         catch (NoSuchElementException nse)
         {
            throw new InvalidArgumentException("'skipCount' parameter is greater then total number of items. ");
         }

         List<File> l = new ArrayList<File>();
         for (int count = 0; versions.hasNext() && (maxItems < 0 || count < maxItems); count++)
         {
            l.add((File)fromItemData(versions.next(), propertyFilter));
         }

         ItemList<File> il = new ItemListImpl<File>(l);
         il.setNumItems(versions.size());
         il.setHasMoreItems(versions.hasNext());

         return il;
      }
      finally
      {
         session.logout();
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#lock(java.lang.String) */
   @Path("lock/{id}")
   @Override
   public LockToken lock(@PathParam("id") String id) throws NotSupportedException, ItemNotFoundException,
      InvalidArgumentException, LockException, PermissionDeniedException, VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemData(session, id);
         if (ItemType.FILE != data.getType())
         {
            throw new InvalidArgumentException("Locking allowed for Files only. ");
         }
         return new LockTokenImpl(((FileData)data).lock());
      }
      finally
      {
         session.logout();
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#move(java.lang.String, java.lang.String, java.lang.String) */
   @Path("move/{id}")
   @Override
   public Item move(@PathParam("id") String id, //
                    @QueryParam("parentId") String parentId, //
                    @QueryParam("lockToken") String lockToken //
   ) throws ItemNotFoundException, ConstraintException, LockException, ItemAlreadyExistException,
      PermissionDeniedException, VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData object = getItemData(session, id);
         ItemData parent = getItemData(session, parentId);
         if (!(ItemType.PROJECT == parent.getType() || ItemType.FOLDER == parent.getType()))
         {
            throw new InvalidArgumentException("Unable move. Item specified as parent is not a folder or project. ");
         }
         if (ItemType.PROJECT == parent.getType() && ItemType.PROJECT == object.getType())
         {
            throw new ConstraintException("Unable move. Item specified as parent is not a folder. " +
               "Project cannot be moved to another one. ");
         }
         String movedId = object.moveTo((FolderData)parent, lockToken);
         Item moved = fromItemData(getItemData(session, movedId), PropertyFilter.ALL_FILTER);
         notifyListeners(new ChangeEvent(this, //
            moved.getId(), //
            moved.getPath(), //
            moved.getMimeType(), //
            ChangeEvent.ChangeType.MOVED)
         );
         return moved;
      }
      finally
      {
         session.logout();
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.server.VirtualFileSystem#rename(java.lang.String, javax.ws.rs.core.MediaType,
    *      java.lang.String, java.lang.String)
    */
   @Path("rename/{id}")
   @Override
   public Item rename(@PathParam("id") String id, //
                      @QueryParam("mediaType") MediaType newMediaType, //
                      @QueryParam("newname") String newname, //
                      @QueryParam("lockToken") String lockToken //
   ) throws ItemNotFoundException, InvalidArgumentException, LockException, PermissionDeniedException,
      VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemData(session, id);
         MediaType currentMediaType = data.getMediaType();
         String[] removeMixinTypes = null;
         String[] addMixinTypes = null;
         if (newMediaType != null && !compareMediaTypeIgnoreParameters(currentMediaType, newMediaType))
         {
            removeMixinTypes =
               data.getType() == ItemType.FILE ? mediaType2NodeTypeResolver.getFileMixins(currentMediaType)
                  : mediaType2NodeTypeResolver.getFolderMixins(currentMediaType);
            addMixinTypes =
               data.getType() == ItemType.FILE ? mediaType2NodeTypeResolver.getFileMixins(newMediaType)
                  : mediaType2NodeTypeResolver.getFolderMixins(newMediaType);
         }
         String renamedId = data.rename(newname, newMediaType, lockToken, addMixinTypes, removeMixinTypes);
         Item renamed = fromItemData(getItemData(session, renamedId), PropertyFilter.ALL_FILTER);
         notifyListeners(new ChangeEvent(this, //
            renamed.getId(), //
            renamed.getPath(), //
            renamed.getMimeType(), //
            ChangeEvent.ChangeType.RENAMED)
         );
         return renamed;
      }
      finally
      {
         session.logout();
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.server.VirtualFileSystem#search(javax.ws.rs.core.MultivaluedMap, int, int,
    *      org.exoplatform.ide.vfs.shared.PropertyFilter)
    */
   @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
   @Override
   public ItemList<Item> search(MultivaluedMap<String, String> query, //
                                @DefaultValue("-1") @QueryParam("maxItems") int maxItems, //
                                @QueryParam("skipCount") int skipCount, //
                                @DefaultValue("*") @QueryParam("propertyFilter") PropertyFilter propertyFilter //
   ) throws NotSupportedException, InvalidArgumentException, VirtualFileSystemException
   {
      if (skipCount < 0)
      {
         throw new InvalidArgumentException("'skipCount' parameter is negative. ");
      }

      List<String> where = null;
      String nodeType = null;
      if (query != null)
      {
         nodeType = query.getFirst("nodeType");
         where = new ArrayList<String>();
         String path = query.getFirst("path");
         if (path != null && path.length() > 0)
         {
            where.add("jcr:path LIKE '" + path + "/%'");
         }
         String text = query.getFirst("text");
         if (text != null && text.length() > 0)
         {
            StringBuilder escape = new StringBuilder();
            for (int i = 0; i < text.length(); i++)
            {
               char c = text.charAt(i);
               // Escaping Special Characters.
               // Lucene supports escaping special characters that are part of the query syntax.
               // The current list special characters: + - && || ! ( ) { } [ ] ^ " ~ * ? : \
               // To escape these character use the \ before the character.
               // For example to search for (1+1):2 use the query: \(1\+1\)\:2
               // NOTE: ~ not escape because it escape in JCR code
               if ("*+-&!.{}:()[]^$|".indexOf(c) != -1)
               {
                  escape.append('\\');
               }
               escape.append(c);
            }
            where.add("CONTAINS(*, '" + escape.toString() + "')");
         }
         String mediaType = query.getFirst("mediaType");
         if (mediaType != null && mediaType.length() > 0)
         {
            where.add("jcr:mimeType = '" + mediaType + "'");
         }
      }
      StringBuilder sql = new StringBuilder();
      sql.append("SELECT * FROM ");
      sql.append((nodeType != null && nodeType.length() > 0) ? nodeType : "nt:resource");
      if (where != null && where.size() > 0)
      {
         sql.append(" WHERE ");
         for (int i = 0; i < where.size(); i++)
         {
            if (i > 0)
            {
               sql.append(" AND ");
            }
            sql.append(where.get(i));
         }
      }

      Session session = session();
      try
      {
         QueryManager queryManager = session.getWorkspace().getQueryManager();
         Query jcrQuery = queryManager.createQuery(sql.toString(), Query.SQL);
         QueryResult result = jcrQuery.execute();

         NodeIterator nodes = result.getNodes();
         try
         {
            if (skipCount > 0)
            {
               nodes.skip(skipCount);
            }
         }
         catch (NoSuchElementException nse)
         {
            throw new InvalidArgumentException("'skipCount' parameter is greater then total number of items. ");
         }

         String name = query.getFirst("name");
         Pattern namePattern = null;
         if (name != null && name.length() > 0)
         {
            StringBuilder pb = new StringBuilder();
            pb.append('.');
            pb.append('*');
            for (int i = 0; i < name.length(); i++)
            {
               char c = name.charAt(i);
               if (c == '*' || c == '?')
               {
                  pb.append('.');
               }
               if (".()[]^$|".indexOf(c) != -1)
               {
                  pb.append('\\');
               }
               pb.append(c);
            }
            pb.append('.');
            pb.append('*');
            namePattern = Pattern.compile(pb.toString(), Pattern.CASE_INSENSITIVE);
         }

         List<Item> l = new ArrayList<Item>();
         String rootPath = getJcrPath(session.getUserID(), "/");
         for (int count = 0; nodes.hasNext() && (maxItems < 0 || count < maxItems); count++)
         {
            ItemData data = ItemData.fromNode(nodes.nextNode(), rootPath);
            if (namePattern == null || namePattern.matcher(data.getName()).matches())
            {
               l.add(fromItemData(data, propertyFilter));
            }
         }

         ItemList<Item> il = new ItemListImpl<Item>(l);
         if (namePattern == null) // Total number is unknown since we apply additional filtering by name. 
         {
            il.setNumItems((int)nodes.getSize());
         }
         il.setHasMoreItems(nodes.hasNext());

         return il;
      }
      catch (InvalidQueryException e)
      {
         throw new InvalidArgumentException(e.getMessage());
      }
      catch (RepositoryException e)
      {
         throw new VirtualFileSystemException(e.getMessage(), e);
      }
      finally
      {
         session.logout();
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#search(java.lang.String, int, int) */
   @Override
   public ItemList<Item> search(@QueryParam("statement") String statement, //
                                @DefaultValue("-1") @QueryParam("maxItems") int maxItems, //
                                @QueryParam("skipCount") int skipCount //
   ) throws NotSupportedException, InvalidArgumentException, VirtualFileSystemException
   {
      if (skipCount < 0)
      {
         throw new InvalidArgumentException("'skipCount' parameter is negative. ");
      }
      Session session = session();
      try
      {
         QueryManager queryManager = session.getWorkspace().getQueryManager();
         Query query = queryManager.createQuery(statement, Query.SQL);
         QueryResult result = query.execute();
         NodeIterator nodes = result.getNodes();
         try
         {
            if (skipCount > 0)
            {
               nodes.skip(skipCount);
            }
         }
         catch (NoSuchElementException nse)
         {
            throw new InvalidArgumentException("'skipCount' parameter is greater then total number of items. ");
         }

         List<Item> l = new ArrayList<Item>();
         StringBuilder propertyFilterBuilder = new StringBuilder();
         for (String n : result.getColumnNames())
         {
            if (SKIPPED_QUERY_PROPERTIES.contains(n))
            {
               continue;
            }
            if (propertyFilterBuilder.length() > 0)
            {
               propertyFilterBuilder.append(',');
            }
            propertyFilterBuilder.append(n);
         }
         PropertyFilter propertyFilter = PropertyFilter.valueOf(propertyFilterBuilder.toString());
         String rootPath = getJcrPath(session.getUserID(), "/");
         for (int count = 0; nodes.hasNext() && (maxItems < 0 || count < maxItems); count++)
         {
            l.add(fromItemData(ItemData.fromNode(nodes.nextNode(), rootPath), propertyFilter));
         }

         ItemList<Item> il = new ItemListImpl<Item>(l);
         il.setNumItems((int)nodes.getSize());
         il.setHasMoreItems(nodes.hasNext());

         return il;
      }
      catch (InvalidQueryException e)
      {
         throw new InvalidArgumentException(e.getMessage());
      }
      catch (RepositoryException e)
      {
         throw new VirtualFileSystemException(e.getMessage(), e);
      }
      finally
      {
         session.logout();
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#unlock(java.lang.String, java.lang.String) */
   @Path("unlock/{id}")
   @Override
   public void unlock(@PathParam("id") String id, //
                      @QueryParam("lockToken") String lockToken //
   ) throws NotSupportedException, ItemNotFoundException, LockException, PermissionDeniedException,
      VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemData(session, id);
         if (data.getType() != ItemType.FILE)
         {
            throw new LockException("Object is not locked. "); // Folder can't be locked.
         }
         ((FileData)data).unlock(lockToken);
      }
      finally
      {
         session.logout();
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.server.VirtualFileSystem#updateACL(java.lang.String, java.util.List,
    *      java.lang.Boolean, java.lang.String)
    */
   @Path("acl/{id}")
   @Override
   public void updateACL(@PathParam("id") String id, //
                         List<AccessControlEntry> acl, //
                         @DefaultValue("false") @QueryParam("override") Boolean override, //
                         @QueryParam("lockToken") String lockToken //
   ) throws NotSupportedException, ItemNotFoundException, LockException, PermissionDeniedException,
      VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemData(session, id);
         data.updateACL(acl, override, lockToken);
         MediaType mediaType = data.getMediaType();
         notifyListeners(new ChangeEvent(this, //
            data.getId(), //
            data.getPath(), //
            mediaType != null ? mediaType.toString() : null, //
            ChangeEvent.ChangeType.ACL_UPDATED)
         );

      }
      finally
      {
         session.logout();
      }
   }

   @Path("content/{id}")
   @Override
   public void updateContent(
      @PathParam("id") String id, //
      @DefaultValue(MediaType.APPLICATION_OCTET_STREAM) @HeaderParam("Content-Type") MediaType mediaType,
      InputStream newContent, //
      @QueryParam("lockToken") String lockToken //
   ) throws ItemNotFoundException, InvalidArgumentException, LockException, PermissionDeniedException,
      VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemData(session, id);
         if (ItemType.FILE != data.getType())
         {
            throw new InvalidArgumentException("Object " + data.getName() + " is not file. ");
         }
         ((FileData)data).setContent(newContent, mediaType, lockToken);
         mediaType = data.getMediaType();
         notifyListeners(new ChangeEvent(this, //
            data.getId(), //
            data.getPath(), //
            mediaType != null ? mediaType.toString() : null, //
            ChangeEvent.ChangeType.CONTENT_UPDATED)
         );
      }
      finally
      {
         session.logout();
      }
   }

   /**
    * @see org.exoplatform.ide.vfs.server.VirtualFileSystem#updateItem(java.lang.String, java.util.List,
    *      java.lang.String)
    */
   @Path("item/{id}")
   @Override
   public Item updateItem(@PathParam("id") String id, //
                          List<Property> properties, //
                          @QueryParam("lockToken") String lockToken //
   ) throws ItemNotFoundException, LockException, PermissionDeniedException, VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemData(session, id);
         if (properties != null && properties.size() > 0)
         {
            String[] removeMixinTypes = null;
            String[] addMixinTypes = null;
            for (Property property : properties)
            {
               //TODO : need send user id to the JRebel according license agreement.
               //This is temporary solution
               if ("jrebel".equals(property.getName()) && property.getValue() != null && Boolean.parseBoolean(property.getValue().get(0)))
               {
                  LOG_JREBEL_PROP.error(ConversationState.getCurrent().getIdentity().getUserId());                  
               }
                  
               if ("vfs:mimeType".equals(property.getName()))
               {
                  List<String> value = property.getValue();
                  if (!(value == null || value.isEmpty()))
                  {
                     MediaType newMediaType = MediaType.valueOf(value.get(0));
                     MediaType currentMediaType = data.getMediaType();
                     if (!compareMediaTypeIgnoreParameters(currentMediaType, newMediaType))
                     {
                        removeMixinTypes =
                           data.getType() == ItemType.FILE ? mediaType2NodeTypeResolver.getFileMixins(currentMediaType)
                              : mediaType2NodeTypeResolver.getFolderMixins(currentMediaType);
                        addMixinTypes =
                           data.getType() == ItemType.FILE ? mediaType2NodeTypeResolver.getFileMixins(newMediaType)
                              : mediaType2NodeTypeResolver.getFolderMixins(newMediaType);
                     }
                  }
               }
            }
            data.updateProperties(properties, addMixinTypes, removeMixinTypes, lockToken);
            data = getItemData(session, id);
            MediaType mediaType = data.getMediaType();
            notifyListeners(new ChangeEvent(this, //
               data.getId(), //
               data.getPath(), //
               mediaType != null ? mediaType.toString() : null, //
               ChangeEvent.ChangeType.PROPERTIES_UPDATED)
            );
         }
         return fromItemData(data, PropertyFilter.ALL_FILTER);
      }
      finally
      {
         session.logout();
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#exportZip(java.lang.String) */
   @Path("export/{folderId}")
   @Override
   public ContentStream exportZip(@PathParam("folderId") String folderId) throws ItemNotFoundException,
      InvalidArgumentException, PermissionDeniedException, IOException, VirtualFileSystemException
   {
      Session session = session();
      try
      {
         ItemData data = getItemData(session, folderId);
         if (!(ItemType.FOLDER == data.getType() || ItemType.PROJECT == data.getType()))
         {
            throw new InvalidArgumentException("Unable export to zip. Item is not a folder or project. ");
         }
         FolderData exportFolder = (FolderData)data;
         java.io.File zipFile = java.io.File.createTempFile("export", ".zip");
         FileOutputStream out = new FileOutputStream(zipFile);
         try
         {
            ZipOutputStream zipOut = new ZipOutputStream(out);
            LinkedList<FolderData> q = new LinkedList<FolderData>();
            q.add(exportFolder);
            final String rootZipPath = exportFolder.getPath();
            byte[] b = new byte[8192];
            while (!q.isEmpty())
            {
               LazyIterator<ItemData> children = q.pop().getChildren(null);
               while (children.hasNext())
               {
                  ItemData current = children.next();
                  final String zipEntryName = current.getPath().substring(rootZipPath.length() + 1).replace('\\', '/');
                  if (current.getType() == ItemType.FILE)
                  {
                     zipOut.putNextEntry(new ZipEntry(zipEntryName));
                     InputStream in = null;
                     try
                     {
                        in = ((FileData)current).getContent();
                        int r;
                        while ((r = in.read(b)) != -1)
                        {
                           zipOut.write(b, 0, r);
                        }
                     }
                     finally
                     {
                        if (in != null)
                        {
                           in.close();
                        }
                     }
                  }
                  else if (".project".equals(current.getName()))
                  {
                     zipOut.putNextEntry(new ZipEntry(zipEntryName));
                     try
                     {
                        JsonWriter jw = new JsonWriter(zipOut);
                        JsonGenerator.createJsonArray(current.getProperties(PropertyFilter.ALL_FILTER)).writeTo(jw);
                        jw.flush();
                     }
                     catch (JsonException e)
                     {
                        throw new VirtualFileSystemRuntimeException(e.getMessage(), e);
                     }
                  }
                  else
                  {
                     zipOut.putNextEntry(new ZipEntry(zipEntryName + '/'));
                     q.add((FolderData)current);
                  }
                  zipOut.closeEntry();
               }
            }
            zipOut.close();
         }
         finally
         {
            out.close();
         }

         return new ContentStream(exportFolder.getName() + ".zip", //
            new DeleteOnCloseFileInputStream(zipFile), //
            "application/zip", //
            zipFile.length(), //
            new Date());
      }
      finally
      {
         session.logout();
      }
   }

   /** Delete java.io.File after close. */
   private static final class DeleteOnCloseFileInputStream extends FileInputStream
   {
      private final java.io.File file;
      private boolean deleted = false;

      public DeleteOnCloseFileInputStream(java.io.File file) throws FileNotFoundException
      {
         super(file);
         this.file = file;
      }

      /** @see java.io.FileInputStream#close() */
      @Override
      public void close() throws IOException
      {
         try
         {
            super.close();
         }
         finally
         {
            if (!deleted)
            {
               deleted = file.delete();
            }
         }
         //System.out.println("---> " + file.getAbsolutePath() + ", exists : " + file.exists());
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#importZip(java.lang.String, java.io.InputStream, Boolean) */
   @Path("import/{parentId}")
   @Override
   public void importZip(@PathParam("parentId") String parentId, //
                         InputStream in, //
                         @DefaultValue("false") @QueryParam("overwrite") Boolean overwrite //
   ) throws ItemNotFoundException, PermissionDeniedException, VirtualFileSystemException, IOException
   {
      Session session = null;
      ZipInputStream zip = null;
      try
      {
         session = session();
         ItemData parentData = getItemData(session, parentId);
         if (!(ItemType.FOLDER == parentData.getType() || ItemType.PROJECT == parentData.getType()))
         {
            throw new InvalidArgumentException("Unable import from zip. "
               + "Item specified as parent is not a folder or project. ");
         }

         final ZipContent zipContent = spoolZipStream(in);

         if (zipContent.isProject && ItemType.PROJECT == parentData.getType())
         {
            throw new ConstraintException("Unable import from zip. Project cannot be imported to another one. ");
         }

         zip = new ZipInputStream(zipContent.data);
         // Wrap zip stream to prevent close it. We can pass stream to other method
         // and it can read content of current ZipEntry but not able to close original
         // stream of ZIPed data.
         InputStream noCloseZip = new NotClosableInputStream(zip);
         FolderData parentFolder = (FolderData)parentData;
         ZipEntry zipEntry;
         while ((zipEntry = zip.getNextEntry()) != null)
         {
            String zipEntryName = zipEntry.getName();
            String[] segments = zipEntryName.split("/");
            FolderData current = parentFolder;
            for (int i = 0, l = segments.length - 1; i < l; i++)
            {
               ItemData child = current.getChild(segments[i]);
               if (child == null)
               {
                  child = current.createFolder(segments[i], //
                     mediaType2NodeTypeResolver.getFolderNodeType((String)null), //
                     mediaType2NodeTypeResolver.getFolderMixins((String)null), //
                     null);
               }
               current = (FolderData)child;
            }

            final String name = segments[segments.length - 1];
            if (zipEntry.isDirectory())
            {
               if (!current.hasChild(name))
               {
                  current.createFolder(name, //
                     mediaType2NodeTypeResolver.getFolderNodeType((String)null), //
                     mediaType2NodeTypeResolver.getFolderMixins((String)null), //
                     null);
               }
            }
            else if (".project".equals(name))
            {
               // If project has other media type it will be updated later.
               final MediaType mediaType = MediaType.valueOf(Project.PROJECT_MIME_TYPE);
               current.rename(null, //
                  mediaType, //
                  null, //
                  mediaType2NodeTypeResolver.getFolderMixins(mediaType), //
                  mediaType2NodeTypeResolver.getFolderMixins((String)null));
               List<Property> properties;
               try
               {
                  JsonParser jp = new JsonParser();
                  jp.parse(noCloseZip);
                  Property[] array =
                     (Property[])ObjectBuilder.createArray(PropertyImpl[].class, jp.getJsonObject());
                  properties = Arrays.asList(array);
               }
               catch (JsonException e)
               {
                  throw new VirtualFileSystemRuntimeException(e.getMessage(), e);
               }
               if (properties.size() > 0)
               {
                  current.updateProperties(properties, null, null, null);
               }
            }
            else
            {
               ItemData child = current.getChild(name);
               if (child != null && overwrite && ItemType.FILE == child.getType())
               {
                  ((FileData)child).setContent(noCloseZip, child.getMediaType(), null);
               }
               else
               {
                  final MediaType mediaType = Resolver.INSTANCE.getMediaType(name);
                  current.createFile(name, //
                     mediaType2NodeTypeResolver.getFileNodeType(mediaType), //
                     mediaType2NodeTypeResolver.getFileContentNodeType(mediaType), //
                     mediaType, //
                     mediaType2NodeTypeResolver.getFileMixins(mediaType), //
                     null, //
                     noCloseZip);
               }
            }
            zip.closeEntry();
         }
      }
      finally
      {
         if (session != null)
         {
            session.logout();
         }
         if (zip != null)
         {
            zip.close();
         }
      }
   }

   /** Wrapper for ZipInputStream that make possible read content of ZipEntry but prevent close ZipInputStream. */
   private static final class NotClosableInputStream extends FilterInputStream
   {
      public NotClosableInputStream(InputStream delegate)
      {
         super(delegate);
      }

      /** @see java.io.InputStream#close() */
      @Override
      public void close() throws IOException
      {
      }
   }

   /** Memory threshold. If zip stream over this size it spooled in file. */
   private static final int BUFFER = 102400; // 100k
   /** The threshold after that checking of ZIP ratio started. */
   private static final long ZIP_THRESHOLD = 1000000;
   /**
    * Max compression ratio. If the number of bytes uncompressed data is exceed the number
    * of bytes of compressed stream more than this ratio (and number of uncompressed data
    * is more than threshold) then VirtualFileSystemRuntimeException is thrown.
    */
   private static final int ZIP_RATIO = 100;

   /**
    * Spool content of zip in memory or in file.
    *
    * @param src
    *    source zip
    * @return spool zip
    * @throws IOException
    *    if any i/o error occur
    */
   private ZipContent spoolZipStream(InputStream src) throws IOException
   {
      java.io.File file = null;
      byte[] inMemory = null;

      int count = 0;
      ByteArrayOutputStream inMemorySpool = new ByteArrayOutputStream(BUFFER);

      int bytes;
      byte[] buff = new byte[8192];
      while (count <= BUFFER && (bytes = src.read(buff)) != -1)
      {
         inMemorySpool.write(buff, 0, bytes);
         count += bytes;
      }

      InputStream in;
      if (count > BUFFER)
      {
         file = java.io.File.createTempFile("import", ".zip");
         FileOutputStream fileSpool = new FileOutputStream(file);
         try
         {
            inMemorySpool.writeTo(fileSpool);
            while ((bytes = src.read(buff)) != -1)
            {
               fileSpool.write(buff, 0, bytes);
            }
         }
         finally
         {
            fileSpool.close();
         }
         in = new FileInputStream(file);
      }
      else
      {
         inMemory = inMemorySpool.toByteArray();
         in = new ByteArrayInputStream(inMemory);
      }

      ZipInputStream zip = null;
      try
      {
         // Counts numbers of compressed data.
         final CountingInputStream compressedCounter = new CountingInputStream(in);
         zip = new ZipInputStream(compressedCounter);
         // Counts number of uncompressed data.
         CountingInputStream uncompressedCounter = new CountingInputStream(zip)
         {
            @Override
            public int read() throws IOException
            {
               int i = super.read();
               checkCompressionRatio();
               return i;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException
            {
               int i = super.read(b, off, len);
               checkCompressionRatio();
               return i;
            }

            @Override
            public int read(byte[] b) throws IOException
            {
               int i = super.read(b);
               checkCompressionRatio();
               return i;
            }

            @Override
            public long skip(long length) throws IOException
            {
               long i = super.skip(length);
               checkCompressionRatio();
               return i;
            }

            private void checkCompressionRatio()
            {
               long uncompressedBytes = getByteCount(); // number of uncompressed bytes
               if (uncompressedBytes > ZIP_THRESHOLD)
               {
                  long compressedBytes = compressedCounter.getByteCount(); // number of compressed bytes
                  if (uncompressedBytes > (ZIP_RATIO * compressedBytes))
                  {
                     throw new VirtualFileSystemRuntimeException("Zip bomb detected. ");
                  }
               }
            }
         };

         boolean isProject = false;

         ZipEntry zipEntry;
         while ((zipEntry = zip.getNextEntry()) != null)
         {
            if (".project".equals(zipEntry.getName()))
            {
               isProject = true;
            }
            else if (!zipEntry.isDirectory())
            {
               while (uncompressedCounter.read(buff) != -1)
               {
                  // Read full data from stream to be able detect zip-bomb.
               }
            }
         }

         return new ZipContent(
            inMemory != null ? new ByteArrayInputStream(inMemory) : new DeleteOnCloseFileInputStream(file),
            isProject
         );
      }
      finally
      {
         if (zip != null)
         {
            zip.close();
         }
      }
   }

   private static final class ZipContent
   {
      final InputStream data;
      final boolean isProject;

      ZipContent(InputStream data, boolean isProject)
      {
         this.data = data;
         this.isProject = isProject;
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#downloadFile(java.lang.String) */
   @Path("downloadfile/{id}")
   @Override
   public Response downloadFile(@PathParam("id") String id) throws ItemNotFoundException, InvalidArgumentException,
      PermissionDeniedException, VirtualFileSystemException
   {
      ContentStream content = getContent(id);
      return Response //
         .ok(content.getStream(), content.getMimeType()) //
         .lastModified(content.getLastModificationDate()) //
         .header(HttpHeaders.CONTENT_LENGTH, Long.toString(content.getLength())) //
         .header("Content-Disposition", "attachment; filename=\"" + content.getFileName() + "\"") //
         .build();
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#uploadFile(java.lang.String, java.util.Iterator) */
   @Path("uploadfile/{parentId}")
   @Override
   public Response uploadFile(@PathParam("parentId") String parentId, //
                              java.util.Iterator<FileItem> formData //
   ) throws ItemNotFoundException, InvalidArgumentException, ItemAlreadyExistException, PermissionDeniedException,
      VirtualFileSystemException, IOException
   {
      Session session = session();
      try
      {
         ItemData parentData = getItemData(session, parentId);
         if (!(ItemType.FOLDER == parentData.getType() || ItemType.PROJECT == parentData.getType()))
         {
            throw new InvalidArgumentException("Unable upload file. Item specified as parent is not a folder or project. ");
         }

         FileItem contentItem = null;
         MediaType mediaType = null;
         String name = null;
         boolean overwrite = false;

         while (formData.hasNext())
         {
            FileItem item = formData.next();
            if (!item.isFormField())
            {
               if (contentItem == null)
               {
                  contentItem = item;
               }
               else
               {
                  throw new InvalidArgumentException("More then one upload file is found but only one should be. ");
               }
            }
            else if ("mimeType".equals(item.getFieldName()))
            {
               String m = item.getString().trim();
               if (m.length() > 0)
               {
                  mediaType = MediaType.valueOf(m);
               }
            }
            else if ("name".equals(item.getFieldName()))
            {
               name = item.getString().trim();
            }
            else if ("overwrite".equals(item.getFieldName()))
            {
               overwrite = Boolean.parseBoolean(item.getString().trim());
            }
         }

         if (contentItem == null)
         {
            throw new InvalidArgumentException("Cannot find file for upload. ");
         }

         if (name == null || name.isEmpty())
         {
            name = contentItem.getName();
         }
         checkName(name);

         if (mediaType == null)
         {
            String contentType = contentItem.getContentType();
            mediaType = contentType != null //
               ? MediaType.valueOf(contentType) //
               : MediaType.APPLICATION_OCTET_STREAM_TYPE;
         }

         InputStream content = contentItem.getInputStream();
         FolderData folder = (FolderData)parentData;
         if (!folder.hasChild(name))
         {
            FileData file = folder.createFile(name, //
               mediaType2NodeTypeResolver.getFileNodeType(mediaType), //
               mediaType2NodeTypeResolver.getFileContentNodeType(mediaType), //
               mediaType, //
               mediaType2NodeTypeResolver.getFileMixins(mediaType), //
               null, //
               content);
            notifyListeners(new ChangeEvent(this, //
               file.getId(), //
               file.getPath(), //
               file.getMediaType().toString(), //
               ChangeEvent.ChangeType.CREATED)
            );
         }
         else if (overwrite)
         {
            final String filePath = parentData.getPath() + "/" + name;
            ItemData file = getItemDataByPath(session, filePath);
            if (ItemType.FILE != file.getType())
            {
               throw new ItemAlreadyExistException(
                  "Unable upload file. Item with the same name exists but it is not a file. ");
            }
            ((FileData)file).setContent(content, mediaType, null);
            notifyListeners(new ChangeEvent(this, //
               file.getId(), //
               file.getPath(), //
               file.getMediaType().toString(), //
               ChangeEvent.ChangeType.CONTENT_UPDATED)
            );
         }
         else
         {
            throw new ItemAlreadyExistException("Unable upload file. File with the same name exists. ");
         }
         return Response.ok("", MediaType.TEXT_HTML).build();
      }
      catch (VirtualFileSystemException e)
      {
         sendErrorAsHTML(e);
         // never thrown
         throw e;
      }
      catch (IOException e)
      {
         sendErrorAsHTML(e);
         // never thrown
         throw e;
      }
      finally
      {
         session.logout();
      }
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#downloadZip(java.lang.String) */
   @Path("downloadzip/{folderId}")
   @Override
   public Response downloadZip(@PathParam("folderId") String folderId) throws ItemNotFoundException,
      InvalidArgumentException, PermissionDeniedException, IOException, VirtualFileSystemException
   {
      ContentStream zip = exportZip(folderId);
      return Response //
         .ok(zip.getStream(), zip.getMimeType()) //
         .lastModified(zip.getLastModificationDate()) //
         .header(HttpHeaders.CONTENT_LENGTH, Long.toString(zip.getLength())) //
         .header("Content-Disposition", "attachment; filename=\"" + zip.getFileName() + "\"") //
         .build();
   }

   /** @see org.exoplatform.ide.vfs.server.VirtualFileSystem#uploadZip(java.lang.String, java.util.Iterator) */
   @Path("uploadzip/{parentId}")
   @Override
   public Response uploadZip(@PathParam("parentId") String parentId, //
                             Iterator<FileItem> formData) throws ItemNotFoundException, InvalidArgumentException,
      PermissionDeniedException, IOException, VirtualFileSystemException
   {
      try
      {
         FileItem contentItem = null;
         boolean overwrite = false;
         while (formData.hasNext())
         {
            FileItem item = formData.next();
            if (!item.isFormField())
            {
               if (contentItem == null)
               {
                  contentItem = item;
               }
               else
               {
                  throw new InvalidArgumentException("More then one upload file is found but only one should be. ");
               }
            }
            else if ("overwrite".equals(item.getFieldName()))
            {
               overwrite = Boolean.parseBoolean(item.getString().trim());
            }
         }
         if (contentItem == null)
         {
            throw new InvalidArgumentException("Cannot find file for upload. ");
         }
         importZip(parentId, contentItem.getInputStream(), overwrite);
         return Response.ok("", MediaType.TEXT_HTML).build();
      }
      catch (VirtualFileSystemException e)
      {
         sendErrorAsHTML(e);
         // never thrown
         throw e;
      }
      catch (IOException e)
      {
         sendErrorAsHTML(e);
         // never thrown
         throw e;
      }
   }

   @Path("watch/start/{projectId}")
   @Override
   public void startWatchUpdates(@PathParam("projectId") String projectId) throws ItemNotFoundException,
      InvalidArgumentException, VirtualFileSystemException
   {
      if (listeners == null)
      {
         throw new VirtualFileSystemException("EventListenerList is not configured properly. ");
      }
      Session session = session();
      try
      {
         ItemData projectData = getItemData(session, projectId);
         if (ItemType.PROJECT != projectData.getType())
         {
            throw new InvalidArgumentException("Item is not a project. ");
         }
         if (!listeners.addEventListener(
            ProjectUpdateEventFilter.newFilter(this, (ProjectData)projectData), new ProjectUpdateListener(projectId)))
         {
            throw new InvalidArgumentException("Project '" + projectData.getName() + "' is under watching already. ");
         }
         List<Property> properties = new ArrayList<Property>(1);
         properties.add(new PropertyImpl("vfs:lastUpdateTime", "0"));
         projectData.updateProperties(properties, null, null, null);
      }
      finally
      {
         session.logout();
      }
   }

   @Path("watch/stop/{projectId}")
   @Override
   public void stopWatchUpdates(@PathParam("projectId") String projectId) throws ItemNotFoundException,
      InvalidArgumentException, VirtualFileSystemException
   {
      if (listeners != null)
      {
         Session session = session();
         try
         {
            ItemData projectData = getItemData(session, projectId);
            if (ItemType.PROJECT != projectData.getType() // Do not check listeners if not a project.
               || !listeners.removeEventListener(ProjectUpdateEventFilter.newFilter(this, (ProjectData)projectData),
               new ProjectUpdateListener(projectId)))
            {
               throw new InvalidArgumentException("'" + projectData.getName() + "' is not under watching. ");
            }
         }
         finally
         {
            session.logout();
         }
      }
   }

   /**
    * Get JCR path from Virtual File System path.
    *
    * @param userID
    *    the current user identifier
    * @param vfsPath
    *    Virtual File System path
    * @return JCR path
    */
   protected final String getJcrPath(String userID, String vfsPath)
   {
      if (rootNodePath == null || rootNodePath.isEmpty() || "/".equals(rootNodePath))
      {
         // Not need to do anything if rootNodePath is not set.
         return vfsPath;
      }
      String root = rootNodePath;
      if (root.contains("${userId}"))
      {
         root = root.replace("${userId}", userID);
      }
      if ("/".equals(vfsPath))
      {
         return root;
      }
      return (root + vfsPath);
   }

   //-----------------------------------------

   private void checkName(String name) throws InvalidArgumentException
   {
      if (name == null || name.trim().length() == 0)
      {
         throw new InvalidArgumentException("Item's name is not set. ");
      }
   }

   private Item fromItemData(ItemData data, PropertyFilter propertyFilter)
      throws PermissionDeniedException, VirtualFileSystemException
   {
      return fromItemData(data, propertyFilter, true);
   }

   private Item fromItemData(ItemData data, PropertyFilter propertyFilter, boolean addLinks)
      throws PermissionDeniedException, VirtualFileSystemException
   {
      if (data.getType() == ItemType.FILE)
      {
         FileData fileData = (FileData)data;
         return new FileImpl(fileData.getId(), fileData.getName(), fileData.getPath(), fileData.getParentId(),
            fileData.getCreationDate(), fileData.getLastModificationDate(), fileData.getVersionId(), fileData
            .getMediaType().toString(), fileData.getContentLength(), fileData.isLocked(),
            fileData.getProperties(propertyFilter), addLinks ? createFileLinks(fileData) : null);
      }

      if (data.getType() == ItemType.PROJECT)
      {
         ProjectData projectData = (ProjectData)data;
         MediaType mediaType = projectData.getMediaType();
         return new ProjectImpl(projectData.getId(), projectData.getName(), mediaType == null ? ProjectImpl.FOLDER_MIME_TYPE
            : mediaType.toString(), projectData.getPath(), projectData.getParentId(), projectData.getCreationDate(),
            projectData.getProperties(propertyFilter), addLinks ? createProjectLinks(projectData) : null,
            projectData.getProjectType());
      }

      MediaType mediaType = data.getMediaType();
      return new FolderImpl(data.getId(), data.getName(), mediaType == null ? null : mediaType.toString(), data.getPath(),
         data.getParentId(), data.getCreationDate(), data.getProperties(propertyFilter),
         addLinks ? createFolderLinks((FolderData)data) : null);
   }

   private Map<String, Link> createFileLinks(FileData file) throws VirtualFileSystemException
   {
      Map<String, Link> links = createBaseLinks(file);
      String id = file.getId();

      links.put(Link.REL_CONTENT, //
         new LinkImpl(createURI("content", id), Link.REL_CONTENT, file.getMediaType().toString()));

      links.put(Link.REL_DOWNLOAD_FILE, //
         new LinkImpl(createURI("downloadfile", id), Link.REL_DOWNLOAD_FILE, file.getMediaType().toString()));

      links.put(Link.REL_CONTENT_BY_PATH, //
         new LinkImpl(createURI("contentbypath", file.getPath().substring(1)), Link.REL_CONTENT_BY_PATH, file
            .getMediaType().toString()));

      links.put(Link.REL_VERSION_HISTORY, //
         new LinkImpl(createURI("version-history", id), Link.REL_VERSION_HISTORY, MediaType.APPLICATION_JSON));

      links.put(Link.REL_CURRENT_VERSION, //
         new LinkImpl(createURI("item", file.getLatestVersionId()), Link.REL_CURRENT_VERSION, MediaType.APPLICATION_JSON));

      if (file.isLocked())
      {
         links.put(Link.REL_UNLOCK, //
            new LinkImpl(createURI("unlock", id, "lockToken", "[lockToken]"), Link.REL_UNLOCK, null));
      }
      else
      {
         links.put(Link.REL_LOCK, //
            new LinkImpl(createURI("lock", id), Link.REL_LOCK, MediaType.APPLICATION_JSON));
      }

      return links;
   }

   private Map<String, Link> createFolderLinks(FolderData folder) throws VirtualFileSystemException
   {
      Map<String, Link> links = createBaseFolderLinks(folder);
      String id = folder.getId();

      links.put(Link.REL_CREATE_PROJECT, //
         new LinkImpl(createURI("project", id, "name", "[name]", "type", "[type]"), Link.REL_CREATE_PROJECT,
            MediaType.APPLICATION_JSON));

      return links;
   }

   private Map<String, Link> createProjectLinks(ProjectData project) throws VirtualFileSystemException
   {
      return createBaseFolderLinks(project);
   }

   private Map<String, Link> createBaseFolderLinks(FolderData folder) throws VirtualFileSystemException
   {
      Map<String, Link> links = createBaseLinks(folder);
      String id = folder.getId();

      links.put(Link.REL_CHILDREN, //
         new LinkImpl(createURI("children", id), Link.REL_CHILDREN, MediaType.APPLICATION_JSON));

//      links.put(Link.REL_TREE, //
//         new Link(createURI("tree", id), Link.REL_TREE, MediaType.APPLICATION_JSON));

      links.put(Link.REL_CREATE_FOLDER, //
         new LinkImpl(createURI("folder", id, "name", "[name]"), Link.REL_CREATE_FOLDER, MediaType.APPLICATION_JSON));

      links.put(Link.REL_CREATE_FILE, //
         new LinkImpl(createURI("file", id, "name", "[name]"), Link.REL_CREATE_FILE, MediaType.APPLICATION_JSON));

      links.put(Link.REL_UPLOAD_FILE, //
         new LinkImpl(createURI("uploadfile", id), Link.REL_UPLOAD_FILE, MediaType.TEXT_HTML));

      links.put(Link.REL_EXPORT, //
         new LinkImpl(createURI("export", id), Link.REL_EXPORT, "application/zip"));

      links.put(Link.REL_IMPORT, //
         new LinkImpl(createURI("import", id), Link.REL_IMPORT, "application/zip"));

      links.put(Link.REL_DOWNLOAD_ZIP, //
         new LinkImpl(createURI("downloadzip", id), Link.REL_DOWNLOAD_ZIP, "application/zip"));

      links.put(Link.REL_UPLOAD_ZIP, //
         new LinkImpl(createURI("uploadzip", id), Link.REL_UPLOAD_ZIP, MediaType.TEXT_HTML));

      return links;
   }

   private Map<String, Link> createBaseLinks(ItemData data) throws VirtualFileSystemException
   {
      Map<String, Link> links = new HashMap<String, Link>();
      String id = data.getId();

      links.put(Link.REL_SELF, //
         new LinkImpl(createURI("item", id), Link.REL_SELF, MediaType.APPLICATION_JSON));

      links.put(Link.REL_ACL, //
         new LinkImpl(createURI("acl", id), Link.REL_ACL, MediaType.APPLICATION_JSON));

      String parentId = data.getParentId();
      // Root folder can't be moved copied and has not parent.
      if (parentId != null)
      {
         links.put(Link.REL_DELETE, //
            new LinkImpl((data.isLocked() ? createURI("delete", id, "lockToken", "[lockToken]") : createURI("delete", id)),
               Link.REL_DELETE, null));

         links.put(Link.REL_COPY, //
            new LinkImpl(createURI("copy", id, "parentId", "[parentId]"), Link.REL_COPY, MediaType.APPLICATION_JSON));

         links.put(Link.REL_MOVE, //
            new LinkImpl((data.isLocked() ? createURI("move", id, "parentId", "[parentId]", "lockToken", "[lockToken]")
               : createURI("move", id, "parentId", "[parentId]")), Link.REL_MOVE, MediaType.APPLICATION_JSON));

         links.put(Link.REL_PARENT, //
            new LinkImpl(createURI("item", parentId), Link.REL_PARENT, MediaType.APPLICATION_JSON));

         links.put(
            Link.REL_RENAME, //
            new LinkImpl(createURI("rename", id, "newname", "[newname]", "mediaType", "[mediaType]", "lockToken",
               "[lockToken]"), Link.REL_RENAME, MediaType.APPLICATION_JSON));
      }
      return links;
   }

   private String createURI(String rel, String id, String... query)
   {
      UriBuilder uriBuilder = UriBuilder.fromUri(baseUri);
      uriBuilder.path(VirtualFileSystemFactory.class, "getFileSystem");
      uriBuilder.path(rel);
      if (id != null)
      {
         uriBuilder.path(id);
      }
      if (query != null && query.length > 0)
      {
         for (int i = 0; i < query.length; i++)
         {
            String name = query[i];
            String value = i < query.length ? query[++i] : "";
            uriBuilder.queryParam(name, value);
         }
      }

      URI uri = uriBuilder.build(vfsID);

      return uri.toString();
   }

   private ItemData getItemData(Session session, String id) throws ItemNotFoundException, PermissionDeniedException,
      VirtualFileSystemException
   {
      if (id == null)
      {
         throw new NullPointerException("Object id may not be null. ");
      }
      try
      {
         return ItemData.fromNode(((ExtendedSession)session).getNodeByIdentifier(id),
            getJcrPath(session.getUserID(), "/"));
      }
      catch (javax.jcr.ItemNotFoundException e)
      {
         throw new ItemNotFoundException("Object '" + id + "' does not exists. ");
      }
      catch (AccessDeniedException e)
      {
         throw new PermissionDeniedException("Access denied to object " + id + ". ");
      }
      catch (RepositoryException e)
      {
         throw new VirtualFileSystemException(e.getMessage(), e);
      }
   }

   private ItemData getItemDataByPath(Session session, String path) throws ItemNotFoundException,
      PermissionDeniedException, VirtualFileSystemException
   {
      if (path == null)
      {
         throw new NullPointerException("Object path may not be null.");
      }
      try
      {
         if (!path.startsWith("/"))
         {
            path = '/' + path;
         }
         String jcrPath = getJcrPath(session.getUserID(), path);
         javax.jcr.Item jcrItem = session.getItem(jcrPath);
         if (!jcrItem.isNode())
         {
            throw new ItemNotFoundException("Object " + path + " does not exists. ");
         }
         return ItemData.fromNode((Node)jcrItem, getJcrPath(session.getUserID(), "/"));
      }
      catch (PathNotFoundException e)
      {
         throw new ItemNotFoundException("Object " + path + " does not exists. ");
      }
      catch (AccessDeniedException e)
      {
         throw new PermissionDeniedException("Access denied to object " + path + ". ");
      }
      catch (RepositoryException e)
      {
         throw new VirtualFileSystemException(e.getMessage(), e);
      }
   }

   private Session session() throws VirtualFileSystemException
   {
      try
      {
         return repository.login(workspaceName);
      }
      catch (RepositoryException e)
      {
         throw new VirtualFileSystemException(e.getMessage(), e);
      }
   }

   private void notifyListeners(ChangeEvent event) throws VirtualFileSystemException
   {
      if (listeners != null)
      {
         listeners.notifyListeners(event);
      }
   }

   private boolean compareMediaTypeIgnoreParameters(MediaType a, MediaType b)
   {
      if (a == null && b == null)
      {
         return true;
      }
      return !(a == null || b == null)
         && a.getType().equalsIgnoreCase(b.getType()) && a.getSubtype().equalsIgnoreCase(b.getSubtype());
   }

   /**
    * Throws WebApplicationException that contains error message in HTML format.
    *
    * @param e
    *    exception
    */
   private void sendErrorAsHTML(Exception e)
   {
      // GWT framework (used on client side) requires result in HTML format if use HTML forms.
      if (e instanceof ItemAlreadyExistException)
      {
         throw new WebApplicationException(Response.ok(formatAsHtml(e.getMessage(), ExitCodes.ITEM_EXISTS),
            MediaType.TEXT_HTML).build());
      }
      else if (e instanceof ItemNotFoundException)
      {
         throw new WebApplicationException(Response.ok(formatAsHtml(e.getMessage(), ExitCodes.ITEM_NOT_FOUND),
            MediaType.TEXT_HTML).build());
      }
      else if (e instanceof InvalidArgumentException)
      {
         throw new WebApplicationException(Response.ok(formatAsHtml(e.getMessage(), ExitCodes.INVALID_ARGUMENT),
            MediaType.TEXT_HTML).build());
      }
      else if (e instanceof ConstraintException)
      {
         throw new WebApplicationException(Response.ok(formatAsHtml(e.getMessage(), ExitCodes.CONSTRAINT),
            MediaType.TEXT_HTML).build());
      }
      else if (e instanceof PermissionDeniedException)
      {
         throw new WebApplicationException(Response.ok(formatAsHtml(e.getMessage(), ExitCodes.NOT_PERMITTED),
            MediaType.TEXT_HTML).build());
      }
      else if (e instanceof LockException)
      {
         throw new WebApplicationException(Response.ok(formatAsHtml(e.getMessage(), ExitCodes.LOCK_CONFLICT),
            MediaType.TEXT_HTML).build());
      }
      throw new WebApplicationException(Response.ok(formatAsHtml(e.getMessage(), ExitCodes.INTERNAL_ERROR),
         MediaType.TEXT_HTML).build());
   }

   private String formatAsHtml(String message, int exitCode)
   {
      return "<pre>Code: " + exitCode + " Text: " + message + "</pre>";
   }
}
