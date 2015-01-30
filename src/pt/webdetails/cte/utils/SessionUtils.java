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
package pt.webdetails.cte.utils;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;

import java.util.List;

public class SessionUtils {

  public static boolean userInSessionIsAdmin() {
    return SecurityHelper.getInstance().isPentahoAdministrator( PentahoSessionHolder.getSession() );
  }

  public static String getUserInSession() {
    return PentahoSessionHolder.getSession().getName();
  }

  public static String[] getRolesForUserInSession() {
    return getRolesForUser( getUserInSession() );
  }

  public static boolean userExists( String user ) {

    if ( getUserRoleListService() != null ) {

      List<String> users = getUserRoleListService().getAllUsers();

      return users != null && users.contains( user );
    }
    return false;
  }

  public static boolean roleExists( String role ) {

    boolean roleExists = false;

    if ( getUserRoleListService() != null ) {

      List<String> roles = getUserRoleListService().getAllRoles();

      roleExists = roles != null && roles.contains( role );

      if ( !roleExists ) {

        roles = getUserRoleListService().getSystemRoles(); // last chance: check system ( hidden ) roles

        roleExists = roles != null && roles.contains( role );
      }
    }

    return roleExists;
  }

  public static String[] getRolesForUser( String user ) {

    if ( getUserRoleListService() != null ) {

      List<String> roles = getUserRoleListService().getRolesForUser( null, user );

      if ( roles != null ) {
        return roles.toArray( new String[] { } );
      }
    }
    return null;
  }

  public static String[] getUsersInRole( String role ) {

    if ( getUserRoleListService() != null ) {

      List<String> roles = getUserRoleListService().getUsersInRole( null, role );

      if ( roles != null ) {
        return roles.toArray( new String[] { } );
      }
    }
    return null;
  }

  protected static IUserRoleListService getUserRoleListService() {
    return PentahoSystem.get( IUserRoleListService.class );
  }
}
