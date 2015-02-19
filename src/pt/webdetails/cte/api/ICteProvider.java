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
package pt.webdetails.cte.api;

import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IBasicFile;

import java.io.InputStream;

public interface ICteProvider {

  void init( ICteEnvironment environment ) throws InitializationException;

  /**
   * Unique id for this provider
   * @return unique id
   */
  String getId();

  /**
   * A small readable name for this provider (ex: "Repository", "Mondrian Files", "Filesystem",.. )
   * @return description
   */
  String getName();

  /**
   * array of folders paths this ICteProvider needs to filter-out
   * @return array of folders paths
   */
  public String[] getBlacklistedFolders();

  /**
   * array of file extensions this ICteProvider needs to filter-out
   * @return array of file extensions
   */
  public String[] getBlacklistedFileExtensions();

  /**
   * Centralized method for all security and file path validations
   *
   * @param path - path to file
   * @return boolean - true if can edit, false otherwise
   */
  boolean canEdit( String path );

  /**
   * Checks to see if a given file is accessible
   *
   * @param path - path to file
   * @return boolean - true if is accessible, false otherwise
   */
  boolean canRead( String path );

  /**
   * returns the content of a file
   *
   * @param path - file path
   * @return the content of a file
   */
  InputStream getFile( String path ) throws Exception;

  /**
   * saves the content onto the file path
   *
   * @param path - file path
   * @param content - file content
   * @return true is properly saved, false otherwise
   */
  boolean saveFile( String path, InputStream content ) throws Exception;

  /**
   * returns a file tree below the provided dir
   *
   * @param dir - dir path
   * @param allowedExtensions - list of allowed file extensions under the tree
   * @param showHiddenFiles - if true then hidden files will be included
   * @param userIsAdmin - true if user is administrator
   * @return IBasicFile[] - tree under the given dir path
   */
  IBasicFile[] getTree( String dir, String[] allowedExtensions, boolean showHiddenFiles, boolean userIsAdmin ) throws Exception;

}
