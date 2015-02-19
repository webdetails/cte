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

package pt.webdetails.cte.provider;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;

public class GenericBasicFileFilter implements IBasicFileFilter {

  /**
   * type of file filtering
   * <p/>
   * FILTER_IN - matches only ( act as a whitelist )
   * <p/>
   * FILTER_OUT - no matches only ( act as a blacklist )
   * <p/>
   */
  public static enum FilterType {
    FILTER_IN, FILTER_OUT
  }

  /**
   * type of file filtering; default is FILTER_IN
   */
  private FilterType filterType;

  private String fileName;
  private String[] fileExtensions;

  public GenericBasicFileFilter( String fileName, FilterType filterType ) {
    this( fileName, new String[] { }, filterType );
  }

  public GenericBasicFileFilter( String[] fileExtensions, FilterType filterType ) {
    this( null, fileExtensions, filterType );
  }

  public GenericBasicFileFilter( String fileName, String[] fileExtensions, FilterType filterType ) {
    this.fileName = fileName;
    this.fileExtensions = fileExtensions != null ? fileExtensions : new String[] { };
    this.filterType = filterType != null ? filterType : FilterType.FILTER_IN /* default */;
  }

  @Override public boolean accept( IBasicFile file ) {

    // first, the basic validations
    if ( file == null ) {
      return false; // this shouldn't even happen..
    } else if ( file.isDirectory() ) {
      return true; // we don't look at directories
    } else if ( StringUtils.isEmpty( file.getName() ) ) {
      return false; // this shouldn't even happen..
    }

    // now, onwards to the file validation

    boolean doNameFiltering = !StringUtils.isEmpty( fileName );
    boolean doExtensionFiltering = fileExtensions != null && fileExtensions.length > 0;

    boolean nameMatched = false;
    boolean extensionMatched = false;

    // file name is equal ?
    if ( doNameFiltering ) {

      String name = FilenameUtils.getBaseName( file.getName() );

      //ex: component.xml, sample.component.xml
      nameMatched = fileName.equalsIgnoreCase( name ) || ( !name.startsWith( "." ) && name.endsWith( "." + fileName ) );
    }

    if ( doExtensionFiltering ) {

      // is file extension one of the provided filter extensions ?
      for ( String extension : fileExtensions ) {
        if ( !StringUtils.isEmpty( extension ) ) {
          extensionMatched |= cleanDot( extension ).equalsIgnoreCase( cleanDot( file.getExtension() ) );
        }
      }
    }

    boolean acceptName = true;
    boolean acceptExtension = true;

    if ( doNameFiltering ) {
      acceptName = filterType == FilterType.FILTER_IN ? nameMatched : /* FILTER_OUT */ !nameMatched;
    }

    if ( doExtensionFiltering ) {
      acceptExtension = filterType == FilterType.FILTER_IN ? extensionMatched : /* FILTER_OUT */ !extensionMatched;
    }

    return acceptName && acceptExtension;
  }

  private static String cleanDot( String extension ) {
    return !StringUtils.isEmpty( extension ) && extension.startsWith( "." ) ? extension.substring( 1 ) : extension;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  public String[] getFileExtensions() {
    return fileExtensions;
  }

  public void setFileExtensions( String[] fileExtensions ) {
    this.fileExtensions = fileExtensions;
  }

  public FilterType getFilterType() {
    return filterType;
  }

  public void setFilterType( FilterType filterType ) {
    this.filterType = filterType;
  }
}
