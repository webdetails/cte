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
package pt.webdetails.cte.provider.repo;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.api.ICteProvider;
import pt.webdetails.cte.engine.CteEngine;
import pt.webdetails.cte.provider.GenericBasicFileFilter;

import java.io.InputStream;
import java.util.List;

public class MondrianSchemaProvider implements ICteProvider {

  private static final String MONDRIAN_SCHEMAS_BASE_ROOT = "/etc/mondrian";
  private static final String[] MONDRIAN_SCHEMAS_EXT = new String[] { "xml" };

  private Logger logger = LoggerFactory.getLogger( MondrianSchemaProvider.class );

  private String id;
  private String name;

  @Override public void init( ICteEnvironment environment ) throws InitializationException {

  }

  @Override public boolean isAccessible( IUserSession user ) {
    // mondrian schema file editing is an admin-only feature
    return user != null && user.isAdministrator();
  }

  @Override public String getId() {

    if ( StringUtils.isEmpty( id ) ) {
      id = "mondrian"; // default
    }

    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  @Override public String getName() {

    if ( StringUtils.isEmpty( name ) ) {
      name = "Mondrian Schemas"; // default
    }

    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  @Override public String[] getBlacklistedFolders() {
    return new String[0];
  }

  @Override public String[] getBlacklistedFileExtensions() {
    return new String[0];
  }

  @Override public boolean canEdit( String path ) {
    return canRead( path );
  }

  @Override public boolean canRead( String path ) {

    String sp = sanitize( path );

    return !StringUtils.isEmpty( sp ) && ( sp.toLowerCase().startsWith( MONDRIAN_SCHEMAS_BASE_ROOT ) ) && isAccessible(
        getEnvironment().getUserSession() ) && getEnvironment().getUserContentAccess( null ).fileExists( sp );
  }

  @Override public InputStream getFile( String path ) throws Exception {

    if ( canRead( path ) ) {
      return getEnvironment().getUserContentAccess( null ).getFileInputStream( path );
    }
    return null;
  }

  @Override public boolean saveFile( String path, InputStream content ) throws Exception {

    boolean success = false;

    if ( canEdit( path ) ) {

      success = getEnvironment().getUserContentAccess( null ).saveFile( path, content );

      if ( success ) {

        // to be called after a successful file change
        // ( otherwise the user would not see its changes reflected right away )
        clearMondrianCache();
      }
    }

    return success;
  }

  @Override public IBasicFile[] getTree( String dir, String[] allowedExtensions, boolean showHiddenFiles )
      throws Exception {

    String sanitizedDir = sanitize( dir );

    if ( !canRead( sanitizedDir ) ) {
      return new IBasicFile[] { };
    }

    GenericBasicFileFilter
        filter =
        new GenericBasicFileFilter( MONDRIAN_SCHEMAS_EXT, GenericBasicFileFilter.FilterType.FILTER_IN );

    IUserContentAccess access = getEnvironment().getUserContentAccess( null );
    List<IBasicFile> files = access.listFiles( sanitizedDir, filter, 1, true, true );

    return files != null ? files.toArray( new IBasicFile[] { } ) : new IBasicFile[] { };
  }

  private ICteEnvironment getEnvironment() {
    return CteEngine.getInstance().getEnvironment();
  }

  /**
   * sanitizes path in order to ensure one of two:
   * 1 - internally converts from base root path '/' to MONDRIAN_SCHEMAS_BASE_ROOT
   * 2 - ensures path always starts with a '/'
   *
   * @param path path to sanitize
   * @return sanitized path
   */
  private String sanitize( final String path ) {

    String wrap = path;

    if ( StringUtils.isEmpty( path ) ) {
      return path;

    } else if ( path.equals( Util.SEPARATOR ) ) {
      // enforce root dir MONDRIAN_SCHEMAS_BASE_ROOT when path is the root '/'
      wrap = MONDRIAN_SCHEMAS_BASE_ROOT;

    } else if ( !path.startsWith( Util.SEPARATOR ) ) {
      // handle the always annoying "path starts with a '/' or not"
      wrap = Util.SEPARATOR + path;

    }

    return wrap;
  }

  private boolean clearMondrianCache() {

    try {
      // @see org.pentaho.platform.web.http.api.resources.SystemRefreshResource.flushMondrianSchemaCache();

      IPentahoSession session = PentahoSessionHolder.getSession();

      // Flush the catalog helper (legacy)
      IMondrianCatalogService
          mondrianCatalogService =
          PentahoSystem.get( IMondrianCatalogService.class, "IMondrianCatalogService", session ); //$NON-NLS-1$
      mondrianCatalogService.reInit( session );

      // Flush the IOlapService
      IOlapService olapService = PentahoSystem.get( IOlapService.class, IOlapService.class.getSimpleName(), session );
      olapService.flushAll( session );

    } catch ( Throwable t ) {
      logger.error( t.getMessage(), t );
        /* Do nothing.
         * <p/>
         * This is a simple 'nice-to-have' feature, where we actually clear mondrian cache after the user
         * makes a successful mondrian file change, so that he can immediately see its changes applied.
         * <p/>
         * In some off-chance this doesn't work, user can always do it via PUC > Tools > Refresh > Mondrian Cache.
        */
    }

    return true;
  }
}
