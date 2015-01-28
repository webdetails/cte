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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.messaging.JsonResult;
import pt.webdetails.cpf.utils.JsonHelper;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpf.utils.PluginIOUtils;
import pt.webdetails.cte.Constants;
import pt.webdetails.cte.api.ICteEditor;
import pt.webdetails.cte.engine.CteEngine;

import javax.activation.MimeType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;

@Path( "/cte/api" ) public class CteApi {

  private Logger logger = LoggerFactory.getLogger( CteApi.class );

  @GET
  @Path( "/canEdit" )
  @Produces( { MediaType.WILDCARD  } )
  public String canEdit( @QueryParam( Constants.PARAM_PATH ) String path, @Context HttpServletRequest servletRequest,
      @Context HttpServletResponse servletResponse ) throws Exception {

    boolean canEdit = false;

    if ( !StringUtils.isEmpty( path ) ) {

      try {

        canEdit = getCteEditor().canEdit( path );

      } catch ( Exception e ) {
        logger.error( e.getMessage(), e );
        throw e;
      }
    } else {
      logger.error( "CteApi.canEdit(): file path is null" );
    }

    return Boolean.toString( canEdit );
  }


  @GET
  @Path( "/edit" )
  @Produces( { MediaType.WILDCARD } )
  public void edit( @QueryParam( Constants.PARAM_PATH ) String path, @Context HttpServletRequest servletRequest,
      @Context HttpServletResponse servletResponse ) throws Exception {

    if ( !StringUtils.isEmpty( path ) ) {

      try {
        PluginIOUtils.writeOutAndFlush( servletResponse.getOutputStream(), getCteEditor().getEditor( path ) );

      } catch ( Exception e ) {
        logger.error( e.getMessage(), e );
        throw e;
      }
    } else {
      logger.error( "CteApi.edit(): file path is null" );
    }
  }

  @GET
  @Path( "/getFile" )
  @Produces( { MediaType.WILDCARD } )
  public void getFile( @QueryParam( Constants.PARAM_PATH ) String path, @Context HttpServletRequest servletRequest,
      @Context HttpServletResponse servletResponse ) throws Exception {

    if( !getCteEditor().canEdit( path ) ){
      logger.error( "CteApi.getFile(): not allowed to edit file at " + path );
      // TODO send back HTTP 403 FORBIDDEN
      String response = "Not allowed";
      PluginIOUtils.writeOutAndFlush( servletResponse.getOutputStream(), new ByteArrayInputStream( response.getBytes(
          CteEngine.getInstance().getEnvironment().getSystemEncoding() ) ) );
      return;
    }

    if ( !StringUtils.isEmpty( path ) ) {

      try {
        PluginIOUtils.writeOutAndFlush( servletResponse.getOutputStream(), getCteEditor().getFile( path ) );

      } catch ( Exception e ) {
        logger.error( e.getMessage(), e );
        throw e;
      }

    } else {
      logger.error( "CteApi.getFile(): file path is null" );
    }
  }

  private ICteEditor getCteEditor() {
    return getEngine().getCteEditor();
  }

  private CteEngine getEngine() {
    return CteEngine.getInstance();
  }
}
