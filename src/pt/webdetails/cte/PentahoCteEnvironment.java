/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
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
import pt.webdetails.cpf.plugincall.api.IPluginCall;
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

  @Override
  public Locale getLocale() {
    return LocaleHelper.getLocale();
  }

  @Override
  public String getSystemEncoding() {
    return LocaleHelper.getSystemEncoding();
  }

  @Override
  public String getPluginId() {
    return PLUGIN_ID;
  }

  @Override
  public IPluginCall getPluginCall( String s, String s1, String s2 ) {
    return null;
  }

  @Override
  public IUrlProvider getUrlProvider() {
    return super.getUrlProvider();
  }

  @Override
  public String getApplicationBaseUrl() {
    return PentahoRequestContextHolder.getRequestContext().getContextPath();
  }

  @Override
  public String getPluginBaseUrl() {
    return Util.joinPath( getApplicationBaseUrl(), PLUGIN, getPluginId() ) + Util.SEPARATOR;
  }

  @Override
  public String getPluginRepositoryDir() {
    return Util.joinPath( Util.SEPARATOR, PUBLIC, Util.SEPARATOR, PLUGIN_ID );
  }

  @Override
  public String getPluginSystemDir() {
    return Util.joinPath( Util.SEPARATOR, SYSTEM, Util.SEPARATOR, PLUGIN_ID );
  }

  @Override
  public IUserContentAccessExtended getUserContentAccess( String path ) {
    return super.getUserContentAccess( path );
  }

  @Override
  public IReadAccess getPluginRepositoryReader( String path ) {
    return super.getPluginRepositoryReader( path );
  }

  @Override
  public IRWAccess getPluginRepositoryWriter( String path ) {
    return super.getPluginRepositoryWriter( path );
  }

  @Override
  public IReadAccess getPluginSystemReader( String path ) {
    return super.getPluginSystemReader( path );
  }

  @Override
  public IRWAccess getPluginSystemWriter( String path ) {
    return super.getPluginSystemWriter( path );
  }
}
