/*!
* Copyright 2002 - 2015 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/
package pt.webdetails.cte.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.bean.IBeanFactory;
import pt.webdetails.cpf.bean.AbstractBeanFactory;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cte.Constants;
import pt.webdetails.cte.CteSettings;
import pt.webdetails.cte.api.ICteEditor;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.api.ICteProviderManager;
import pt.webdetails.cte.provider.CteDefaultProviderManager;

import java.io.IOException;

public class CteEngine {

  private static CteEngine instance;
  private static Logger logger = LoggerFactory.getLogger( CteEngine.class );
  private CteSettings settings;
  private ICteEditor editor;
  private ICteProviderManager providerManager;
  private ICteEnvironment environment;

  private CteEngine() throws InitializationException {

    IBeanFactory factory = new AbstractBeanFactory(){
      @Override
      public String getSpringXMLFilename(){ return "plugin.spring.xml"; }
    };

    this.environment = (ICteEnvironment) factory.getBean( ICteEnvironment.class.getSimpleName() );

    if ( environment == null ) {
      logger.error( "ICteEnvironment has not been set; CteEngine will not function properly" );
    }

    // settings.xml
    this.settings = new CteSettings( environment.getPluginSystemWriter( null ) );

    if ( settings == null ) {
      logger.error( "CteSettings has not been set; CteEngine will not function properly" );
    }

    // chosen editor
    this.editor = (ICteEditor) factory.getBean( ICteEditor.class.getSimpleName() );

    if ( settings == null ) {
      logger.error( "ICteEditor has not been set; CteEngine will not function properly" );
    }

    // chosen provider manager
    this.providerManager = (ICteProviderManager) factory.getBean( ICteProviderManager.class.getSimpleName() );

    if ( providerManager == null ) {
      logger.error( "ProviderManager has not been set; CteEngine will not function properly" );
    }
  }

  public void init() throws InitializationException {

    getEnvironment().init();

    getSettings().init();

    getEditor().init( getEnvironment() );

    getProviderManager().init( getEnvironment(), getSettings().getBlacklistedPlugins().toArray( new String[] {} ) );
  }

  public static CteEngine getInstance() {

    if ( instance == null ) {
      try {
        instance = new CteEngine();

        instance.init(); // instance properly created, let's go ahead and initialize all of its containing beans

      } catch ( InitializationException e ) {
        logger.error( e.getMessage(), e );
      }
    }

    return instance;
  }

  public void ensureBasicDir() {

    IRWAccess repoAccess = getEnvironment().getPluginRepositoryWriter( null );
    IReadAccess systemAccess = getEnvironment().getPluginSystemReader( null );

    if ( !repoAccess.fileExists( "." /* base cte dir */ ) ) {
      repoAccess.createFolder( "." , true /* isHidden */ );
    }

    if ( !repoAccess.fileExists( Constants.PLUGIN_WELCOME_FILE ) &&
        systemAccess.fileExists( "resources/" + Constants.PLUGIN_WELCOME_FILE ) ) {

        try {
          repoAccess.saveFile( Constants.PLUGIN_WELCOME_FILE,
              systemAccess.getFileInputStream( "resources/" + Constants.PLUGIN_WELCOME_FILE ) );

        } catch ( IOException e ) {
          logger.error( e.getMessage(), e );
        }
    }

    if ( !repoAccess.fileExists( Constants.PLUGIN_INVALID_PERMISSIONS_FILE ) &&
        systemAccess.fileExists( "resources/" + Constants.PLUGIN_INVALID_PERMISSIONS_FILE )) {

        try {
          repoAccess.saveFile( Constants.PLUGIN_INVALID_PERMISSIONS_FILE,
              systemAccess.getFileInputStream( "resources/" + Constants.PLUGIN_INVALID_PERMISSIONS_FILE ) );

        } catch ( IOException e ) {
          logger.error( e.getMessage(), e );
        }
    }
  }

  public ICteProviderManager getProviderManager() {
    return providerManager;
  }

  protected void setProviderManager( CteDefaultProviderManager providerManager ) {
    this.providerManager = providerManager;
  }

  public ICteEnvironment getEnvironment() {
    return environment;
  }

  public void setEnvironment( ICteEnvironment environment ) {
    this.environment = environment;
  }

  public CteSettings getSettings() {
    return settings;
  }

  public void setSettings( CteSettings settings ) {
    this.settings = settings;
  }

  public ICteEditor getEditor() {
    return editor;
  }

  public void setEditor( ICteEditor editor ) {
    this.editor = editor;
  }
}
