package com.eucalyptus.webui.client.activity;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eucalyptus.webui.client.ClientFactory;
import com.eucalyptus.webui.client.place.ConfigPlace;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.view.DetailView;
import com.eucalyptus.webui.client.view.HasValueWidget;
import com.eucalyptus.webui.client.view.ConfigView;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ConfigActivity extends AbstractSearchResultActivity implements ConfigView.Presenter, DetailView.Presenter {
  
  public static final String TITLE = "SYSTEM CONFIGURATIONS";
  
  private static final Logger LOG = Logger.getLogger( ConfigActivity.class.getName( ) );

  protected SearchResultRow currentSelected = null;

  public ConfigActivity( ConfigPlace place, ClientFactory clientFactory ) {
    super( place, clientFactory );
  }
  
  @Override
  protected void showView( SearchResult result ) {
    if ( this.view == null ) {
      this.view = this.clientFactory.getConfigView( );
      ( ( ConfigView ) this.view ).setPresenter( this );
      container.setWidget( this.view );
      ( ( ConfigView ) this.view ).clear( );
    }
    ( ( ConfigView ) this.view ).showSearchResult( result );
  }

  @Override
  protected void doSearch( String query, SearchRange range ) {
    LOG.log( Level.INFO, "'service' new search: " + query );
    this.clientFactory.getBackendService( ).lookupConfiguration( this.clientFactory.getLocalSession( ).getSession( ), query, range, new AsyncCallback<SearchResult>( ) {

      @Override
      public void onFailure( Throwable cause ) {
        LOG.log( Level.WARNING, "Failed to get configurations: " + cause );
        displayData( null );
      }

      @Override
      public void onSuccess( SearchResult result ) {
        displayData( result );
      }
      
    } );
  }

  @Override
  public void onSelectionChange( SearchResultRow selection ) {
    this.currentSelected = selection;
    if ( selection == null ) {
      LOG.log( Level.INFO, "Selection changed to null" );      
      this.clientFactory.getShellView( ).hideDetail( );
    } else {
      LOG.log( Level.INFO, "Selection changed to " + selection );
      this.clientFactory.getShellView( ).showDetail( DETAIL_PANE_SIZE );
      showSelectedDetails( );
    }
  }

  private void showSelectedDetails( ) {
    ArrayList<SearchResultFieldDesc> descs = new ArrayList<SearchResultFieldDesc>( );
    descs.addAll( cache.getDescs( ) );
    descs.addAll( currentSelected.getExtraFieldDescs( ) );
    this.clientFactory.getShellView( ).getDetailView( ).showData( descs, currentSelected.getRow( ) );          
  }

  @Override
  public void saveValue( ArrayList<HasValueWidget> values ) {
    if ( values == null || values.size( ) < 1 || this.currentSelected == null ) {
      LOG.log( Level.WARNING, "No valid values or empty selection" );
    }
    LOG.log( Level.INFO, "Saving: " + values );
    SearchResultRow result = new SearchResultRow( );
    result.setExtraFieldDescs( this.currentSelected.getExtraFieldDescs( ) );
    for ( int i = 0; i < values.size( ); i++ ) {
      result.addField( values.get( i ).getValue( ) );
    }
    this.clientFactory.getBackendService( ).setConfiguration( this.clientFactory.getLocalSession( ).getSession( ), result, new AsyncCallback<Void>( ) {

      @Override
      public void onFailure( Throwable cause ) {
        LOG.log( Level.WARNING, "Failed to set configuration.", cause );
      }

      @Override
      public void onSuccess( Void arg0 ) {
        clientFactory.getShellView( ).getDetailView( ).disableSave( );
        reloadCurrentRange( );
      }
      
    } );
  }

  @Override
  protected String getTitle( ) {
    return TITLE;
  }

}
