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
package pt.webdetails.cte.web;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.repository.util.RepositoryHelper;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpf.utils.PluginIOUtils;
import pt.webdetails.cte.Constants;
import pt.webdetails.cte.api.ICteEditor;
import pt.webdetails.cte.engine.CteEngine;
import pt.webdetails.cte.utils.SessionUtils;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;

@Path( Constants.PLUGIN_ID + "/api/" ) public class CteApi {

  private Logger logger = LoggerFactory.getLogger( CteApi.class );

  @GET @Path( Constants.ENDPOINT_CAN_EDIT )
  public String canEdit( @QueryParam( Constants.PARAM_PATH ) String path ) {
    return Boolean.toString( getCteEditor().canEdit( path ) );
  }

  @GET @Path( Constants.ENDPOINT_CAN_READ )
  public String canRead( @QueryParam( Constants.PARAM_PATH ) String path ) {
    return Boolean.toString( getCteEditor().canRead( path ) );
  }

  @GET @Path( Constants.ENDPOINT_EDITOR )
  public void edit( @QueryParam( Constants.PARAM_PATH ) String path, @Context HttpServletResponse servletResponse )
      throws WebApplicationException {

    if ( !getCteEditor().canRead( path ) ) {
      logger.info( "CteApi.edit(): not allowed to read " + path );
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }

    try {
      PluginIOUtils.writeOutAndFlush( servletResponse.getOutputStream(), getCteEditor().getEditor( path ) );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @GET @Path( Constants.ENDPOINT_GET_FILE )
  public void getFile( @QueryParam( Constants.PARAM_PATH ) String path, @Context HttpServletResponse servletResponse )
      throws WebApplicationException {

    if ( !getCteEditor().canRead( path ) ) {
      logger.info( "CteApi.getFile(): not allowed to read " + path );
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }

    try {
      PluginIOUtils.writeOutAndFlush( servletResponse.getOutputStream(), getCteEditor().getFile( path ) );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @POST @Path( Constants.ENDPOINT_SAVE_FILE )
  public String saveFile( @FormParam( Constants.PARAM_PATH ) String path,
      @FormParam( Constants.PARAM_DATA ) @DefaultValue( StringUtils.EMPTY ) String data ) throws WebApplicationException {

    if ( !getCteEditor().canEdit( path ) ) {
      logger.info( "CteApi.saveFile(): not allowed to edit " + path );
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }

    try {
      return String.valueOf( getCteEditor().saveFile( path,
              new ByteArrayInputStream( data.getBytes( getEngine().getEnvironment().getSystemEncoding() ) ) ) );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  @POST @Path( Constants.ENDPOINT_TREE_EXPLORE ) @Produces( MimeTypes.PLAIN_TEXT )
  public String tree( @FormParam( Constants.PARAM_DIR ) @DefaultValue( "/" ) String dir,
      @QueryParam( Constants.PARAM_FILE_EXTENSIONS ) @DefaultValue( StringUtils.EMPTY )
      String commaSeparatedAllowedExtensions,
      @QueryParam( Constants.PARAM_SHOW_HIDDEN_FILES ) @DefaultValue( "false" ) boolean showHiddenFiles )
      throws WebApplicationException {

    if ( !getCteEditor().canRead( dir ) ) {
      logger.info( "CteApi.tree(): not allowed to read " + dir );
      throw new WebApplicationException( Response.Status.FORBIDDEN );
    }

    String[] allowedExtensions = new String[] { };
    if ( !StringUtils.isEmpty( commaSeparatedAllowedExtensions ) ) {

      allowedExtensions =
          commaSeparatedAllowedExtensions.contains( "," ) ? commaSeparatedAllowedExtensions.toLowerCase().split( "," ) :
              new String[] { commaSeparatedAllowedExtensions.toLowerCase() };
    }

    try {

      return RepositoryHelper.toJQueryFileTree( dir,
          getCteEditor().getTree( dir, allowedExtensions, showHiddenFiles, SessionUtils.userInSessionIsAdmin() ) );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR );
    }
  }

  private ICteEditor getCteEditor() {
    return getEngine().getCteEditor();
  }

  private CteEngine getEngine() {
    return CteEngine.getInstance();
  }
}
