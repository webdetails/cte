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
package pt.webdetails.cte.editor.cda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cte.Constants;
import pt.webdetails.cte.api.ICteEditor;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.engine.CteEngine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class CdaLeveragedEditor implements ICteEditor {

  private Logger logger = LoggerFactory.getLogger( CdaLeveragedEditor.class );

  @Override public void init( ICteEnvironment environment ) throws InitializationException {
  }

  @Override
  public InputStream getEditor() throws Exception {
    return getEditor( null );
  }

  @Override
  public InputStream getEditor( InputStream fileContent ) throws Exception {

    Map<String, Object> params = new HashMap<String, Object>();

    params.put( "path", Util.joinPath( CteEngine.getInstance().getEnvironment().getPluginRepositoryDir(),
        Constants.PLUGIN_WELCOME_FILE ) );

    String reply = doCDAInterPluginCall( "getExtEditor", params );

    if ( reply != null ) {
      return new ByteArrayInputStream( reply.getBytes( Charset.defaultCharset() ) );
    }
    return null;
  }

  private String doCDAInterPluginCall( String method, Map<String, Object> params ) {
    InterPluginCall ipc = new InterPluginCall( InterPluginCall.CDA, method, params );
    return ipc.call();
  }
}
