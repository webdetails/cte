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
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cte.Constants;
import pt.webdetails.cte.api.ICteEditor;
import pt.webdetails.cte.api.ICteEnvironment;

import java.io.IOException;

public class CteEngine {

  private static CteEngine instance;
  private static Logger logger = LoggerFactory.getLogger( CteEngine.class );
  private ICteEditor cteEditor;
  private ICteEnvironment environment;

  private CteEngine() throws InitializationException {

    CoreBeanFactory factory = new CoreBeanFactory( Constants.PLUGIN_ID );

    this.environment = (ICteEnvironment) factory.getBean( ICteEnvironment.class.getSimpleName() );

    if ( environment == null ) {
      logger.error( "ICteEditor has not been set; CteEngine will not function properly" );
    }

    environment.init();

    this.cteEditor = (ICteEditor) factory.getBean( ICteEditor.class.getSimpleName() );

    if ( cteEditor == null ) {
      logger.error( "ICteEditor has not been set; CteEngine will not function properly" );
    }

    ensureBasicDir();
  }

  public static CteEngine getInstance() {

    if ( instance == null ) {
      try {
        instance = new CteEngine();
      } catch ( InitializationException e ) {
        logger.error( e.getMessage(), e );
      }
    }

    return instance;
  }

  public void ensureBasicDir(){

    IRWAccess repoAccess = getEnvironment().getPluginRepositoryWriter( null );
    IReadAccess systemAccess = getEnvironment().getPluginSystemReader( null );

    if ( !repoAccess.fileExists( Constants.PLUGIN_WELCOME_FILE ) ) {

      if ( systemAccess.fileExists( "resources/" + Constants.PLUGIN_WELCOME_FILE ) ) {

        try {
          repoAccess.saveFile( Constants.PLUGIN_WELCOME_FILE,
              systemAccess.getFileInputStream( "resources/" + Constants.PLUGIN_WELCOME_FILE ) );

        } catch ( IOException e ) {
          logger.error( e.getMessage(), e );
        }
      }
    }

    if ( !repoAccess.fileExists( Constants.PLUGIN_INVALID_PERMISSIONS_FILE ) ) {

      if ( systemAccess.fileExists( "resources/" + Constants.PLUGIN_INVALID_PERMISSIONS_FILE ) ) {

        try {
          repoAccess.saveFile( Constants.PLUGIN_INVALID_PERMISSIONS_FILE,
              systemAccess.getFileInputStream( "resources/" + Constants.PLUGIN_INVALID_PERMISSIONS_FILE ) );

        } catch ( IOException e ) {
          logger.error( e.getMessage(), e );
        }
      }
    }
  }

  public ICteEditor getCteEditor() {
    return cteEditor;
  }

  protected void setCteEditor( ICteEditor cteEditor ) {
    this.cteEditor = cteEditor;
  }

  public ICteEnvironment getEnvironment() {
    return environment;
  }

  public void setEnvironment( ICteEnvironment environment ) {
    this.environment = environment;
  }
}
