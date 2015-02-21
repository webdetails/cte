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
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.util.RepositoryHelper;
import pt.webdetails.cpf.utils.PluginIOUtils;
import pt.webdetails.cte.Constants;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.api.ICteProvider;
import pt.webdetails.cte.api.ICteProviderManager;
import pt.webdetails.cte.engine.CteEngine;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;

@Path( Constants.PLUGIN_ID + "/api/" ) public class CteApi {

  private Logger logger = LoggerFactory.getLogger( CteApi.class );

  // for ( an even higher ) ease of use
  private final String DIR = Constants.PARAM_DIR;
  private final String PATH = Constants.PARAM_PATH;
  private final String PROVIDER = Constants.PARAM_PROVIDER;


  @GET @Path( Constants.ENDPOINT_CAN_EDIT + "/{" + PROVIDER + ": [^?]+ }/{ " + PATH + ": [^?]+ }" )
  public String canEdit( @PathParam( PROVIDER ) String provider, @PathParam( PATH ) String path  ) {
    return Boolean.toString( isValidProvider( provider ) ? getProvider( provider ).canEdit( path ) : false );
  }

  @GET @Path( Constants.ENDPOINT_CAN_EDIT )
  public String canEditAlt( @QueryParam( PROVIDER ) String provider, @QueryParam( PATH ) String path ) {
    return canEdit( provider , path );
  }

  @GET @Path( Constants.ENDPOINT_CAN_READ + "/{" + PROVIDER + ": [^?]+ }/{ " + PATH + ": [^?]+ }" )
  public String canRead( @PathParam( PROVIDER ) String provider, @PathParam( PATH ) String path ) {
    return Boolean.toString( isValidProvider( provider ) ? getProvider( provider ).canRead( path ) : false );
  }

  @GET @Path( Constants.ENDPOINT_CAN_READ )
  public String canReadAlt( @QueryParam( PROVIDER ) String provider, @QueryParam( PATH ) String path ) {
    return canRead( provider, path );
  }

