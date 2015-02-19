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
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.util.messages.LocaleHelper;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.api.IUserContentAccessExtended;
import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cte.api.ICteEnvironment;

import java.util.Locale;

public class PentahoCteEnvironment extends PentahoPluginEnvironment implements ICteEnvironment {

  protected static Log logger = LogFactory.getLog( PentahoCteEnvironment.class );

  private static final String PLUGIN_ID = Constants.PLUGIN_ID;
  private static final String PUBLIC = "public";
  private static final String SYSTEM = "system";
  private static final String PLUGIN = "plugin";

  @Override public void init() throws InitializationException {
    super.init( this );
  }

  @Override public void refresh() {
    try {
      init();
    } catch ( InitializationException e ) {
      logger.error( e.getMessage(), e );
    }
  }

  @Override public Locale getLocale() {
    return LocaleHelper.getLocale();
  }

  @Override public String getSystemEncoding() {
    return LocaleHelper.getSystemEncoding();
  }

  @Override public String getPluginId() {
    return PLUGIN_ID;
  }

  @Override public IUrlProvider getUrlProvider() {
    return super.getUrlProvider();
  }

  @Override public String getApplicationBaseUrl() {
    return PentahoRequestContextHolder.getRequestContext().getContextPath();
  }

  @Override public String getPluginBaseUrl() {
    return Util.joinPath( getApplicationBaseUrl(), PLUGIN, getPluginId() ) + Util.SEPARATOR;
  }

  @Override public String getPluginRepositoryDir() {
    return Util.joinPath( Util.SEPARATOR, PUBLIC, Util.SEPARATOR, PLUGIN_ID );
  }

  @Override public String getPluginSystemDir() {
    return Util.joinPath( Util.SEPARATOR, SYSTEM, Util.SEPARATOR, PLUGIN_ID );
  }

  @Override public IUserContentAccessExtended getUserContentAccess( String path ) {
    return super.getUserContentAccess( path );
  }

  @Override public IReadAccess getPluginRepositoryReader( String path ) {
    return super.getPluginRepositoryReader( path );
  }

  @Override public IRWAccess getPluginRepositoryWriter( String path ) {
    return super.getPluginRepositoryWriter( path );
  }

  @Override public IReadAccess getPluginSystemReader( String path ) {
    return super.getPluginSystemReader( path );
  }

  @Override public IReadAccess getOtherPluginSystemReader( String pluginId, String path ) {
    return super.getOtherPluginSystemReader( pluginId, path );
  }

  @Override public IRWAccess getPluginSystemWriter( String path ) {
    return super.getPluginSystemWriter( path );
  }

  @Override public IRWAccess getOtherPluginSystemWriter( String pluginId, String path ) {
    return super.getOtherPluginSystemWriter( pluginId, path );
  }
}
