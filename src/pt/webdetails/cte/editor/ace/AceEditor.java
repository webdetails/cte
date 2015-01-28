package pt.webdetails.cte.editor.ace;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cte.api.ICteEditor;
import pt.webdetails.cte.engine.CteEngine;

import javax.ws.rs.WebApplicationException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class AceEditor implements ICteEditor{

  @Override public boolean canEdit( String path ) {
    return true; // TODO: implement security logic
  }

  @Override public InputStream getEditor( String path ) throws Exception {
    if ( StringUtils.isEmpty( path ) ) {
      throw new WebApplicationException( 400 );
    }
    return new ByteArrayInputStream( getEditor().getExtEditor().getBytes( CteEngine.getInstance().getEnvironment().getSystemEncoding() ) );
  }

  private ExtEditor getEditor() {
    return new ExtEditor( CteEngine.getInstance().getEnvironment().getUrlProvider(), PluginEnvironment.repository() );
    //return new ExtEditor( PluginEnvironment.env().getUrlProvider(), CteEngine.getInstance().getEnvironment().getRepo() );

  }

  @Override public InputStream getFile( String path ) throws Exception {
    IUserContentAccess access = CteEngine.getInstance().getEnvironment().getUserContentAccess( null );

    if ( !StringUtils.isEmpty( path ) && access.fileExists( path ) ) {
      return access.getFileInputStream( path );
    } else {
      return null;
    }

  }

  @Override public boolean saveFile( String path, InputStream fileContents ) throws Exception {
    return false;
  }
}
