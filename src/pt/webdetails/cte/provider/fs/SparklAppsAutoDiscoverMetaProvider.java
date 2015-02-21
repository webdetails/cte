package pt.webdetails.cte.provider.fs;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cte.api.ICteEnvironment;
import pt.webdetails.cte.api.ICteProvider;
import pt.webdetails.cte.engine.CteEngine;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Think of this class as a 'meta-provider', that discovers sparkl apps in
 * the pentaho-solutions/system and registers providers for each of them
 */
public class SparklAppsAutoDiscoverMetaProvider implements ICteProvider {

  // we store sparkl apps specific blacklists in cte's settings.xml, for ease of use
  private static final String SETTINGS_SPARKL_APPS_BLACKLISTED_FOLDERS = "sparkl-apps-blacklist/folders/path";
  private static final String SETTINGS_SPARKL_APPS_BLACKLISTED_EXTENSIONS = "sparkl-apps-blacklist/files/extension";

  private Logger logger = LoggerFactory.getLogger( SparklAppsAutoDiscoverMetaProvider.class );

  @Override public void init( ICteEnvironment environment ) throws InitializationException {

    String[] sparklAppBlacklistedFolders = getSparklAppBlacklistedFolders();
    String[] sparklAppBlacklistedFileExtensions = getSparklAppBlacklistedFileExtensions();

    for ( String pluginId : environment.getRegisteredPluginIds() ) {

      IReadAccess readAccess = environment.getOtherPluginSystemReader( pluginId, null );

      // unequivocal proof that this plugin *is* a sparkl app => contains cpk.spring.xml in base path
      if ( readAccess != null && readAccess.fileExists( "cpk.spring.xml" ) ) {

        logger.info( "Found a sparkl app :" + pluginId );

        if ( !getEngine().getProviderManager().providerExists( pluginId ) ) {

          GenericPluginFileSystemProvider sparklAppProvider = new GenericPluginFileSystemProvider( pluginId );
          sparklAppProvider.setBlacklistedFolders( sparklAppBlacklistedFolders );
          sparklAppProvider.setBlacklistedFileExtensions( sparklAppBlacklistedFileExtensions );
          sparklAppProvider.init( environment );

          injectProvider( sparklAppProvider );
        }
      }
    }
  }

  /*
   * We're adding a a provider to the providersList, WHILE this is being iterated in the ProviderManager;
   * this is known to cause java.util.ArrayList$ConcurrentModificationException.
   *
   * We can bypass this by delegating the "provider adding" logic to a thread that holds off for a couple of millis
   */
  private void injectProvider( final GenericPluginFileSystemProvider sparklAppProvider ) {

    Thread t = new Thread( new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep( 100 /* millis */ );
        } catch ( InterruptedException e ) {
          /* do nothing */
        }

        logger.info( "Adding a GenericPluginFileSystemProvider for sparkl app " + sparklAppProvider.getId() );
        getEngine().getProviderManager().addProvider( sparklAppProvider, false /* override if exists */);

      }
    });

    t.setDaemon(true); // critical: setting this thread as daemon

    t.start();
  }

  @Override public boolean isAccessible( IUserSession user ) {
    // this class is meant to run 'under-the-hood' only
    return false;
  }

  @Override public String getId() {
    return "sparkl-apps-meta-provider";
  }

  @Override public String getName() {
    return null;
  }

  @Override public String[] getBlacklistedFolders() {
    return new String[0];
  }

  @Override public String[] getBlacklistedFileExtensions() {
    return new String[0];
  }

  @Override public boolean canEdit( String path ) {
    return false;
  }

  @Override public boolean canRead( String path ) {
    return false;
  }

  @Override public InputStream getFile( String path ) throws Exception {
    return null;
  }

  @Override public boolean saveFile( String path, InputStream content ) throws Exception {
    return false;
  }

  @Override public IBasicFile[] getTree( String dir, String[] allowedExtensions, boolean showHiddenFiles )
      throws Exception {
    return new IBasicFile[0];
  }

  protected String[] getSparklAppBlacklistedFileExtensions() {

    List<String> blacklistedFileExtensions = new ArrayList<String>();

    // standard CTE blacklisted file extensions
    List<String> standardBlacklistedFileExtensions = getEngine().getSettings().getBlacklistedFileExtensions();

    if ( standardBlacklistedFileExtensions != null && standardBlacklistedFileExtensions.size() > 0 ) {
      blacklistedFileExtensions.addAll( standardBlacklistedFileExtensions );
    }

    // sparkl app specific blacklisted file extensions
    List<String> sparklAppBlacklistedFileExtensions =
        getElementListAsStringList( SETTINGS_SPARKL_APPS_BLACKLISTED_EXTENSIONS );

    if ( sparklAppBlacklistedFileExtensions != null && sparklAppBlacklistedFileExtensions.size() > 0 ) {
      blacklistedFileExtensions.addAll( sparklAppBlacklistedFileExtensions );
    }

    return blacklistedFileExtensions.toArray( new String[] { } );
  }

  protected String[] getSparklAppBlacklistedFolders() {

    List<String> blacklistedFolders = new ArrayList<String>();

    // standard CTE blacklisted folders
    List<String> standardBlacklistedFolders = getEngine().getSettings().getBlacklistedFolders();

    if ( standardBlacklistedFolders != null && standardBlacklistedFolders.size() > 0 ) {
      blacklistedFolders.addAll( standardBlacklistedFolders );
    }

    // sparkl app specific blacklisted folders
    List<String> sparklAppBlacklistedFolders = getElementListAsStringList( SETTINGS_SPARKL_APPS_BLACKLISTED_FOLDERS );

    if ( sparklAppBlacklistedFolders != null && sparklAppBlacklistedFolders.size() > 0 ) {
      blacklistedFolders.addAll( sparklAppBlacklistedFolders );
    }

    return blacklistedFolders.toArray( new String[] { } );
  }

  private List<String> getElementListAsStringList( String section ) {

    List<String> stringElements = new ArrayList<String>();

    List<Element> xmlElements = getEngine().getSettings().getSettingsXmlSection( section );

    if ( xmlElements != null ) {

      for ( Element xmlElement : xmlElements ) {

        String value = StringUtils.strip( xmlElement.getTextTrim() );

        if ( StringUtils.isEmpty( value ) ) {
          logger.error( "Invalid empty value. Skipping.." );
          continue;
        }

        stringElements.add( value );
      }
    }
    return stringElements;
  }

  private CteEngine getEngine() {
    return CteEngine.getInstance();
  }
}
