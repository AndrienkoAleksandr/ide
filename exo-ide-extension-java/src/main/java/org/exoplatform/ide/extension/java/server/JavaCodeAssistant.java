/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.ide.extension.java.server;

import com.thoughtworks.qdox.model.AbstractJavaEntity;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;

import org.exoplatform.ide.codeassistant.jvm.CodeAssistantException;
import org.exoplatform.ide.codeassistant.jvm.CodeAssistantStorage;
import org.exoplatform.ide.codeassistant.jvm.bean.ShortTypeInfoBean;
import org.exoplatform.ide.codeassistant.jvm.shared.JavaType;
import org.exoplatform.ide.codeassistant.jvm.shared.ShortTypeInfo;
import org.exoplatform.ide.codeassistant.jvm.shared.TypeInfo;
import org.exoplatform.ide.extension.java.server.parser.JavaDocBuilderErrorHandler;
import org.exoplatform.ide.extension.java.server.parser.JavaDocBuilderVfs;
import org.exoplatform.ide.extension.java.server.parser.Util;
import org.exoplatform.ide.extension.java.server.parser.VfsClassLibrary;
import org.exoplatform.ide.extension.java.server.parser.scanner.FileSuffixFilter;
import org.exoplatform.ide.extension.java.server.parser.scanner.FolderFilter;
import org.exoplatform.ide.extension.java.server.parser.scanner.FolderScanner;
import org.exoplatform.ide.vfs.server.PropertyFilter;
import org.exoplatform.ide.vfs.server.VirtualFileSystem;
import org.exoplatform.ide.vfs.server.VirtualFileSystemRegistry;
import org.exoplatform.ide.vfs.server.exceptions.InvalidArgumentException;
import org.exoplatform.ide.vfs.server.exceptions.ItemNotFoundException;
import org.exoplatform.ide.vfs.server.exceptions.PermissionDeniedException;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.ide.vfs.shared.File;
import org.exoplatform.ide.vfs.shared.Folder;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.ItemList;
import org.exoplatform.ide.vfs.shared.ItemType;
import org.exoplatform.ide.vfs.shared.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version ${Id}: Nov 22, 2011 4:53:15 PM evgen $
 * 
 */
public class JavaCodeAssistant extends org.exoplatform.ide.codeassistant.jvm.CodeAssistant
{

   /**
    * Default Maven 'sourceDirectory' value
    */
   private static final String DEFAULT_SOURCE_FOLDER = "src/main/java";

   private VirtualFileSystemRegistry vfsRegistry;

   public JavaCodeAssistant(CodeAssistantStorage storage, VirtualFileSystemRegistry vfsRegistry)
   {
      super(storage);
      this.vfsRegistry = vfsRegistry;
   }

   private JavaDocBuilderVfs parseProject(String projectId, String vfsId) throws VirtualFileSystemException,
      ItemNotFoundException, PermissionDeniedException, CodeAssistantException
   {
      VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null);

      Project project = getProject(projectId, vfs);

      Folder sourceFolder = getSourceFolder(vfs, project);

