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
package pt.webdetails.cte;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPlatformReadyListener;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.engine.security.SecurityHelper;
import pt.webdetails.cte.engine.CteEngine;

import java.util.concurrent.Callable;

public class CteLifecycleListener implements IPluginLifecycleListener, IPlatformReadyListener {

  static Log logger = LogFactory.getLog( CteLifecycleListener.class );

  @Override
  public void init() throws PluginLifecycleException {
    logger.debug( "CteLifecycleListener.init()" );
  }

  @Override
  public void loaded() throws PluginLifecycleException {
    logger.debug( "CteLifecycleListener.loaded()" );
  }

  @Override
  public void unLoaded() throws PluginLifecycleException {
    logger.debug( "CteLifecycleListener.unLoaded()" );
  }

  @Override
  public void ready() throws PluginLifecycleException {
    logger.debug( "CteLifecycleListener.ready()" );

    try {
      SecurityHelper.getInstance().runAsSystem( new Callable() {

        @Override
        public Object call() throws Exception {
          CteEngine.getInstance().ensureBasicDir();
          return null;
        }
      });
    } catch ( Exception e ) {
      logger.error( e );
    }
  }
}
