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
package pt.webdetails.cte.editor.ace;

import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cte.api.ICteEditor;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.engine.CteEngine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class AceEditor implements ICteEditor {

  private String extEditor;

  public AceEditor() {
  }

  private String getExtEditor() throws Exception {

    // sanitized calls; output is always the same ( ext-editor.html )

    if ( extEditor == null ) {
      extEditor = new ExtEditor( getEnvironment().getUrlProvider(), PluginEnvironment.repository() ).getExtEditor();
    }

    return extEditor;
  }

  @Override public InputStream getEditor() throws Exception {
    return getEditor( null );
  }

  @Override
  public InputStream getEditor( InputStream fileContent ) throws Exception {
    return new ByteArrayInputStream( getExtEditor().getBytes( getEnvironment().getSystemEncoding() ) );
  }

  private ICteEnvironment getEnvironment() {
    return CteEngine.getInstance().getEnvironment();
  }
}

