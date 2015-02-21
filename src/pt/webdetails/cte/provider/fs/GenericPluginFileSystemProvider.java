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
package pt.webdetails.cte.provider.fs;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.api.ICteProvider;
import pt.webdetails.cte.engine.CteEngine;
import pt.webdetails.cte.provider.GenericBasicFileFilter;
import pt.webdetails.cte.provider.GenericFileAndDirFilter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericPluginFileSystemProvider implements ICteProvider {

  private Logger logger = LoggerFactory.getLogger( GenericPluginFileSystemProvider.class );

  private String id;
  private String name;

  IReadAccess readAccess;
  IRWAccess writeAccess;

  String[] blacklistedFolders;
  String[] blacklistedFileExtensions;

  public GenericPluginFileSystemProvider( String id ) {
    setId( id );
  }

  @Override public void init( ICteEnvironment environment ) throws InitializationException {

    logger.info( "Initializing GenericPluginFileSystemProvider for plugin " + getId() +
        ". Note that this is an admin-only provider, as it 'opens' plenty of doors ( that are best remained shut )" );

    if ( StringUtils.isEmpty( getId() ) ) {
      throw new InitializationException( "Missing required unique ID; "
          + "This class uses this ID for 2 things simultaneously: 1 - unique provider ID; 2 - plugin ID", null );
    }

    logger.info( "Initializing system access objects ( read/write ) for plugin " + getId() + " ..." );

    try {

      if ( getId().equalsIgnoreCase( environment.getPluginId() ) ) {

        setReadAccess( environment.getPluginSystemReader( null ) );
        setWriteAccess( environment.getPluginSystemWriter( null ) );

      } else {

        setReadAccess( environment.getOtherPluginSystemReader( getId(), null ) );
        setWriteAccess( environment.getOtherPluginSystemWriter( getId(), null ) );

      }

      if ( getReadAccess() != null && getWriteAccess() != null ) {
        logger.info( "Initialization successful" );
      } else {
        logger.error( "Initialization failed; this provider may not work as intended" );
      }

    } catch ( Exception e ) {
      throw new InitializationException( "Error while initializing system access objects for plugin " + getId(), e );
    }
  }

  @Override public boolean isAccessible( IUserSession user ) {
    // this provider offers the means to edit system files; so this needs to be an admin-only provider
    return getEnvironment().getUserSession().isAdministrator();
  }

  @Override public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  @Override public String getName() {
    if ( StringUtils.isEmpty( name ) ) {
      name = getId().toUpperCase() + " Plugin File System"; // default
    }
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  @Override public String[] getBlacklistedFolders() {

    if ( blacklistedFolders == null ) {

      // use settings.xml blacklisted folders
      blacklistedFolders = CteEngine.getInstance().getSettings().getBlacklistedFolders().toArray( new String[] { } );
    }

    return blacklistedFolders;
  }

  public void setBlacklistedFolders( String[] blacklistedFolders ) {
    this.blacklistedFolders = blacklistedFolders;
  }

  @Override public String[] getBlacklistedFileExtensions() {

    if ( blacklistedFileExtensions == null ) {

      // use settings.xml blacklisted file extensions
      blacklistedFileExtensions =
          CteEngine.getInstance().getSettings().getBlacklistedFileExtensions().toArray( new String[] { } );
    }

    return blacklistedFileExtensions;
  }

  public void setBlacklistedFileExtensions( String[] blacklistedFileExtensions ) {
    this.blacklistedFileExtensions = blacklistedFileExtensions;
  }

  @Override public boolean canEdit( String path ) {

    // this is an admin-only provider, as it 'opens' plenty of doors ( that are best remained shut )

    logger.debug( "GenericPluginFileSystemProvider.canEdit(): note that this is an admin-only provider" );

    return canRead( path );
  }

  @Override public boolean canRead( String path ) {

    // this is an admin-only provider, as it 'opens' plenty of doors ( that are best remained shut )

    logger.debug( "GenericPluginFileSystemProvider.canRead(): note that this is an admin-only provider" );

    return getEnvironment().getUserSession().isAdministrator() && !StringUtils.isEmpty( path ) && getReadAccess()
        .fileExists( path ) && getReadAccess().fetchFile( path ) != null;
  }

  @Override public InputStream getFile( String path ) throws Exception {
    return canEdit( path ) ? getReadAccess().getFileInputStream( path ) : null;
  }

  @Override public boolean saveFile( String path, InputStream content ) throws Exception {
    return canEdit( path ) ? getWriteAccess().saveFile( path, content ) : false;
  }

  @Override public IBasicFile[] getTree( String dir, String[] allowedExtensions, boolean showHiddenFiles )
      throws Exception {

    // only admin users are allowed to use this feature
    showHiddenFiles = showHiddenFiles && getEnvironment().getUserSession().isAdministrator();

    if ( allowedExtensions != null && allowedExtensions.length > 0 ) {

      return getFilteredTree( dir, allowedExtensions, showHiddenFiles );

    } else {

      return getStandardTree( dir, showHiddenFiles );

    }
  }

  private IBasicFile[] getFilteredTree( String dir, String[] allowedExtensions, boolean showHiddenFiles )
      throws Exception {

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

    GenericFileAndDirFilter
        fileAndDirFilter =
        new GenericFileAndDirFilter( null, getBlacklistedFileExtensions(), getBlacklistedFolders(),
            GenericBasicFileFilter.FilterType.FILTER_OUT );

    List<IBasicFile> fileList = readAccess.listFiles( dir, fileAndDirFilter, 1, true, showHiddenFiles );

    if ( fileList != null && fileList.size() > 0 ) {

      // weird thing when using SystemPluginResourceAccess.listFiles() :
      // for a given dir, the listFiles() result set *also includes* the dir itself...
      // ex: see cde-pentaho5#ResourcesApi ( same fix being applied there )

      fileList.remove( 0 ); //remove the first because the root is being added

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

    boolean isInBlacklistedFolder = false;

    if ( !path.startsWith( Util.SEPARATOR ) ) {
      path = Util.SEPARATOR + path;
    }

    for ( String blacklistedFolder : getBlacklistedFolders() ) {
      isInBlacklistedFolder |= path.startsWith( blacklistedFolder );
    }

    return isInBlacklistedFolder;
  }

  private ICteEnvironment getEnvironment() {
    return CteEngine.getInstance().getEnvironment();
  }

  public IReadAccess getReadAccess() {
    return readAccess;
  }

  public void setReadAccess( IReadAccess readAccess ) {
    this.readAccess = readAccess;
  }

  public IRWAccess getWriteAccess() {
    return writeAccess;
  }

  public void setWriteAccess( IRWAccess writeAccess ) {
    this.writeAccess = writeAccess;
  }
}
