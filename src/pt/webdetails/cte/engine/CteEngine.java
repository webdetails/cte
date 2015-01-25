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
package pt.webdetails.cte.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cte.Constants;
import pt.webdetails.cte.api.ICteEditor;

public class CteEngine {

  private static CteEngine instance;
  private Logger logger = LoggerFactory.getLogger( CteEngine.class );
  private ICteEditor cteEditor;

  private CteEngine() {

    CoreBeanFactory factory = new CoreBeanFactory( Constants.PLUGIN_NAME );

    this.cteEditor = (ICteEditor) factory.getBean( ICteEditor.class.getSimpleName() );

    if ( cteEditor == null ) {
      logger.error( "ICteEditor has not been set; CteEngine will not function properly" );
    }
  }

  public static CteEngine getInstance() {

    if ( instance == null ) {
      instance = new CteEngine();
    }

    return instance;
  }

  public ICteEditor getCteEditor() {
    return cteEditor;
  }

  protected void setCteEditor( ICteEditor cteEditor ) {
    this.cteEditor = cteEditor;
  }
}