      JavaDocBuilderVfs builder = new JavaDocBuilderVfs(vfs, new VfsClassLibrary(vfs));
      builder.getClassLibrary().addClassLoader(ClassLoader.getSystemClassLoader());
      builder.setErrorHandler(new JavaDocBuilderErrorHandler());
      builder.addSourceTree(sourceFolder);
      return builder;
   }

   /**
    * @param vfs
    * @param project
    * @return
    * @throws ItemNotFoundException
    * @throws PermissionDeniedException
    * @throws VirtualFileSystemException
    * @throws CodeAssistantException
    */
   private Folder getSourceFolder(VirtualFileSystem vfs, Project project) throws ItemNotFoundException,
      PermissionDeniedException, VirtualFileSystemException, CodeAssistantException
   {
      String sourcePath = null;
      if (project.hasProperty("sourceFolder"))
         sourcePath = (String)project.getPropertyValue("sourceFolder");
      else
         sourcePath = DEFAULT_SOURCE_FOLDER;

      Item sourceFolder = vfs.getItemByPath(project.getPath() + "/" + sourcePath, null, PropertyFilter.NONE_FILTER);

      if (sourceFolder.getItemType() != ItemType.FOLDER)
         throw new CodeAssistantException(500, "Can't find project source, in " + sourcePath);
      return (Folder)sourceFolder;
   }

   /**
    * @param projectId
    * @param vfs
    * @return
    * @throws ItemNotFoundException
    * @throws PermissionDeniedException
    * @throws VirtualFileSystemException
    * @throws CodeAssistantException
    */
   private Project getProject(String projectId, VirtualFileSystem vfs) throws ItemNotFoundException,
      PermissionDeniedException, VirtualFileSystemException, CodeAssistantException
   {
      Item item = vfs.getItem(projectId, PropertyFilter.ALL_FILTER);
      Project project = null;
      if (item instanceof Project)
         project = (Project)item;
      else
         throw new CodeAssistantException(400, "'projectId' is not project Id");
      return project;
   }

   /**
    * @see org.exoplatform.ide.codeassistant.jvm.CodeAssistant#getClassJavaDocFromProject(java.lang.String, java.lang.String,
    *      java.lang.String)
    */
   @Override
   protected String getClassJavaDocFromProject(String fqn, String projectId, String vfsId)
      throws CodeAssistantException, VirtualFileSystemException
   {

      JavaDocBuilderVfs project = parseProject(projectId, vfsId);
      JavaClass clazz = project.getClassByName(fqn);
      if (clazz == null)
         throw new CodeAssistantException(404, "Not found");

      return getJavaDoc(clazz);
   }

   /**
    * Return word until first point like "ClassName" on file name "ClassName.java"
    */
   private String getClassNameOnFileName(String fileName)
   {
      if (fileName != null)
         return fileName.substring(0, fileName.indexOf("."));

      return null;
   }

   /**
    * Return possible FQN like "org.exoplatform.example.ClassName" on file path "/org/exoplatform/example/ClassName.java"
    */
   private String getFQNByFilePath(File file, Project project)
   {
      String sourceFolderPath = (String)project.getPropertyValue("sourceFolder");
      if (sourceFolderPath == null)
      {
         sourceFolderPath = DEFAULT_SOURCE_FOLDER;
      }
      String fqn = file.getPath().substring((project.getPath() + "/" + sourceFolderPath).length() + 1);
      // remove file extension from path like ".java" from path "org/exoplatform/example/ClassName.java"
      if (fqn.matches(".*[.][^/]*$"))
         fqn = fqn.substring(0, fqn.lastIndexOf("."));
      // replace "/" on "."
      fqn = fqn.replaceAll("/", ".");

      return fqn;
   }

   /**
    * Find classes in package
    */
   private List<ShortTypeInfo> findClassesInPackage(File file, Project project, VirtualFileSystem vfs)
      throws CodeAssistantException, VirtualFileSystemException
   {
      List<ShortTypeInfo> classes = new ArrayList<ShortTypeInfo>();
      ItemList<Item> children = vfs.getChildren(file.getParentId(), -1, 0, "file", PropertyFilter.ALL_FILTER);
      for (Item i : children.getItems())
      {
         if (i.getName().endsWith(".java"))
         {
            if (!file.getId().equals(i.getId()))
            {
               classes.add(new ShortTypeInfoBean(getClassNameOnFileName(i.getName()), 0, "CLASS", null));
            }
         }
      }
      return classes;
   }

   /**
    * @see org.exoplatform.ide.codeassistant.jvm.CodeAssistant#getClassByFqnFromProject(java.lang.String, java.lang.String,
    *      java.lang.String)
    */
   @Override
   protected TypeInfo getClassByFqnFromProject(String fqn, String projectId, String vfsId)
      throws VirtualFileSystemException, CodeAssistantException
   {
      JavaDocBuilderVfs builder = parseProject(projectId, vfsId);
      JavaClass clazz = builder.getClassByName(fqn);

      if (clazz == null)
         return null;

      return Util.convert(clazz);
   }

   /**
    * @see org.exoplatform.ide.codeassistant.jvm.CodeAssistant#getTypesByNamePrefixFromProject(java.lang.String, java.lang.String,
    *      java.lang.String)
    */
   @Override
   protected List<ShortTypeInfo> getTypesByNamePrefixFromProject(String className, String projectId, String vfsId)
      throws CodeAssistantException, VirtualFileSystemException
   {
      JavaDocBuilderVfs builder = parseProject(projectId, vfsId);
      List<ShortTypeInfo> types = new ArrayList<ShortTypeInfo>();
      for (JavaClass clazz : builder.getClasses())
      {
         if (clazz.getName().startsWith(className))
            types.add(Util.toShortTypeInfo(clazz));
      }
      return types;
   }

   /**
    * @see org.exoplatform.ide.codeassistant.jvm.CodeAssistant#getTypesByFqnPrefixInProject(java.lang.String, java.lang.String,
    *      java.lang.String)
    */
   @Override
   protected List<ShortTypeInfo> getTypesByFqnPrefixInProject(String prefix, String projectId, String vfsId)
      throws CodeAssistantException, VirtualFileSystemException
   {
      JavaDocBuilderVfs builder = parseProject(projectId, vfsId);
      List<ShortTypeInfo> types = new ArrayList<ShortTypeInfo>();
      for (JavaClass clazz : builder.getClasses())
      {
         if (clazz.getFullyQualifiedName().startsWith(prefix))
            types.add(Util.toShortTypeInfo(clazz));
      }
      return types;
   }

   /**
    * @see org.exoplatform.ide.codeassistant.jvm.CodeAssistant#getByTypeFromProject(org.exoplatform.ide.codeassistant.jvm.shared.JavaType,
    *      java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   protected List<ShortTypeInfo> getByTypeFromProject(JavaType type, String prefix, String projectId, String vfsId)
      throws CodeAssistantException, VirtualFileSystemException
   {
      JavaDocBuilderVfs builder = parseProject(projectId, vfsId);
      List<ShortTypeInfo> types = new ArrayList<ShortTypeInfo>();
      if (prefix == null || prefix.isEmpty())
      {
         for (JavaClass clazz : builder.getClasses())
         {
            if (type == Util.getType(clazz))
            {
               types.add(Util.toShortTypeInfo(clazz));
            }
         }
      }
      else
      {
         for (JavaClass clazz : builder.getClasses())
         {
            if (type == Util.getType(clazz) && clazz.getName().startsWith(prefix))
            {
               types.add(Util.toShortTypeInfo(clazz));
            }
         }
      }
      return types;
   }

   /**
    * @see org.exoplatform.ide.codeassistant.jvm.CodeAssistant#getClassesFromProject(java.lang.String, java.lang.String,
    *      java.lang.String)
    */
   @Override
   public List<ShortTypeInfo> getClassesFromProject(String fileId, String projectId, String vfsId)
      throws VirtualFileSystemException, CodeAssistantException
   {
      List<ShortTypeInfo> classNames = null;
      VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null);
      Item item = vfs.getItem(fileId, PropertyFilter.ALL_FILTER);
      if (item.getItemType() != ItemType.FILE)
         throw new InvalidArgumentException("Unable find Classes. Item " + item.getName() + " is not a file. ");

      Item p = vfs.getItem(projectId, PropertyFilter.ALL_FILTER);

      Project project = null;
      if (p instanceof Project)
         project = (Project)p;
      else
         throw new InvalidArgumentException("Unable find Classes. Item " + p.getName() + " is not a project. ");

      classNames = findClassesInPackage((File)item, project, vfs);

      return classNames;
   }

   /**
    * @see org.exoplatform.ide.codeassistant.jvm.CodeAssistant#getMemberJavaDocFromProject(java.lang.String, java.lang.String,
    *      java.lang.String)
    */
   @Override
   protected String getMemberJavaDocFromProject(String fqn, String projectId, String vfsId)
      throws CodeAssistantException, VirtualFileSystemException
   {
      JavaDocBuilderVfs project = parseProject(projectId, vfsId);
      String classFqn = fqn.substring(0, fqn.lastIndexOf('.'));
      String memberFqn = fqn.substring(fqn.lastIndexOf('.') + 1);
      JavaClass clazz = project.getClassByName(classFqn);
      if (clazz == null)
         throw new CodeAssistantException(404, "Not found");

      // member is method
      if (memberFqn.contains("("))
      {
         for (JavaMethod method : clazz.getMethods())
         {
            if ((method.getName() + Util.toParameters(method.getParameterTypes(true))).equals(memberFqn))
            {
               return getJavaDoc(method);
            }
         }
      }
      // member is field
      else
      {
         for (JavaField field : clazz.getFields())
         {
            if (field.getName().equals(memberFqn))
            {
               return getJavaDoc(field);
            }
         }
      }

      throw new CodeAssistantException(404, "Not found");
   }

   private String getJavaDoc(AbstractJavaEntity entity) throws CodeAssistantException
   {
      if (entity.getComment() == null && entity.getTags().length == 0)
         throw new CodeAssistantException(404, "Not found");

      return (entity.getComment() == null ? "" : entity.getComment()) + Util.tagsToString(entity.getTags());
   }

   /**
    * @see org.exoplatform.ide.codeassistant.jvm.CodeAssistant#getTypeInfoByNamePrefixFromProject(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   protected List<TypeInfo> getTypeInfoByNamePrefixFromProject(String namePrefix, String projectId, String vfsId)
      throws VirtualFileSystemException, CodeAssistantException
   {
      JavaDocBuilderVfs builder = parseProject(projectId, vfsId);
      List<TypeInfo> typeInfos = new ArrayList<TypeInfo>();
      for (JavaClass clazz : builder.getClasses())
      {
         if (clazz.getName().startsWith(namePrefix))
            typeInfos.add(Util.convert(clazz));
      }
      return typeInfos;
   }

   /**
    * @throws VirtualFileSystemException
    * @throws CodeAssistantException
    * @see org.exoplatform.ide.codeassistant.jvm.CodeAssistant#getPackagesByPrefixFromProject(java.lang.String, java.lang.String,
    *      java.lang.String)
    */
   @Override
   protected List<String> getPackagesByPrefixFromProject(String prefix, String projectId, String vfsId)
      throws VirtualFileSystemException, CodeAssistantException
   {
      VirtualFileSystem vfs = vfsRegistry.getProvider(vfsId).newInstance(null);

      Project project = getProject(projectId, vfs);
      Folder sourceFolder = getSourceFolder(vfs, project);

      FolderScanner scanner = new FolderScanner(sourceFolder, vfs);
      scanner.addFilter(new FolderFilter());
      List<Item> list = scanner.scan();
      List<String> pakages = new ArrayList<String>();
      String sourcePath = sourceFolder.getPath();
      for (Item i : list)
      {
         String substring = i.getPath().substring(sourcePath.length() + 1);
         substring = substring.replaceAll("/", ".");
         if (substring.startsWith(prefix))
            pakages.add(substring);
      }
      return pakages;
   }
}
