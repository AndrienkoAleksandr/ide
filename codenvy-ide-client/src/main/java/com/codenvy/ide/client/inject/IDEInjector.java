package com.codenvy.ide.client.inject;

import com.codenvy.ide.client.BootstrapController;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

/**
 * THIS CLASS WILL BE OVERRIDEN BY MAVEN BUILD. DON'T EDIT CLASS, IT WILL HAVE NO EFFECT.
 * 
 * Interface for GIN Injector, that provides access to the top level
 * application components. Implementation of Injector is generated
 * on compile time.
 */
@GinModules({
   com.codenvy.ide.java.client.inject.JavaGinModule.class ,
   com.codenvy.ide.extension.cloudfoundry.client.inject.CloudFoundryGinModule.class ,
   com.codenvy.ide.extension.maven.client.inject.MavenGinModule.class ,
   com.codenvy.ide.core.inject.CoreGinModule.class ,
   com.codenvy.ide.client.inject.IDEClientModule.class 
})
public interface IDEInjector extends Ginjector
{

   /**
    * @return the instance of BootstrapController
    */
   BootstrapController getBootstrapController();

}