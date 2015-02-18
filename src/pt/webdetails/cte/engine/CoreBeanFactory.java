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
package pt.webdetails.cte.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.URL;

public class CoreBeanFactory {

  private static final Log logger = LogFactory.getLog( CoreBeanFactory.class );

  protected static ConfigurableApplicationContext context;

  private static final String SPRING_XML_SUFFIX = ".spring.xml";
  private static final String DEFAULT_SPRING_XML_NAME = "plugin" + SPRING_XML_SUFFIX;

  public CoreBeanFactory( String pluginName ) {
    context = getSpringBeanFactory( pluginName + SPRING_XML_SUFFIX ); // <plugin_name>.spring.xml
  }

  public Object getBean( String id ) {
    if ( context.containsBean( id ) ) {
      return context.getBean( id );
    }
    return null;
  }

  public boolean containsBean( String id ) {
    if ( context != null ) {
      return context.containsBean( id );
    }
    return false;
  }

  public String[] getBeanNamesForType( @SuppressWarnings( "rawtypes" ) Class clazz ) {
    return context.getBeanNamesForType( clazz );
  }

  protected ConfigurableApplicationContext getSpringBeanFactory( String pluginName ) {
    try {
      final ClassLoader cl = this.getClass().getClassLoader();

      // if we can't find a <plugin_name>.spring.xml, we'll default to 'plugin.spring.xml'
      String springBeanName = cl.getResource( pluginName ) != null ? pluginName : DEFAULT_SPRING_XML_NAME;

      URL url = cl.getResource( springBeanName );

      if ( url != null ) {
        logger.debug( "Found spring file @ " + url ); //$NON-NLS-1$
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext( springBeanName ) {
          @Override protected void initBeanDefinitionReader( XmlBeanDefinitionReader beanDefinitionReader ) {

            beanDefinitionReader.setBeanClassLoader( cl );
          }

          @Override protected void prepareBeanFactory( ConfigurableListableBeanFactory clBeanFactory ) {
            super.prepareBeanFactory( clBeanFactory );
            clBeanFactory.setBeanClassLoader( cl );
          }

          /**
           * Critically important to override this and return the desired CL
           **/
          @Override public ClassLoader getClassLoader() {
            return cl;
          }
        };
        return context;
      }
    } catch ( Exception e ) {
      logger.fatal( "Error loading " + pluginName, e );
    }
    logger.fatal(
        "Spring definition file does not exist. There should be a <plugin_name>.spring.xml file on the classpath " );
    return null;

  }

}
