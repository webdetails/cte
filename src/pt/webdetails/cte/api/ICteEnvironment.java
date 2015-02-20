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

import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.session.IUserSession;

import java.util.Locale;

public interface ICteEnvironment {

  void init() throws InitializationException;

  void refresh();

  Locale getLocale();

  String getSystemEncoding();

  String getPluginId();

  String getApplicationBaseUrl();

  String getPluginBaseUrl();

  String getPluginRepositoryDir();

  String getPluginSystemDir();

  IUserContentAccess getUserContentAccess( String path );

  IReadAccess getPluginRepositoryReader( String path );

  IRWAccess getPluginRepositoryWriter( String path );

  IReadAccess getPluginSystemReader( String path );

  IReadAccess getOtherPluginSystemReader( String pluginId, String path );

  IRWAccess getPluginSystemWriter( String path );

  IRWAccess getOtherPluginSystemWriter( String pluginId, String path );

  IUrlProvider getUrlProvider();

  String[] getRegisteredPluginIds();

  IUserSession getUserSession();
}
