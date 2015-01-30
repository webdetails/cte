package pt.webdetails.cte.editor.ace;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cpf.PluginEnvironment;
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
import java.util.List;

public class AceEditor implements ICteEditor{

  private String extEditor;

  public AceEditor() {}

  private String getExtEditor() throws Exception {

    // sanitize calls; output is always the same ( ext-editor.html )

    if( extEditor == null ){
      extEditor = new ExtEditor( getEnvironment().getUrlProvider(), PluginEnvironment.repository() ).getExtEditor();
      //return new ExtEditor( PluginEnvironment.env().getUrlProvider(), CteEngine.getInstance().getEnvironment().getRepo() );
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
  public IBasicFile[] getTree( String dir, String fileExtensions, boolean showHiddenFiles, boolean userIsAdmin ) {

    ArrayList<String> extensionsList = new ArrayList<String>();
    String[] extensions = StringUtils.split( fileExtensions, "." );

    if ( extensions != null ) {
      for ( String extension : extensions ) {
        extensionsList.add( extension );
      }
    }

    GenericBasicFileFilter fileFilter =
        new GenericBasicFileFilter( null, extensionsList.toArray( new String[ extensionsList.size() ] ), true );

    GenericFileAndDirectoryFilter fileAndDirFilter = new GenericFileAndDirectoryFilter( fileFilter );

    // folder filtering ( see settings.xml ) will only occur for non-admin users
    if( !userIsAdmin ) {
        // TODO
        //fileAndDirFilter.setDirectories( CdeSettings.getFilePickerHiddenFolderPaths( CdeSettings.FolderType.REPO ) );
        //fileAndDirFilter.setFilterType( GenericFileAndDirectoryFilter.FilterType.FILTER_OUT ); // act as a black-list
    }

    IUserContentAccess access = getEnvironment().getUserContentAccess( null );
    List<IBasicFile> fileList = access.listFiles( dir, fileAndDirFilter, 1, true, showHiddenFiles );

    if ( fileList != null && fileList.size() > 0 ) {
      return fileList.toArray( new IBasicFile[ fileList.size() ] );
    }

    return new IBasicFile[] { };
  }

  private ICteEnvironment getEnvironment(){
    return CteEngine.getInstance().getEnvironment();
  }
}

