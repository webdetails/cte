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
