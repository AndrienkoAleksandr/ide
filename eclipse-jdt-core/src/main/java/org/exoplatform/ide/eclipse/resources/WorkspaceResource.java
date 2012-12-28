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
package org.exoplatform.ide.eclipse.resources;

import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFilterMatcherDescriptor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.exoplatform.ide.vfs.server.VirtualFileSystem;
import org.exoplatform.ide.vfs.server.exceptions.ConstraintException;
import org.exoplatform.ide.vfs.server.exceptions.InvalidArgumentException;
import org.exoplatform.ide.vfs.server.exceptions.ItemAlreadyExistException;
import org.exoplatform.ide.vfs.server.exceptions.ItemNotFoundException;
import org.exoplatform.ide.vfs.server.exceptions.LockException;
import org.exoplatform.ide.vfs.server.exceptions.PermissionDeniedException;
import org.exoplatform.ide.vfs.server.exceptions.VirtualFileSystemException;
import org.exoplatform.ide.vfs.shared.Item;
import org.exoplatform.ide.vfs.shared.PropertyFilter;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:azatsarynnyy@exoplatfrom.com">Artem Zatsarynnyy</a>
 * @version $Id: WorkspaceResource.java Dec 27, 2012 12:47:21 PM azatsarynnyy $
 *
 */
public class WorkspaceResource implements IWorkspace
{
   protected final IWorkspaceRoot defaultRoot = new WorkspaceRootResource(Path.ROOT, this);

   /**
    * {@link VirtualFileSystem} instance.
    */
   private VirtualFileSystem vfs;

   public WorkspaceResource(VirtualFileSystem vfs)
   {
      this.vfs = vfs;
   }

