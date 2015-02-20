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
package pt.webdetails.cte.provider.fs;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.repository.api.IRWAccess;

import java.util.ArrayList;
import java.util.List;

public class SparklAppsSettings extends PluginSettings {

  // we store sparkl apps specific blacklists in cte's settings.xml, for ease of use
  private static final String SETTINGS_SPARKL_APPS_BLACKLISTED_FOLDERS = "sparkl-apps-blacklist/folders/path";
  private static final String SETTINGS_SPARKL_APPS_BLACKLISTED_EXTENSIONS = "sparkl-apps-blacklist/files/extension";

  List<String> blacklistedFolders;
  List<String> blacklistedFileExtensions;

  public SparklAppsSettings( IRWAccess writeAccess ) {
    super( writeAccess );
  }

  public void init() {
    setBlacklistedFolders( initBlacklistedFolders() );
    setBlacklistedFileExtensions( initBlacklistedFileExtensions() );
  }

  public List<String> getBlacklistedFolders() {
    return blacklistedFolders;
  }

  public void setBlacklistedFolders( List<String> blacklistedFolders ) {
    this.blacklistedFolders = blacklistedFolders;
  }

  public List<String> getBlacklistedFileExtensions() {
    return blacklistedFileExtensions;
  }

  public void setBlacklistedFileExtensions( List<String> blacklistedFileExtension ) {
    this.blacklistedFileExtensions = blacklistedFileExtension;
  }

  protected List<String> initBlacklistedFileExtensions() {

    List<String> blacklistedFileExtensions = new ArrayList<String>();
    List<Element> xmlElements = getSettingsXmlSection( SETTINGS_SPARKL_APPS_BLACKLISTED_EXTENSIONS );

    if ( xmlElements != null ) {

      for ( Element xmlElement : xmlElements ) {

        String value = StringUtils.strip( xmlElement.getTextTrim() );

        if ( StringUtils.isEmpty( value ) ) {
          logger.error( "Invalid empty file extension. Skipping.." );
          continue;
        }

        blacklistedFileExtensions.add( value );
      }
    }

    return blacklistedFileExtensions;
  }

  protected List<String> initBlacklistedFolders() {

    List<String> blacklistedFolders = new ArrayList<String>();
    List<Element> xmlElements = getSettingsXmlSection( SETTINGS_SPARKL_APPS_BLACKLISTED_FOLDERS );

    if ( xmlElements != null ) {

      for ( Element xmlElement : xmlElements ) {

        String value = StringUtils.strip( xmlElement.getTextTrim() );

        if ( StringUtils.isEmpty( value ) ) {
          logger.error( "Invalid empty path. Skipping.." );
          continue;
        }

        blacklistedFolders.add( value );
      }
    }

    return blacklistedFolders;
  }
}
