package pt.webdetails.cte.editor.ace;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cte.api.ICteEditor;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.engine.CteEngine;

import javax.ws.rs.WebApplicationException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class AceEditor implements ICteEditor{

  private String extEditor;
  private List<String> blacklistedFolders;
  private List<String> blacklistedFileExtensions;

  private boolean bypassBlacklists = false; // super-admin hidden flag

  public AceEditor() {
    this( new ArrayList<String>(), new ArrayList<String>()  );
  }

  public AceEditor( List<String> blacklistedFolders , List<String> blacklistedFileExtensions ) {

    this.blacklistedFolders = blacklistedFolders != null ? blacklistedFolders : new ArrayList<String>();

    if( blacklistedFileExtensions != null ){

      ListIterator<String> iterator = blacklistedFileExtensions.listIterator();
      while ( iterator.hasNext() ) {
        iterator.set( iterator.next().toLowerCase() );
      }
    }

    this.blacklistedFileExtensions = blacklistedFileExtensions != null ?
        blacklistedFileExtensions : new ArrayList<String>();
  }

  private String getExtEditor() throws Exception {

    // sanitized calls; output is always the same ( ext-editor.html )

    if( extEditor == null ){
      extEditor = new ExtEditor( getEnvironment().getUrlProvider(), PluginEnvironment.repository() ).getExtEditor();
    }

    return extEditor;
  }

  @Override
  public boolean canRead( String path ) {
    IUserContentAccess access = getEnvironment().getUserContentAccess( null );

    return !StringUtils.isEmpty( path ) && access.fileExists( path ) && access.hasAccess( path, FileAccess.READ );
  }

  @Override
  public boolean canEdit( String path ) {
    IUserContentAccess access = getEnvironment().getUserContentAccess( null );

    return canRead( path ) && access.hasAccess( path, FileAccess.WRITE );
  }

  @Override
  public InputStream getEditor() throws Exception {
    return new ByteArrayInputStream( getExtEditor().getBytes( getEnvironment().getSystemEncoding() ) );
  }

  @Override
  public InputStream getEditor( String path ) throws Exception {
    if ( StringUtils.isEmpty( path ) ) {
      throw new WebApplicationException( 400 );
    }
    return new ByteArrayInputStream( getExtEditor().getBytes( getEnvironment().getSystemEncoding() ) );
  }

  @Override
  public InputStream getFile( String path ) throws Exception {
    IUserContentAccess access = getEnvironment().getUserContentAccess( null );

    if ( canRead( path ) ) {
      return access.getFileInputStream( path );
    } else {
      return null;
    }
  }

  @Override
  public boolean saveFile( String path, InputStream content ) throws Exception {

    IUserContentAccess access = getEnvironment().getUserContentAccess( null );

    if ( canEdit( path ) ) {
      return access.saveFile( path, content );
    }

    return false;
  }


  @Override
  public IBasicFile[] getTree( String dir, String[] allowedExtensions, boolean showHiddenFiles, boolean userIsAdmin ) throws Exception {

    if( allowedExtensions != null && allowedExtensions.length > 0 ) {

      return getFilteredTree( dir, allowedExtensions, showHiddenFiles, userIsAdmin );

    } else {

      return getStandardTree( dir, showHiddenFiles, userIsAdmin );

    }
  }

  private IBasicFile[] getFilteredTree( String dir, String[] allowedExtensions,
      boolean showHiddenFiles, boolean userIsAdmin ) throws Exception {

    List<String> allowedExtensionsList = Arrays.asList( allowedExtensions );

    IBasicFile[] files = getStandardTree( dir, showHiddenFiles, userIsAdmin );

    List<IBasicFile> filteredFileList = new ArrayList<IBasicFile>();

    for( IBasicFile file : files ) {

      if( !StringUtils.isEmpty( file.getExtension() )
          && allowedExtensionsList.contains( file.getExtension().toLowerCase() )  ) {
        filteredFileList.add( file );
      }
    }

    return filteredFileList.toArray( new IBasicFile[]{} );
  }

  private IBasicFile[] getStandardTree( String dir, boolean showHiddenFiles, boolean userIsAdmin ) throws Exception {

    IBasicFile[] files = new IBasicFile[] {};

    AceEditorFilter fileAndDirFilter = null;

    if( bypassBlacklists && userIsAdmin ){

      // Not to be trifled with

      // this is a super-admin hidden feature, where all blacklists are bypassed; user in session will
      // have access to system folders and any and all files that may contain sensitive information

      fileAndDirFilter = new AceEditorFilter( null, null, null, GenericBasicFileFilter.FilterType.FILTER_IN );

    } else {

      // act as a blacklist
      fileAndDirFilter = new AceEditorFilter( null, getBlacklistedFileExtensions().toArray( new String[] { } ),
          getBlacklistedFolders().toArray( new String[] { } ), GenericBasicFileFilter.FilterType.FILTER_OUT );
    }

    IUserContentAccess access = getEnvironment().getUserContentAccess( null );
    List<IBasicFile> fileList = access.listFiles( dir, fileAndDirFilter, 1, true, showHiddenFiles );

    if ( fileList != null && fileList.size() > 0 ) {

      List<IBasicFile> filteredFileList = new ArrayList<IBasicFile>();

      for( IBasicFile file : fileList ) {

        if( !isInBlacklistedFolder( file.getPath(), userIsAdmin ) && canRead( file.getPath() ) ) {
            filteredFileList.add( file );
        }
      }

      files = filteredFileList.toArray( new IBasicFile[ filteredFileList.size() ] );
    }

    return files;
  }

  private boolean isInBlacklistedFolder( String path, boolean userIsAdmin  ) {

    if( bypassBlacklists && userIsAdmin ){

      // Not to be trifled with

      // this is a super-admin hidden feature, where all blacklists are bypassed; user in session will
      // have access to system folders and any and all files that may contain sensitive information

      return false;
    }


    boolean isInBlacklistedFolder = false;

    if( !path.startsWith( Util.SEPARATOR ) ){
      path = Util.SEPARATOR + path;
    }

    for( String blacklistedFolder : getBlacklistedFolders() ) {
      isInBlacklistedFolder |= path.startsWith( blacklistedFolder );
    }

    return isInBlacklistedFolder;
  }

  public List<String> getBlacklistedFolders() {
    return blacklistedFolders;
  }

  public void setBlacklistedFolders( List<String> blacklistedFolders ) {
    this.blacklistedFolders = blacklistedFolders;
  }

  public List<String> getBlacklistedFileExtensions() {
    return blacklistedFileExtensions;
  }

  public void setBlacklistedFileExtensions( List<String> blacklistedFileExtensions ) {
    this.blacklistedFileExtensions = blacklistedFileExtensions;
  }

  public boolean isBypassBlacklists() {
    return bypassBlacklists;
  }

  public void setBypassBlacklists( boolean bypassBlacklists ) {
    this.bypassBlacklists = bypassBlacklists;
  }

  private ICteEnvironment getEnvironment(){
    return CteEngine.getInstance().getEnvironment();
  }
}

