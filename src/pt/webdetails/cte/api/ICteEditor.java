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
package pt.webdetails.cte.api;

import pt.webdetails.cpf.repository.api.IBasicFile;

import java.io.InputStream;

public interface ICteEditor {

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
   * Blank editor ( no file )
   *
   * @return blank editor
   */
  InputStream getEditor() throws Exception;

  /**
   * return editor for the content of a file
   *
   * @param path - file path
   * @return editor for the content of a file
   */
  InputStream getEditor( String path ) throws Exception;

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
