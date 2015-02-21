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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.api.ICteProvider;
import pt.webdetails.cte.api.ICteProviderManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CteDefaultProviderManager implements ICteProviderManager {

  private Logger logger = LoggerFactory.getLogger( CteDefaultProviderManager.class );

  List<ICteProvider> providers = new ArrayList<ICteProvider>();
  List<String> blacklistedPlugins = new ArrayList<String>();

  public CteDefaultProviderManager( List<ICteProvider> providers ) throws InitializationException {

    if ( providers == null || providers.size() == 0 ) {
      throw new InitializationException( "Need to declare at least one ICteProvider", null );
    }

    // validate all provider id's are unique

    List<String> ids = new ArrayList<String>();

    for ( ICteProvider provider : providers ) {

      if ( provider == null || StringUtils.isEmpty( provider.getId() ) ) {
        throw new InitializationException( "ICteProvider " + provider.getClass().getSimpleName() + " with null ID. "
            + "Each provider must have a unique ID", null );
      }

      if ( ids.contains( provider.getId() ) ) {
        throw new InitializationException( "Duplicated ICteProvider id: " + provider.getId() +
            ". Each provider must have a unique ID", null );

      } else {
        ids.add( provider.getId() );
      }
    }

    // all validations passed
    this.providers = providers;
  }

  @Override public void init( ICteEnvironment environment, String[] blacklistedPlugins )
      throws InitializationException {

    if( blacklistedPlugins != null && blacklistedPlugins.length > 0 ){
      setBlacklistedPlugins( Arrays.asList( blacklistedPlugins ) );
    }

    // this will aid us in keeping track of those blacklisted providers that must be discarded
    List<String> blacklistedProviderIds = new ArrayList<String>();

    for ( ICteProvider provider : getProviders() ) {

      if ( getBlacklistedPlugins().contains( provider.getId() ) ) {
        logger.error( "Provider ID is blacklisted: " + provider.getId() + ". Discarding it.." );
        blacklistedProviderIds.add( provider.getId() );
      }
    }


    // rebuild providers list, discarding all blacklistedProviderIds
    if( blacklistedProviderIds.size() > 0 ){

      List<ICteProvider> allowedProviders = new ArrayList<ICteProvider>();

      for( ICteProvider provider : getProviders() ){

        if( !blacklistedProviderIds.contains( provider.getId() ) ){
          allowedProviders.add( provider );
        }
      }

      setProviders( allowedProviders );
    }

    initProviders( environment );
  }

  private void initProviders( ICteEnvironment environment ) throws InitializationException  {

    // init all providers registered in provider Manager
    for( ICteProvider provider : getProviders() ){
      provider.init( environment );
    }
  }

  @Override public List<ICteProvider> getProviders() {
    return providers;
  }

  @Override public ICteProvider getProviderById( String id ) {

    if ( providerExists( id ) ) {

      for ( ICteProvider provider : providers ) {

        if ( id.equals( provider.getId() ) ) {
          return provider;
        }
      }
    }

    return null;
  }

  protected void setProviders( List<ICteProvider> providers ) {
    this.providers = providers;
  }

  @Override public boolean addProvider( ICteProvider provider, boolean override ) {

    if ( provider == null || StringUtils.isEmpty( provider.getId() ) ) {
      logger.error( "Cannot add a provider with an empty ID" );
      return false;

    } else if ( providerExists( provider ) && !override ) {
      logger.warn( "Provider already exists and override set to false: " + provider.getId() );
      return false;

    } else if( getBlacklistedPlugins().contains( provider.getId() ) ){
      logger.error( "Provider ID is blacklisted: " + provider.getId() + ". Discarding it..");
      return false;
    }

    getProviders().add( provider );
    return true;
  }

  public boolean providerExists( ICteProvider provider ) {
    return provider != null && providerExists( provider.getId() );
  }

  @Override public boolean providerExists( String id ) {

    boolean matchFound = false;

    if ( !StringUtils.isEmpty( id ) ) {

      for ( ICteProvider provider : getProviders() ) {
        matchFound |= id.equals( provider.getId() );
      }
    }

    return matchFound;
  }

  public List<String> getBlacklistedPlugins() {
    return blacklistedPlugins;
  }

  protected void setBlacklistedPlugins( List<String> blacklistedPlugins ) {
    this.blacklistedPlugins = blacklistedPlugins;
  }
}
