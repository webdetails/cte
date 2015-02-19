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
package pt.webdetails.cte.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.api.ICteProvider;
import pt.webdetails.cte.provider.fs.GenericPluginFileSystemProvider;

import java.util.List;

public class CteProviderManagerExtended extends CteDefaultProviderManager {

  private Logger logger = LoggerFactory.getLogger( CteProviderManagerExtended.class );

  private boolean autoDiscoverSparklApps;

  public CteProviderManagerExtended( List<ICteProvider> providers ) throws InitializationException {
    super( providers );
  }

  @Override public void init( ICteEnvironment environment ) throws InitializationException {

    super.init( environment );

    if ( isAutoDiscoverSparklApps() ) {

      for ( String pluginId : environment.getRegisteredPluginIds() ) {

        IReadAccess readAccess = environment.getOtherPluginSystemReader( pluginId, null );

        // unequivocal proof that this plugin *is* sparkl app: contains cpk.spring.xml in base path
        if ( readAccess != null && readAccess.fileExists( "cpk.spring.xml" ) ) {

          logger.info( "Found a sparkl app :" + pluginId );

          if ( !providerExists( pluginId ) ) {

            GenericPluginFileSystemProvider sparklAppProvider = new GenericPluginFileSystemProvider( pluginId );
            sparklAppProvider.init( environment );

            logger.info( "Adding a GenericPluginFileSystemProvider for sparkl app " + pluginId );
            addProvider( sparklAppProvider, false );

          }
        }
      }
    }
  }

  public boolean isAutoDiscoverSparklApps() {
    return autoDiscoverSparklApps;
  }

  public void setAutoDiscoverSparklApps( boolean autoDiscoverSparklApps ) {
    this.autoDiscoverSparklApps = autoDiscoverSparklApps;
  }
}
