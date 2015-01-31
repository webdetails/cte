/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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
package pt.webdetails.cte.editor.ace;

import pt.webdetails.cpf.repository.api.IBasicFile;

/**
 * this class extends on the existing GenericBasicFileFilter calculations, adding on top of it some basic directory
 * filtering
 */
public class AceEditorFilter extends GenericBasicFileFilter {

  /**
   * directories to be filtered
   */
  private String[] directories;

  public AceEditorFilter( String fileName, String[] fileExtensions, String[] directories, FilterType filterType ) {
    super( fileName, fileExtensions, filterType );
    setDirectories( directories != null ? directories : new String[]{} );
  }

  @Override
  public boolean accept( IBasicFile file ) {

    // superclass outcome
    boolean acceptFile = super.accept( file );

    boolean doDirectoryFiltering = directories != null && directories.length > 0;

    if ( acceptFile && doDirectoryFiltering && file.isDirectory() ) {

      boolean directoryMatched = false;

      for ( String directory : directories ) {
        // check if the file's path starts with the provided directory
        directoryMatched |= file.getFullPath().startsWith( directory );
      }

      acceptFile = getFilterType() == FilterType.FILTER_IN ?
          directoryMatched : /* FilterType.FILTER_OUT */ !directoryMatched;
    }

    return acceptFile;
  }

  public String[] getDirectories() {
    return directories;
  }

  public void setDirectories( String[] directories ) {
    this.directories = directories;
  }

}
