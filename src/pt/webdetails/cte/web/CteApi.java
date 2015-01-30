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
import pt.webdetails.cpf.utils.PluginIOUtils;
import pt.webdetails.cte.Constants;
import pt.webdetails.cte.api.ICteEditor;
import pt.webdetails.cte.engine.CteEngine;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;

@Path( "/cte/api/" ) public class CteApi {

  private Logger logger = LoggerFactory.getLogger( CteApi.class );

  @GET
  @Path( Constants.ENDPOINT_CAN_EDIT )
  @Produces( { MediaType.WILDCARD } )
  public String canEdit( @QueryParam( Constants.PARAM_PATH ) String path ) throws Exception {
    return Boolean.toString( isFileEditAllowed( path ) );
  }

  @GET
  @Path( Constants.ENDPOINT_CAN_READ )
  @Produces( { MediaType.WILDCARD } )
  public String canRead( @QueryParam( Constants.PARAM_PATH ) String path ) throws Exception {
    return Boolean.toString( isFileReadAllowed( path ) );
  }

  @GET
  @Path( Constants.ENDPOINT_BLANK_EDITOR )
  @Produces( { MediaType.WILDCARD } )
  public void blank( @Context HttpServletResponse servletResponse ) throws Exception {

    try {
      PluginIOUtils.writeOutAndFlush( servletResponse.getOutputStream(), getCteEditor().getEditor() );

    } catch ( Exception e ) {
      logger.error( e.getMessage(), e );
      throw e;
    }
  }

  @GET
  @Path( Constants.ENDPOINT_EDITOR )
  @Produces( { MediaType.WILDCARD } )
  public void edit( @QueryParam( Constants.PARAM_PATH ) String path,
      @Context HttpServletResponse servletResponse ) throws Exception {

    if ( isFileReadAllowed( path ) ) {

      try {
        PluginIOUtils.writeOutAndFlush( servletResponse.getOutputStream(), getCteEditor().getEditor( path ) );

      } catch ( Exception e ) {
        logger.error( e.getMessage(), e );
        throw e;
      }
    }
  }

  @GET
  @Path( Constants.ENDPOINT_GET_FILE )
  @Produces( { MediaType.WILDCARD } )
  public void getFile( @QueryParam( Constants.PARAM_PATH ) String path,
      @Context HttpServletResponse servletResponse ) throws Exception {

    if ( isFileReadAllowed( path ) ) {

      try {
        PluginIOUtils.writeOutAndFlush( servletResponse.getOutputStream(), getCteEditor().getFile( path ) );

      } catch ( Exception e ) {
        logger.error( e.getMessage(), e );
        throw e;
      }
    }
  }

  @POST
  @Path( Constants.ENDPOINT_SAVE_FILE )
  @Produces( { MediaType.WILDCARD } )
  public String saveFile( @FormParam( Constants.PARAM_PATH ) String path,
      @FormParam( Constants.PARAM_DATA ) @DefaultValue( StringUtils.EMPTY ) String data ) throws Exception {

    boolean success = false;

    if ( isFileEditAllowed( path ) ) {

      try {
        success = getCteEditor().saveFile( path,
            new ByteArrayInputStream( data.getBytes( getEngine().getEnvironment().getSystemEncoding() ) ) );

      } catch ( Exception e ) {
        logger.error( e.getMessage(), e );
        throw e;
      }
    }

    return String.valueOf( success );
  }

  private ICteEditor getCteEditor() {
    return getEngine().getCteEditor();
  }

  private CteEngine getEngine() {
    return CteEngine.getInstance();
  }

  /**
   * Centralized method for all security and file path validations
   *
   * @param path - path to file
   * @return boolean - true if can edit, false otherwise
   */
  private boolean isFileReadAllowed( String path ) {

    // TODO implement HTTP 403 FORBIDDEN

    boolean canRead = false;

    if ( !StringUtils.isEmpty( path ) ) {

      try {

        canRead = getCteEditor().canRead( path );

        if ( !canRead ) {
          logger.error( "CteApi.isFileReadAllowed(): not allowed to edit file at " + path );
        }

      } catch ( Exception e ) {
        logger.error( e.getMessage(), e );
      }
    } else {
      logger.error( "CteApi.isFileReadAllowed(): file path is null" );
    }

    return canRead;
  }

  /**
   * Centralized method for all security and file path validations
   *
   * @param path - path to file
   * @return boolean - true if can edit, false otherwise
   */
  private boolean isFileEditAllowed( String path ) {

    // TODO implement HTTP 403 FORBIDDEN

    boolean canEdit = false;

    if ( !StringUtils.isEmpty( path ) ) {

      try {

        canEdit = getCteEditor().canEdit( path );

        if ( !canEdit ) {
          logger.error( "CteApi.isFileEditAllowed(): not allowed to edit file at " + path );
        }

      } catch ( Exception e ) {
        logger.error( e.getMessage(), e );
      }
    } else {
      logger.error( "CteApi.isFileEditAllowed(): file path is null" );
    }

    return canEdit;
  }
}
