package com.ensoft.imgurviewer.view.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ensoft.imgurviewer.model.ImgurAlbum;
import com.ensoft.imgurviewer.model.ImgurImage;
import com.ensoft.imgurviewer.service.DeviceService;
import com.ensoft.imgurviewer.service.DownloadService;
import com.ensoft.imgurviewer.service.IntentUtils;
import com.ensoft.imgurviewer.service.resource.ImgurService;
import com.ensoft.imgurviewer.service.resource.ImgurAlbumService;
import com.ensoft.imgurviewer.service.resource.ImgurGalleryService;
import com.ensoft.imgurviewer.service.listener.ImgurAlbumResolverListener;
import com.ensoft.imgurviewer.service.listener.ImgurGalleryResolverListener;
import com.ensoft.imgurviewer.view.adapter.ImgurAlbumAdapter;
import com.ensoft.imgurviewer.view.helper.MetricsHelper;
import com.imgurviewer.R;

public class ImgurAlbumGalleryViewer extends AppActivity
{
	public static final String TAG = ImgurAlbumGalleryViewer.class.getCanonicalName();

	private LinearLayout floatingMenu;
	protected ImgurAlbumAdapter albumAdapter;
	protected ProgressBar progressBar;
	protected RecyclerView recyclerView;
	protected Uri albumData;
	protected ImgurImage[] images;

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		setContentView( R.layout.activity_albumviewer );

		floatingMenu = (LinearLayout)findViewById( R.id.floating_menu );
		floatingMenu.setPadding( 0, MetricsHelper.getStatusBarHeight( this ) + MetricsHelper.dpToPx( this, 8 ), 0, 0 );

		if ( null != getIntent().getExtras() && null != getIntent().getExtras().getString( ALBUM_DATA ) )
		{
			albumData = Uri.parse( getIntent().getExtras().getString( ALBUM_DATA ) );
		}
		else if ( getIntent().getData() != null )
		{
			albumData = getIntent().getData();
		}

		if ( null == albumData )
		{
			finish();

			Log.v( TAG, "Data not found." );

			return;
		}

		Log.v( TAG, "Data is: " + albumData.toString() );

		progressBar = (ProgressBar)findViewById( R.id.albumViewer_progressBar );

		if ( new ImgurAlbumService().isImgurAlbum( albumData ) )
		{
			new ImgurAlbumService().getAlbum( albumData, new ImgurAlbumResolverListener()
			{
				@Override
				public void onAlbumResolved( ImgurAlbum album )
				{
					create( album.getImages() );
				}

				@Override
				public void onError( String error )
				{
					Toast.makeText( ImgurAlbumGalleryViewer.this, error, Toast.LENGTH_SHORT ).show();
				}
			} );
		}
		else if ( new ImgurGalleryService().isImgurGallery( albumData ) )
		{
			new ImgurGalleryService().getGallery( albumData, new ImgurGalleryResolverListener()
			{
				@Override
				public void onAlbumResolved( ImgurAlbum album )
				{
					create( album.getImages() );
				}

				@Override
				public void onImageResolved( ImgurImage image )
				{
					ImgurImage[] images = new ImgurImage[1];
					images[0] = image;
					create( images );
				}

				@Override
				public void onError( String error )
				{
					Toast.makeText( ImgurAlbumGalleryViewer.this, error, Toast.LENGTH_SHORT ).show();
				}
			} );
		}
		else if ( new ImgurService().isMultiImageUri( albumData ) )
		{
			create( new ImgurService().getImagesFromMultiImageUri( albumData ) );
		}
	}

	protected void create( ImgurImage[] images )
	{
		this.images = images;
		progressBar.setVisibility( View.INVISIBLE );
		albumAdapter = new ImgurAlbumAdapter( R.layout.item_album_photo, images );
		albumAdapter.setOrientationLandscape( new DeviceService().isLandscapeOrientation( this ) );

		recyclerView = (RecyclerView) findViewById( R.id.albumViewer_listView );
		recyclerView.setLayoutManager( new LinearLayoutManager( this ) );
		recyclerView.setAdapter( albumAdapter );
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged( newConfig );

		if ( null == albumAdapter )
		{
			return;
		}

		if ( newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE )
		{
			albumAdapter.setOrientationLandscape( true );
			albumAdapter.notifyDataSetChanged();
		}
		else if ( newConfig.orientation == Configuration.ORIENTATION_PORTRAIT )
		{
			albumAdapter.setOrientationLandscape( false );
			albumAdapter.notifyDataSetChanged();
		}
	}

	public void showSettings( View v )
	{
		startActivity( new Intent( this, SettingsView.class ) );
	}

	public void downloadImage( View v )
	{
		if ( null != images )
		{
			for ( ImgurImage image : images )
			{
				new DownloadService( this ).download( image.getLinkUri(), URLUtil.guessFileName( image.getLink(), null, null ) );
			}
		}
	}

	public void shareImage( View v )
	{
		if ( albumData != null )
		{
			IntentUtils.shareMessage( this, getString( R.string.share ), albumData.toString(), getString( R.string.shareUsing ) );
		}
	}
}