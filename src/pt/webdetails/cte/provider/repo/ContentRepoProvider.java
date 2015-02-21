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
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cte.Constants;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.api.ICteProvider;
import pt.webdetails.cte.engine.CteEngine;
import pt.webdetails.cte.provider.GenericBasicFileFilter;
import pt.webdetails.cte.provider.GenericFileAndDirFilter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContentRepoProvider implements ICteProvider {

  private String id;
  private String name;

  private boolean bypassBlacklists = false; // super-admin hidden flag

  public ContentRepoProvider() {
  }

  @Override public boolean canRead( String path ) {
    IUserContentAccess access = getEnvironment().getUserContentAccess( null );

    return !StringUtils.isEmpty( path ) && access.fileExists( path ) && access.hasAccess( path, FileAccess.READ );
  }

  @Override public void init( ICteEnvironment environment ) throws InitializationException {
  }

  @Override public boolean isAccessible( IUserSession user ) {
    // any authenticated user can access this provider
    return user != null;
  }

  @Override public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  @Override public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  @Override public String[] getBlacklistedFolders() {
    // use settings.xml blacklisted folders
    return CteEngine.getInstance().getSettings().getBlacklistedFolders().toArray( new String[] { } );
  }

  @Override public String[] getBlacklistedFileExtensions() {
    // use settings.xml blacklisted file extensions
    return CteEngine.getInstance().getSettings().getBlacklistedFileExtensions().toArray( new String[] { } );
  }

  @Override public boolean canEdit( String path ) {
    IUserContentAccess access = getEnvironment().getUserContentAccess( null );

    return canRead( path ) && access.hasAccess( path, FileAccess.WRITE );
  }

  @Override public InputStream getFile( String path ) throws Exception {
    IUserContentAccess access = getEnvironment().getUserContentAccess( null );

    /**
     * the getFile needs to be in a canEdit validation ( and not in a canRead ),
     * a user that is not allowed to edit the file should not be allowed to check ( even if in read-only mode )
     * its contents, as it may hold some sensitive data.
     *
     * One example if that are the .ktr files:
     *
     * 1 - ktr's are nothing more than xml-based files
     * 2 - user suzy can only execute this item ( she cannot download them, therefore she is unable
     * to manipulate any of its contents )
     * 3 - if she were allowed to view the .ktr content using CTE ( in read-only mode ), she would see the
     * database connections declared within it ( wichi means she would be able to see the datasource connection url,
     * username and password )
     *
     */

    if ( canEdit( path ) ) {

      return access.getFileInputStream( path );

    } else {

      return access.getFileInputStream(
          Util.joinPath( getEnvironment().getPluginRepositoryDir(), Constants.PLUGIN_INVALID_PERMISSIONS_FILE ) );
    }
  }

  @Override public boolean saveFile( String path, InputStream content ) throws Exception {

    IUserContentAccess access = getEnvironment().getUserContentAccess( null );

    if ( canEdit( path ) ) {
      return access.saveFile( path, content );
    }

    return false;
  }

  @Override
  public IBasicFile[] getTree( String dir, String[] allowedExtensions, boolean showHiddenFiles ) throws Exception {

    // only admin users are allowed to use this feature
    showHiddenFiles = showHiddenFiles && getEnvironment().getUserSession().isAdministrator();

    if ( allowedExtensions != null && allowedExtensions.length > 0 ) {

      return getFilteredTree( dir, allowedExtensions, showHiddenFiles );

    } else {

      return getStandardTree( dir, showHiddenFiles );

    }
  }

  private IBasicFile[] getFilteredTree( String dir, String[] allowedExtensions, boolean showHiddenFiles ) throws Exception {

    List<String> allowedExtensionsList = Arrays.asList( allowedExtensions );

    IBasicFile[] files = getStandardTree( dir, showHiddenFiles );

    List<IBasicFile> filteredFileList = new ArrayList<IBasicFile>();

    for ( IBasicFile file : files ) {

      if ( !StringUtils.isEmpty( file.getExtension() ) && allowedExtensionsList
          .contains( file.getExtension().toLowerCase() ) ) {
        filteredFileList.add( file );
      }
    }

    return filteredFileList.toArray( new IBasicFile[] { } );
  }

  private IBasicFile[] getStandardTree( String dir, boolean showHiddenFiles ) throws Exception {

    IBasicFile[] files = new IBasicFile[] { };

    GenericFileAndDirFilter fileAndDirFilter = null;

    if ( bypassBlacklists && getEnvironment().getUserSession().isAdministrator() ) {

      // Not to be trifled with

      // this is a super-admin hidden feature, where all blacklists are bypassed; user in session will
      // have access to system folders and any and all files that may contain sensitive information

      fileAndDirFilter = new GenericFileAndDirFilter( null, null, null, GenericBasicFileFilter.FilterType.FILTER_IN );

    } else {

      // act as a blacklist
      fileAndDirFilter =
          new GenericFileAndDirFilter( null, getBlacklistedFileExtensions(), getBlacklistedFolders(),
              GenericBasicFileFilter.FilterType.FILTER_OUT );
    }

    IUserContentAccess access = getEnvironment().getUserContentAccess( null );
    List<IBasicFile> fileList = access.listFiles( dir, fileAndDirFilter, 1, true, showHiddenFiles );

    if ( fileList != null && fileList.size() > 0 ) {

      List<IBasicFile> filteredFileList = new ArrayList<IBasicFile>();

      for ( IBasicFile file : fileList ) {

        if ( !isInBlacklistedFolder( file.getPath() ) && canRead( file.getPath() ) ) {
          filteredFileList.add( file );
        }
      }

      files = filteredFileList.toArray( new IBasicFile[filteredFileList.size()] );
    }

    return files;
  }

  private boolean isInBlacklistedFolder( String path ) {

    if ( bypassBlacklists && getEnvironment().getUserSession().isAdministrator() ) {

      // Not to be trifled with

      // this is a super-admin hidden feature, where all blacklists are bypassed; user in session will
      // have access to system folders and any and all files that may contain sensitive information

      return false;
    }

    boolean isInBlacklistedFolder = false;

    if ( !path.startsWith( Util.SEPARATOR ) ) {
      path = Util.SEPARATOR + path;
    }

    for ( String blacklistedFolder : getBlacklistedFolders() ) {
      isInBlacklistedFolder |= path.startsWith( blacklistedFolder );
    }

    return isInBlacklistedFolder;
  }

  public boolean isBypassBlacklists() {
    return bypassBlacklists;
  }

  public void setBypassBlacklists( boolean bypassBlacklists ) {
    this.bypassBlacklists = bypassBlacklists;
  }

  private ICteEnvironment getEnvironment() {
    return CteEngine.getInstance().getEnvironment();
  }
}