  @GET @Path( Constants.ENDPOINT_EDITOR + "/{" + PROVIDER + ": [^?]+ }/{ " + PATH + ": [^?]+ }" )
  public void edit( @PathParam( PROVIDER ) String provider, @PathParam( PATH ) String path,
      @Context HttpServletResponse response ) throws WebApplicationException {

    try {
      PluginIOUtils.writeOutAndFlush( response.getOutputStream(), getEngine().getEditor().getEditor() );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @GET @Path( Constants.ENDPOINT_EDITOR )
  public void editAlt( @QueryParam( PROVIDER ) String provider, @QueryParam( PATH ) String path,
      @Context HttpServletResponse response ) throws WebApplicationException {
    edit( provider, path, response );
  }

  @GET @Path( Constants.ENDPOINT_GET_FILE + "/{" + PROVIDER + ": [^?]+ }/{ " + PATH + ": [^?]+ }" )
  public void getFile( @PathParam( PROVIDER ) String provider, @PathParam( PATH ) String path,
      @Context HttpServletResponse response ) throws WebApplicationException {

    if ( !isValidProvider( provider ) || !getProvider( provider ).canRead( path ) ) {
      logger.info( "CteApi.getFile(): not allowed to read " + path );
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }

    try {
      PluginIOUtils.writeOutAndFlush( response.getOutputStream(), getProvider( provider ).getFile( path ) );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @GET @Path( Constants.ENDPOINT_GET_FILE )
  public void getFileAlt( @QueryParam( PROVIDER ) String provider, @QueryParam( PATH ) String path,
      @Context HttpServletResponse response ) throws WebApplicationException {
    getFile( provider, path, response );
  }

  @POST @Path( Constants.ENDPOINT_SAVE_FILE + "/{" + PROVIDER + ": [^?]+ }/{ " + PATH + ": [^?]+ }" )
  public String saveFile( @PathParam( PROVIDER ) String provider, @PathParam( PATH ) String path,
      @FormParam( Constants.PARAM_DATA ) @DefaultValue( "" ) String data ) throws WebApplicationException {

    if ( !isValidProvider( provider ) ||  !getProvider( provider ).canEdit( path ) ) {
      logger.info( "CteApi.saveFile(): not allowed to edit " + path );
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }

    try {
      return String.valueOf( getProvider( provider ).saveFile( path,
          new ByteArrayInputStream( data.getBytes( getEnvironment().getSystemEncoding() ) ) ) );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @POST @Path( Constants.ENDPOINT_SAVE_FILE )
  public String saveFileAlt( @FormParam( PROVIDER ) String provider, @FormParam( PATH ) String path,
      @FormParam( Constants.PARAM_DATA ) @DefaultValue( "" ) String data ) throws WebApplicationException {
    return saveFile( provider, path, data );
  }

  @GET @Path( Constants.ENDPOINT_PROVIDERS )
  public String providers() {

    try {
      JSONArray jsonArray = new JSONArray();

      for ( ICteProvider provider : getProviderManager().getProviders() ) {

        // check if this user has access to this provider
        if( provider.isAccessible( getEnvironment().getUserSession() ) ) {

          JSONObject jsonObj = new JSONObject();
          jsonObj.put( Constants.PARAM_ID, provider.getId() );
          jsonObj.put( Constants.PARAM_NAME, provider.getName() );

          jsonArray.put( jsonObj );

        }
      }

      return jsonArray.toString( 2 );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @GET @Path( Constants.ENDPOINT_TREE_EXPLORE + "/{" + PROVIDER + ": [^?]+ }/{ " + DIR + ": [^?]+ }" )
  public String tree( @PathParam( PROVIDER ) String provider, @PathParam( DIR ) @DefaultValue( "/" ) String dir,
      @QueryParam( Constants.PARAM_FILE_EXTENSIONS ) String commaSeparatedExtensions,
      @QueryParam( Constants.PARAM_SHOW_HIDDEN_FILES ) @DefaultValue( "false" ) boolean showHiddenFiles )
      throws WebApplicationException {

    String[] allowedExtensions = new String[] { };
    if ( !StringUtils.isEmpty( commaSeparatedExtensions ) ) {

      allowedExtensions = commaSeparatedExtensions.contains( "," ) ? commaSeparatedExtensions.toLowerCase().split( "," ) :
              new String[] { commaSeparatedExtensions.toLowerCase() };
    }

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
            getProvider( provider ).getTree( dir, allowedExtensions, showHiddenFiles ) );

      }

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @GET @Path( Constants.ENDPOINT_TREE_EXPLORE )
  public String treeAlt( @QueryParam( PROVIDER ) String provider,
      @QueryParam( Constants.PARAM_DIR ) @DefaultValue( "/" ) String dir,
      @QueryParam( Constants.PARAM_FILE_EXTENSIONS ) @DefaultValue( StringUtils.EMPTY ) String commaSeparatedExtensions,
      @QueryParam( Constants.PARAM_SHOW_HIDDEN_FILES ) @DefaultValue( "false" ) boolean showHiddenFiles )
      throws WebApplicationException {
    return tree( provider, dir, commaSeparatedExtensions, showHiddenFiles );
  }

  private String fullTree( String[] allowedExtensions, boolean showHiddenFiles ) throws WebApplicationException {

    try {

      StringBuffer sb = new StringBuffer();

      for ( ICteProvider provider : getProviderManager().getProviders() ) {

        IBasicFile[] tree = provider.getTree( Util.SEPARATOR , allowedExtensions, showHiddenFiles );

        sb.append( RepositoryHelper.toJQueryFileTree( provider.getId() + Util.SEPARATOR , tree ) );
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

    } else if( !getProvider( provider ).isAccessible( getEnvironment().getUserSession() ) ) {
      throw new WebApplicationException( "user has been denied access to provider", Response.Status.FORBIDDEN );
    }

    return true;
  }

  private ICteProviderManager getProviderManager() {
    return getEngine().getProviderManager();
  }

  private ICteProvider getProvider( String providerId ) {
    return getEngine().getProviderManager().getProviderById( providerId );
  }

  private CteEngine getEngine() {
    return CteEngine.getInstance();
  }

  private ICteEnvironment getEnvironment() {
    return CteEngine.getInstance().getEnvironment();
  }
}
