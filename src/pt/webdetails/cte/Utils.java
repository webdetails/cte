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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import pt.webdetails.cpf.Util;
import pt.webdetails.cte.engine.CteEngine;

public class Utils {

  public static String[] getSettingsXmlBlacklistedFolders() {
    // use settings.xml blacklisted folders
    return CteEngine.getInstance().getSettings().getBlacklistedFolders().toArray( new String[] { } );
  }

  public static String[] getSettingsXmlBlacklistedFileExtensions() {
    // use settings.xml blacklisted file extensions
    return CteEngine.getInstance().getSettings().getBlacklistedFileExtensions().toArray( new String[] { } );
  }

  public static boolean isInBlacklistedFolder( String path ) {
    return isInBlacklistedFolder( path, getSettingsXmlBlacklistedFolders() );
  }

  public static boolean isInBlacklistedFolder( String path, String[] blacklistedFolders ) {

    if ( StringUtils.isEmpty( path ) || blacklistedFolders == null || blacklistedFolders.length == 0 ) {
      return false; // no way of checking
    }

    boolean isInBlacklistedFolder = false;

    if ( !path.startsWith( Util.SEPARATOR ) ) {
      path = Util.SEPARATOR + path;
    }

    for ( String blacklistedFolder : blacklistedFolders ) {
      isInBlacklistedFolder |= path.startsWith( blacklistedFolder );
    }

    return isInBlacklistedFolder;
  }

  public static boolean isABlacklistedFileExtension( String path ) {
    return isABlacklistedFileExtension( path, getSettingsXmlBlacklistedFileExtensions() );
  }

  public static boolean isABlacklistedFileExtension( String path, String[] blacklistedFileExtensions ) {

    String fileExtension = FilenameUtils.getExtension( path );

    if ( StringUtils.isEmpty( fileExtension ) || blacklistedFileExtensions == null
        || blacklistedFileExtensions.length == 0 ) {
      return false; // no way of checking
    }

    boolean isABlacklistedFileExtension = false;

    for ( String blacklistedFileExtension : blacklistedFileExtensions ) {
      isABlacklistedFileExtension |= fileExtension.equalsIgnoreCase( blacklistedFileExtension );
    }

    return isABlacklistedFileExtension;
  }
}
