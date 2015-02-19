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
import pt.webdetails.cte.api.ICteProvider;

import java.util.ArrayList;
import java.util.List;

public class CteProviderManager {

  private Logger logger = LoggerFactory.getLogger( CteProviderManager.class );

  List<ICteProvider> providers = new ArrayList<ICteProvider>();

  public CteProviderManager( List<ICteProvider> providers ) throws InitializationException {

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

  public List<ICteProvider> getProviders() {
    return providers;
  }

  public ICteProvider getProviderById( String id ) {

    if ( providerExists( id ) ) {

      for ( ICteProvider provider : providers ) {

        if ( id.equals( provider.getId() ) ) {
          return provider;
        }
      }
    }

    return null;
  }

  public void setProviders( List<ICteProvider> providers ) {
    this.providers = providers;
  }

  public boolean addProvider( ICteProvider provider , boolean override ) {

    if( provider == null || StringUtils.isEmpty( provider.getId() ) ){
      logger.error("Cannot add a provider with an empty ID");
      return false;

    } else if( providerExists( provider ) && !override ){
      logger.warn( "Provider already exists and override set to false" );
      return false;
    }

    getProviders().add( provider );
    return true;
  }

  public boolean providerExists( ICteProvider provider ){
    return provider != null && providerExists( provider.getId() );
  }

  public boolean providerExists( String id ){

    boolean matchFound = false;

    if( !StringUtils.isEmpty( id ) ) {

      for ( ICteProvider provider : getProviders() ) {
        matchFound |= id.equals( provider.getId() );
      }
    }

    return matchFound;
  }
}
