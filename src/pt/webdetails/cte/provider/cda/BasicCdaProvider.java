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
package pt.webdetails.cte.provider.cda;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.api.ICteProvider;
import pt.webdetails.cte.engine.CteEngine;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BasicCdaProvider implements ICteProvider {

  private Logger logger = LoggerFactory.getLogger( BasicCdaProvider.class );

  @Override
  public boolean canRead( String path ) {
    return !StringUtils.isEmpty( path ); /* CDA takes care of this */
  }

  @Override public void init( ICteEnvironment environment ) throws InitializationException {
  }

  @Override public boolean isAccessible( IUserSession user ) {
    return true;
  }

  @Override public String getId() {
    return "cda-leveraged";
  }

  @Override public String getName() {
    return "Cda Leveraged";
  }

  @Override public String[] getBlacklistedFolders() {
    return CteEngine.getInstance().getSettings().getBlacklistedFolders().toArray( new String[] { } );
  }

  @Override public String[] getBlacklistedFileExtensions() {
    return CteEngine.getInstance().getSettings().getBlacklistedFileExtensions().toArray( new String[] { } );
  }

  @Override
  public boolean canEdit( String path ) {

    if ( StringUtils.isEmpty( path ) ){
      return false;
    }

    Map<String, Object> params = new HashMap<String, Object>();

    params.put( "path", path );
    String reply = doCDAInterPluginCall( "canEdit", params );

    return reply != null && reply.toLowerCase().contains( "true" );
  }

  @Override
  public InputStream getFile( String path ) throws Exception {
    return null; /* CDA takes care of this */
  }

  @Override
  public boolean saveFile( String path, InputStream fileContents ) throws Exception {
    return false; /* CDA takes care of this */
  }

  @Override
  public IBasicFile[] getTree( String dir, String[] allowedExtensions, boolean showHiddenFiles ) throws Exception{
    return null; /* CDA leveraged ACE editor doesn't provide us with a file tree... */
  }

  private String doCDAInterPluginCall( String method, Map<String, Object> params ){
    InterPluginCall ipc = new InterPluginCall( InterPluginCall.CDA, method, params );
    return ipc.call();
  }
}
