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
package pt.webdetails.cte.web;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.util.RepositoryHelper;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpf.utils.PluginIOUtils;
import pt.webdetails.cte.Constants;
import pt.webdetails.cte.api.ICteProvider;
import pt.webdetails.cte.provider.CteProviderManager;
import pt.webdetails.cte.engine.CteEngine;
import pt.webdetails.cte.utils.SessionUtils;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;

@Path( Constants.PLUGIN_ID + "/api/" ) public class CteApi {

  private Logger logger = LoggerFactory.getLogger( CteApi.class );

  // for ( a higher ) ease of use
  private final String PATH = Constants.PARAM_PATH;
  private final String PROVIDER = Constants.PARAM_PROVIDER;

  @GET @Path( Constants.ENDPOINT_CAN_EDIT )
  public String canEdit( @QueryParam( PATH ) String path, @QueryParam( PROVIDER ) String provider ) {
      return Boolean.toString( isValidProvider( provider ) ? getProvider( provider ).canEdit( path ) : false );
  }

  @GET @Path( Constants.ENDPOINT_CAN_READ )
  public String canRead( @QueryParam( PATH ) String path, @QueryParam( PROVIDER ) String provider ) {
    return Boolean.toString( isValidProvider( provider ) ? getProvider( provider ).canRead( path ) : false );
  }

  @GET @Path( Constants.ENDPOINT_EDITOR )
  public void edit( @QueryParam( PATH ) String path,  @QueryParam( PROVIDER ) String provider,
      @Context HttpServletResponse servletResponse ) throws WebApplicationException {

    try {
      PluginIOUtils.writeOutAndFlush( servletResponse.getOutputStream(), getEngine().getEditor().getEditor() );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @GET @Path( Constants.ENDPOINT_GET_FILE )
  public void getFile( @QueryParam( PATH ) String path, @QueryParam( PROVIDER ) String provider,
      @Context HttpServletResponse servletResponse ) throws WebApplicationException {

    if ( !isValidProvider( provider ) || !getProvider( provider ).canRead( path ) ) {
      logger.info( "CteApi.getFile(): not allowed to read " + path );
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }

    try {
      PluginIOUtils.writeOutAndFlush( servletResponse.getOutputStream(), getProvider( provider ).getFile( path ) );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @POST @Path( Constants.ENDPOINT_SAVE_FILE )
  public String saveFile( @FormParam( PATH ) String path, @FormParam( PROVIDER ) String provider,
      @FormParam( Constants.PARAM_DATA ) @DefaultValue( StringUtils.EMPTY ) String data ) throws WebApplicationException {

    if ( !isValidProvider( provider ) ||  !getProvider( provider ).canEdit( path ) ) {
      logger.info( "CteApi.saveFile(): not allowed to edit " + path );
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }

    try {
      return String.valueOf( getProvider( provider ).saveFile( path,
              new ByteArrayInputStream( data.getBytes( getEngine().getEnvironment().getSystemEncoding() ) ) ) );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @GET @Path( Constants.ENDPOINT_PROVIDERS )
  public String providers() {

    try {
      JSONArray jsonArray = new JSONArray();

      for ( ICteProvider provider : getProviderManager().getProviders() ) {

        JSONObject jsonObj = new JSONObject();
        jsonObj.put( "id", provider.getId() );
        jsonObj.put( "name", provider.getName() );

        jsonArray.put( jsonObj );
      }

      return jsonArray.toString( 2 );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @GET @Path( Constants.ENDPOINT_TREE_EXPLORE ) @Produces( MimeTypes.PLAIN_TEXT )
  public String tree( @QueryParam( Constants.PARAM_DIR ) @DefaultValue( "/" ) String dir,
      @QueryParam( PROVIDER ) String provider,
      @QueryParam( Constants.PARAM_FILE_EXTENSIONS ) @DefaultValue( StringUtils.EMPTY )
      String commaSeparatedAllowedExtensions,
      @QueryParam( Constants.PARAM_SHOW_HIDDEN_FILES ) @DefaultValue( "false" ) boolean showHiddenFiles )
      throws WebApplicationException {

    String[] allowedExtensions = new String[] { };
    if ( !StringUtils.isEmpty( commaSeparatedAllowedExtensions ) ) {

      allowedExtensions =
          commaSeparatedAllowedExtensions.contains( "," ) ? commaSeparatedAllowedExtensions.toLowerCase().split( "," ) :
              new String[] { commaSeparatedAllowedExtensions.toLowerCase() };
    }

    boolean isAdmin = SessionUtils.userInSessionIsAdmin();

    try {

      if( StringUtils.isEmpty( provider ) ){

        // a tree with all providers
        return fullTree( allowedExtensions , showHiddenFiles );


      } else if ( !isValidProvider( provider ) || !getProvider( provider ).canRead( dir ) ) {

        // tree of a specific provider

        logger.info( "CteApi.tree(): not allowed to read " + dir );
        throw new WebApplicationException( Response.Status.FORBIDDEN );

      } else {

        return RepositoryHelper.toJQueryFileTree( dir,
            getProvider( provider ).getTree( dir, allowedExtensions, showHiddenFiles, isAdmin ) );

      }

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  private String fullTree( String[] allowedExtensions, boolean showHiddenFiles ) throws WebApplicationException {

    try {

      StringBuffer sb = new StringBuffer();

      boolean isAdmin = SessionUtils.userInSessionIsAdmin();

      for ( ICteProvider provider : getProviderManager().getProviders() ) {

        IBasicFile[] tree = provider.getTree( "/" , allowedExtensions, showHiddenFiles, isAdmin );

        sb.append( RepositoryHelper.toJQueryFileTree( provider.getId() + "/" , tree ) );
      }

      return sb.toString();

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  private boolean isValidProvider( String provider ) throws WebApplicationException {

    if( StringUtils.isEmpty( provider ) ){
      throw new WebApplicationException( "missing provider", Response.Status.FORBIDDEN );

    } else if( getProvider( provider ) == null ){
      throw new WebApplicationException( "invalid/unknown provider", Response.Status.FORBIDDEN );
    }

    return true;
  }

  private CteProviderManager getProviderManager() {
    return getEngine().getProviderManager();
  }

  private ICteProvider getProvider( String providerId ) {
    return getEngine().getProviderManager().getProviderById( providerId );
  }

  private CteEngine getEngine() {
    return CteEngine.getInstance();
  }
}
