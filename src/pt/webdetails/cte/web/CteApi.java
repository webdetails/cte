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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.util.RepositoryHelper;
import pt.webdetails.cpf.utils.PluginIOUtils;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cte.Constants;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.api.ICteProvider;
import pt.webdetails.cte.api.ICteProviderManager;
import pt.webdetails.cte.engine.CteEngine;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


@Path( Constants.PLUGIN_ID + "/api/" ) public class CteApi {

  private Logger logger = LoggerFactory.getLogger( CteApi.class );

  // default/fallback provider to use whenever CTE's editor is called for, and no provider is explicitly passed along
  private String defaultProviderId;

  // for ( an even higher ) ease of use
  private final String DIR = Constants.PARAM_DIR;
  private final String PATH = Constants.PARAM_PATH;
  private final String PROVIDER = Constants.PARAM_PROVIDER;


  @GET @Path( Constants.ENDPOINT_CAN_EDIT + "/{" + PROVIDER + ": [^?]+ }/{ " + PATH + ": [^?]+ }" )
  @Produces( MimeTypes.PLAIN_TEXT )
  public String canEdit( @PathParam( PROVIDER ) String provider, @PathParam( PATH ) String path ) {
    return Boolean.toString( isValidProvider( provider ) ? getProvider( provider ).canEdit( sanitize( path ) ) : false );
  }

  @GET @Path( Constants.ENDPOINT_CAN_EDIT )
  @Produces( MimeTypes.PLAIN_TEXT )
  public String canEditAlt( @QueryParam( PROVIDER ) String provider, @QueryParam( PATH ) String path ) {
    return canEdit( provider , path );
  }

  @GET @Path( Constants.ENDPOINT_CAN_READ + "/{" + PROVIDER + ": [^?]+ }/{ " + PATH + ": [^?]+ }" )
  @Produces( MimeTypes.PLAIN_TEXT )
  public String canRead( @PathParam( PROVIDER ) String provider, @PathParam( PATH ) String path ) {
    return Boolean.toString( isValidProvider( provider ) ? getProvider( provider ).canRead( sanitize( path ) ) : false );
  }

  @GET @Path( Constants.ENDPOINT_CAN_READ )
  @Produces( MimeTypes.PLAIN_TEXT )
  public String canReadAlt( @QueryParam( PROVIDER ) String provider, @QueryParam( PATH ) String path ) {
    return canRead( provider, path );
  }

  @GET @Path( Constants.ENDPOINT_EDITOR + "/{" + PROVIDER + ": [^?]+ }/{ " + PATH + ": [^?]+ }" )
  @Produces( MimeTypes.HTML )
  public void edit( @PathParam( PROVIDER ) String provider, @PathParam( PATH ) String path,
      @QueryParam( Constants.PARAM_BYPASS_CACHE ) @DefaultValue( "false" ) String bypassCache,
      @Context HttpServletResponse response ) throws WebApplicationException {

    try {

      InputStream fis = null;

      if( !StringUtils.isEmpty( path ) && isValidProvider( provider ) && getProvider( provider ).canRead( sanitize( path ) ) ){

        fis = getProvider( provider ).getFile( sanitize( path ) );
      }

      PluginIOUtils.writeOutAndFlush( response.getOutputStream(),
          getEngine().getEditor().getEditor( fis, BooleanUtils.toBoolean( bypassCache ) ) );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @GET @Path( Constants.ENDPOINT_EDITOR )
  @Produces( MimeTypes.HTML )
  public void editAlt( @QueryParam( PROVIDER ) String provider, @QueryParam( PATH ) String path,
      @QueryParam( Constants.PARAM_BYPASS_CACHE ) @DefaultValue( "false" ) String bypassCache,
      @Context HttpServletResponse response ) throws WebApplicationException {
    edit( provider, path, bypassCache, response );
  }

  @GET @Path( Constants.ENDPOINT_GET_FILE + "/{" + PROVIDER + ": [^?]+ }/{ " + PATH + ": [^?]+ }" )
  public void getFile( @PathParam( PROVIDER ) String provider, @PathParam( PATH ) String path,
      @Context HttpServletResponse response ) throws WebApplicationException {

    if ( !isValidProvider( provider ) || !getProvider( provider ).canRead( sanitize( path ) ) ) {
      logger.info( "CteApi.getFile(): not allowed to read " + path );
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }

    try {
      PluginIOUtils.writeOutAndFlush( response.getOutputStream(), getProvider( provider ).getFile( sanitize( path ) ) );

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
  @Produces( MimeTypes.PLAIN_TEXT )
  public String saveFile( @PathParam( PROVIDER ) String provider, @PathParam( PATH ) String path,
      @FormParam( Constants.PARAM_DATA ) @DefaultValue( "" ) String data ) throws WebApplicationException {

    if ( !isValidProvider( provider ) ||  !getProvider( provider ).canEdit( sanitize( path ) ) ) {
      logger.info( "CteApi.saveFile(): not allowed to edit " + path );
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }

    try {
      return String.valueOf( getProvider( provider ).saveFile( sanitize( path ),
          new ByteArrayInputStream( data.getBytes( getEnvironment().getSystemEncoding() ) ) ) );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @POST @Path( Constants.ENDPOINT_SAVE_FILE )
  @Produces( MimeTypes.PLAIN_TEXT )
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


      } else if ( !isValidProvider( provider ) || !getProvider( provider ).canRead( sanitize( dir ) ) ) {

        // tree of a specific provider

        logger.info( "CteApi.tree(): not allowed to read " + dir );
        throw new WebApplicationException( Response.Status.FORBIDDEN );

      } else {

        return RepositoryHelper.toJQueryFileTree( sanitize( dir ),
            getProvider( provider ).getTree( sanitize( dir ), allowedExtensions, showHiddenFiles ) );

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

    if( StringUtils.isEmpty( provider ) && StringUtils.isEmpty( getDefaultProviderId() ) ){
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

    if( !StringUtils.isEmpty( providerId ) ) {
      return getEngine().getProviderManager().getProviderById( providerId );

    } else if ( !StringUtils.isEmpty( getDefaultProviderId() ) ) {
      logger.debug( "Tentative default/fallback provider usage" );
      return getEngine().getProviderManager().getProviderById( getDefaultProviderId() );
    }

    return null;
  }

  private CteEngine getEngine() {
    return CteEngine.getInstance();
  }

  private ICteEnvironment getEnvironment() {
    return CteEngine.getInstance().getEnvironment();
  }

  public String getDefaultProviderId() {
    return defaultProviderId;
  }

  public void setDefaultProviderId( String defaultProviderId ) {
    this.defaultProviderId = defaultProviderId;
  }

  // path may be colons or forward slashes as separators;
  // if using colons, convert all to forward slashes
  private String sanitize( String path ){

    if( !StringUtils.isEmpty( path ) && path.contains( Constants.SEPARATOR_ALT ) ) {
      return path.replaceAll( Constants.SEPARATOR_ALT, Constants.SEPARATOR );
    }

    return path;
  }
}