   /**
    * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
    */
   @Override
   public Object getAdapter(Class adapter)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#addResourceChangeListener(org.eclipse.core.resources.IResourceChangeListener)
    */
   @Override
   public void addResourceChangeListener(IResourceChangeListener listener)
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#addResourceChangeListener(org.eclipse.core.resources.IResourceChangeListener, int)
    */
   @Override
   public void addResourceChangeListener(IResourceChangeListener listener, int eventMask)
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#addSaveParticipant(java.lang.String, org.eclipse.core.resources.ISaveParticipant)
    */
   @Override
   public ISavedState addSaveParticipant(String pluginId, ISaveParticipant participant) throws CoreException
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#build(int, org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public void build(int kind, IProgressMonitor monitor) throws CoreException
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#build(org.eclipse.core.resources.IBuildConfiguration[], int, boolean, org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public void build(IBuildConfiguration[] buildConfigs, int kind, boolean buildReferences, IProgressMonitor monitor)
      throws CoreException
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#checkpoint(boolean)
    */
   @Override
   public void checkpoint(boolean build)
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#computePrerequisiteOrder(org.eclipse.core.resources.IProject[])
    */
   @Override
   public IProject[][] computePrerequisiteOrder(IProject[] projects)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#computeProjectOrder(org.eclipse.core.resources.IProject[])
    */
   @Override
   public ProjectOrder computeProjectOrder(IProject[] projects)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#copy(org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IPath, boolean, org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public IStatus copy(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor)
      throws CoreException
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#copy(org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IPath, int, org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public IStatus copy(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor)
      throws CoreException
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#delete(org.eclipse.core.resources.IResource[], boolean, org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public IStatus delete(IResource[] resources, boolean force, IProgressMonitor monitor) throws CoreException
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#delete(org.eclipse.core.resources.IResource[], int, org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public IStatus delete(IResource[] resources, int updateFlags, IProgressMonitor monitor) throws CoreException
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#deleteMarkers(org.eclipse.core.resources.IMarker[])
    */
   @Override
   public void deleteMarkers(IMarker[] markers) throws CoreException
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#forgetSavedTree(java.lang.String)
    */
   @Override
   public void forgetSavedTree(String pluginId)
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#getFilterMatcherDescriptors()
    */
   @Override
   public IFilterMatcherDescriptor[] getFilterMatcherDescriptors()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#getFilterMatcherDescriptor(java.lang.String)
    */
   @Override
   public IFilterMatcherDescriptor getFilterMatcherDescriptor(String filterMatcherId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#getNatureDescriptors()
    */
   @Override
   public IProjectNatureDescriptor[] getNatureDescriptors()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#getNatureDescriptor(java.lang.String)
    */
   @Override
   public IProjectNatureDescriptor getNatureDescriptor(String natureId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#getDanglingReferences()
    */
   @Override
   public Map<IProject, IProject[]> getDanglingReferences()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#getDescription()
    */
   @Override
   public IWorkspaceDescription getDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#getRoot()
    */
   @Override
   public IWorkspaceRoot getRoot()
   {
      return defaultRoot;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#getRuleFactory()
    */
   @Override
   public IResourceRuleFactory getRuleFactory()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#getSynchronizer()
    */
   @Override
   public ISynchronizer getSynchronizer()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#isAutoBuilding()
    */
   @Override
   public boolean isAutoBuilding()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#isTreeLocked()
    */
   @Override
   public boolean isTreeLocked()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#loadProjectDescription(org.eclipse.core.runtime.IPath)
    */
   @Override
   public IProjectDescription loadProjectDescription(IPath projectDescriptionFile) throws CoreException
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#loadProjectDescription(java.io.InputStream)
    */
   @Override
   public IProjectDescription loadProjectDescription(InputStream projectDescriptionFile) throws CoreException
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#move(org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IPath, boolean, org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public IStatus move(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor)
      throws CoreException
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#move(org.eclipse.core.resources.IResource[], org.eclipse.core.runtime.IPath, int, org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public IStatus move(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor)
      throws CoreException
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#newBuildConfig(java.lang.String, java.lang.String)
    */
   @Override
   public IBuildConfiguration newBuildConfig(String projectName, String configName)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#newProjectDescription(java.lang.String)
    */
   @Override
   public IProjectDescription newProjectDescription(String projectName)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * Creates new {@link ItemResource} of the specified <code>type</code>.
    * 
    * @param path {@link IPath} of resource to create
    * @param type type of resource to create
    * @return created resource
    */
   public ItemResource newResource(IPath path, int type)
   {
      String message;
      switch (type)
      {
         case IResource.FOLDER :
            if (path.segmentCount() < ICoreConstants.MINIMUM_FOLDER_SEGMENT_LENGTH)
            {
               message = "Path must include project and resource name: " + path.toString();
               Assert.isLegal(false, message);
            }
            return new FolderResource(path.makeAbsolute(), this);
         case IResource.FILE :
            if (path.segmentCount() < ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH)
            {
               message = "Path must include project and resource name: " + path.toString();
               Assert.isLegal(false, message);
            }
            return new FileResource(path.makeAbsolute(), this);
         case IResource.PROJECT :
            //return (ItemResource)getRoot().getProject(path.lastSegment());
            return new ProjectResource(path.makeAbsolute(), this);
         case IResource.ROOT :
            return (ItemResource)getRoot();
      }
      Assert.isLegal(false);
      // will never get here because of assertion.
      return null;
   }

   /**
    * Creates provided {@link IResource} in the {@link VirtualFileSystem}.
    * 
    * @param resource {@link IResource} to create in {@link VirtualFileSystem}
    */
   public Item createResource(IResource resource) throws CoreException
   {
      IContainer parent = resource.getParent();
      if (!parent.exists())
         createResource(parent);

      try
      {
         String parentId = getVfsIdByFullPath(resource.getParent().getFullPath());
         switch (resource.getType())
         {
            case IResource.FILE :
               return vfs.createFile(parentId, resource.getName(), /* TODO use special resolver*/
                  MediaType.TEXT_PLAIN_TYPE, ((IFile)resource).getContents());
            case IResource.FOLDER :
               return vfs.createFolder(parentId, resource.getName());
            case IResource.PROJECT :
               return vfs.createProject(parentId, resource.getName(), null, null);
         }
      }
      catch (ItemNotFoundException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
      catch (InvalidArgumentException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
      catch (ItemAlreadyExistException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1,
            "Resource already exists in the workspace.", e));
      }
      catch (PermissionDeniedException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
      catch (VirtualFileSystemException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
      return null;
   }

   /**
    * Returns VFS {@link Item} identifier by provided {@link IPath}.
    * 
    * @param path {@link IPath}
    * @return {@link Item} identifier
    * @throws CoreException
    */
   String getVfsIdByFullPath(IPath path) throws CoreException
   {
      try
      {
         return vfs.getItemByPath(path.toString(), null, PropertyFilter.NONE_FILTER).getId();
      }
      catch (ItemNotFoundException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, "", e));
      }
      catch (PermissionDeniedException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, "", e));
      }
      catch (VirtualFileSystemException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, "", e));
      }
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#removeResourceChangeListener(org.eclipse.core.resources.IResourceChangeListener)
    */
   @Override
   public void removeResourceChangeListener(IResourceChangeListener listener)
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#removeSaveParticipant(java.lang.String)
    */
   @Override
   public void removeSaveParticipant(String pluginId)
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#run(org.eclipse.core.resources.IWorkspaceRunnable, org.eclipse.core.runtime.jobs.ISchedulingRule, int, org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public void run(IWorkspaceRunnable action, ISchedulingRule rule, int flags, IProgressMonitor monitor)
      throws CoreException
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#run(org.eclipse.core.resources.IWorkspaceRunnable, org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public void run(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#save(boolean, org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public IStatus save(boolean full, IProgressMonitor monitor) throws CoreException
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#setDescription(org.eclipse.core.resources.IWorkspaceDescription)
    */
   @Override
   public void setDescription(IWorkspaceDescription description) throws CoreException
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#sortNatureSet(java.lang.String[])
    */
   @Override
   public String[] sortNatureSet(String[] natureIds)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)
    */
   @Override
   public IStatus validateEdit(IFile[] files, Object context)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#validateFiltered(org.eclipse.core.resources.IResource)
    */
   @Override
   public IStatus validateFiltered(IResource resource)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#validateLinkLocation(org.eclipse.core.resources.IResource, org.eclipse.core.runtime.IPath)
    */
   @Override
   public IStatus validateLinkLocation(IResource resource, IPath location)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#validateLinkLocationURI(org.eclipse.core.resources.IResource, java.net.URI)
    */
   @Override
   public IStatus validateLinkLocationURI(IResource resource, URI location)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#validateName(java.lang.String, int)
    */
   @Override
   public IStatus validateName(String segment, int typeMask)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#validateNatureSet(java.lang.String[])
    */
   @Override
   public IStatus validateNatureSet(String[] natureIds)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#validatePath(java.lang.String, int)
    */
   @Override
   public IStatus validatePath(String path, int typeMask)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#validateProjectLocation(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IPath)
    */
   @Override
   public IStatus validateProjectLocation(IProject project, IPath location)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#validateProjectLocationURI(org.eclipse.core.resources.IProject, java.net.URI)
    */
   @Override
   public IStatus validateProjectLocationURI(IProject project, URI location)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @see org.eclipse.core.resources.IWorkspace#getPathVariableManager()
    */
   @Override
   public IPathVariableManager getPathVariableManager()
   {
      // TODO Auto-generated method stub
      return null;
   }

   protected VirtualFileSystem getVFS()
   {
      return vfs;
   }

   /**
    * Returns an open input stream on the contents of provided {@link IFile}.
    * The client is responsible for closing the stream when finished.
    * 
    * @param file {@link IFile} to get contents
    * @return an input stream containing the contents of the file
    * @throws CoreException
    * 
    * @see org.eclipse.core.resources.IFile#getContents(boolean)
    */
   InputStream getFileContents(IFile file) throws CoreException
   {
      try
      {
         String id = getVfsIdByFullPath(file.getFullPath());
         return vfs.getContent(id).getStream();
      }
      catch (ItemNotFoundException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, "", e));
      }
      catch (InvalidArgumentException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, "", e));
      }
      catch (PermissionDeniedException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, "", e));
      }
      catch (VirtualFileSystemException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, "", e));
      }
   }

   /**
    * Update binary contents of specified {@link IFile}.
    * 
    * @param file {@link IFile} to update contents
    * @param newContent new content of {@link IFile}
    * @throws CoreException
    * 
    * @see org.eclipse.core.resources.IFile#setContents(java.io.InputStream, int, org.eclipse.core.runtime.IProgressMonitor)
    */
   void setFileContents(IFile file, InputStream newContent) throws CoreException
   {
      try
      {
         vfs.updateContent(getVfsIdByFullPath(file.getFullPath()), /* TODO use special resolver*/
            MediaType.TEXT_PLAIN_TYPE, newContent, null);
      }
      catch (ItemNotFoundException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, "", e));
      }
      catch (InvalidArgumentException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, "", e));
      }
      catch (LockException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, "", e));
      }
      catch (PermissionDeniedException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, "", e));
      }
      catch (VirtualFileSystemException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, "", e));
      }
   }

   /**
    * Move provided {@link IResource} by <code>destination</code> path.
    * 
    * @param resource {@link IResource} to move
    * @param destination the destination path
    * @throws CoreException
    * 
    * @see org.eclipse.core.resources.IResource#move(org.eclipse.core.runtime.IPath, int, org.eclipse.core.runtime.IProgressMonitor)
    */
   void moveResource(IResource resource, IPath destination) throws CoreException
   {
      try
      {
         // TODO create destination if it not exist

         String id = getVfsIdByFullPath(resource.getFullPath());
         String destinationId = getVfsIdByFullPath(destination);
         vfs.move(id, destinationId, null);
      }
      catch (ItemNotFoundException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
      catch (ConstraintException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
      catch (ItemAlreadyExistException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
      catch (LockException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
      catch (PermissionDeniedException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
      catch (VirtualFileSystemException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
   }

   /**
    * Deletes specified {@link IResource} from the workspace.
    * Deletion applies recursively to all members of specified {@link IResource}.
    * 
    * @param resource {@link IResource} to delete
    * @throws CoreException
    * 
    * @see org.eclipse.core.resources.IResource#delete(int, org.eclipse.core.runtime.IProgressMonitor)
    */
   void deleteResource(IResource resource) throws CoreException
   {
      try
      {
         vfs.delete(getVfsIdByFullPath(resource.getFullPath()), null);
      }
      catch (ItemNotFoundException e)
      {
         return;
      }
      catch (ConstraintException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
      catch (LockException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
      catch (PermissionDeniedException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
      catch (VirtualFileSystemException e)
      {
         throw new CoreException(new Status(IStatus.ERROR, Status.CANCEL_STATUS.getPlugin(), 1, null, e));
      }
   }

}
