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
package pt.webdetails.cte.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.Util;
import pt.webdetails.cte.Constants;
import pt.webdetails.cte.api.ICteEditor;
import pt.webdetails.cte.engine.CteEngine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class DefaultCdaLeveragedEditor implements ICteEditor {

  private Logger logger = LoggerFactory.getLogger( DefaultCdaLeveragedEditor.class );

  @Override
  public boolean canRead( String path ) {
    return true; /* CDA takes care of this */
  }

  @Override
  public boolean canEdit( String path ) {

    Map<String, Object> params = new HashMap<String, Object>();

    params.put( "path", path );
    String reply = doCDAInterPluginCall( "canEdit", params );

    return reply != null && reply.contains( "true" );
  }

  @Override
  public InputStream getEditor() throws Exception {
    return getEditor( Util.joinPath( CteEngine.getInstance().getEnvironment().getPluginRepositoryDir(),
        Constants.PLUGIN_WELCOME_FILE ) );
  }

  @Override
  public InputStream getEditor( String path ) throws Exception {

    Map<String, Object> params = new HashMap<String, Object>();

    params.put( "path", path );
    String reply = doCDAInterPluginCall( "getExtEditor", params );

    if ( reply != null ) {
      return new ByteArrayInputStream( reply.getBytes( Charset.defaultCharset() ) );
    }
    return null;
  }

  @Override
  public InputStream getFile( String path ) throws Exception {
    return null; /* CDA takes care of this */
  }

  @Override
  public boolean saveFile( String path, InputStream fileContents ) throws Exception {
    return false; /* CDA takes care of this */
  }

  private String doCDAInterPluginCall( String method, Map<String, Object> params ){
    InterPluginCall ipc = new InterPluginCall( InterPluginCall.CDA, method, params );
    return ipc.call();
  }
}
