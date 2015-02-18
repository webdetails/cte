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
package pt.webdetails.cte;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pt.webdetails.cpf.SimpleContentGenerator;
import pt.webdetails.cte.engine.CteEngine;

public class CteContentGenerator extends SimpleContentGenerator {

  private static Log logger = LogFactory.getLog( CteContentGenerator.class );

  private String provider;

  @Override public void createContent() throws Exception {
    String path = getPathParameterAsString( Constants.PARAM_PATH, "" );

    if( StringUtils.isEmpty( provider ) ){
      logger.error( "No content provider id has been set; file loading may not function as expected" );
    }

    if( isCallForEditor() ) {

      getResponse().sendRedirect( CteEngine.getInstance().getEnvironment().getPluginBaseUrl() + "api/edit?path=" + path
              + "&provider=" + provider );

    } else {
      // back into the standard platform file handling with you...
      getResponse().sendRedirect( getRequest().getRequestURL().toString().replace( "/generatedContent", "/content" ) );
    }
  }

  @Override public String getPluginName() {
    return Constants.PLUGIN_ID;
  }

  private boolean isCallForEditor(){
    return getRequest().getRequestURI().endsWith( Constants.PLUGIN_EDITOR_PERSPECTIVE_ID );
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider( String provider ) {
    this.provider = provider;
  }
}
