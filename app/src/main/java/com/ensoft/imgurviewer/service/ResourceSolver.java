package com.ensoft.imgurviewer.service;

import android.net.Uri;

import com.ensoft.imgurviewer.service.listener.ResourceLoadListener;
import com.ensoft.imgurviewer.service.resource.FlickrService;
import com.ensoft.imgurviewer.service.resource.GfycatService;
import com.ensoft.imgurviewer.service.resource.GiphyService;
import com.ensoft.imgurviewer.service.resource.GyazoService;
import com.ensoft.imgurviewer.service.resource.ImageServiceSolver;
import com.ensoft.imgurviewer.service.resource.ImgurService;
import com.ensoft.imgurviewer.service.resource.InstagramService;
import com.ensoft.imgurviewer.service.resource.RedditUploadsService;
import com.ensoft.imgurviewer.service.resource.RedditVideoService;
import com.ensoft.imgurviewer.service.resource.ResourceServiceSolver;
import com.ensoft.imgurviewer.service.resource.StreamableService;
import com.ensoft.imgurviewer.service.resource.TwitchClipsService;
import com.ensoft.imgurviewer.service.resource.VidmeService;
import com.ensoft.imgurviewer.view.activity.ImgurAlbumGalleryViewer;

import java.util.ArrayList;

public class ResourceSolver
{
	private ResourceLoadListener resourceLoadListener;
	private ArrayList<ResourceServiceSolver> resourceServiceSolvers = new ArrayList<>();

	public ResourceSolver( ResourceLoadListener resourceLoadListener )
	{
		this.resourceLoadListener = resourceLoadListener;

		loadServices();
	}

	protected void loadServices()
	{
		resourceServiceSolvers.add( new ResourceServiceSolver( new ImgurService(), resourceLoadListener, ImgurAlbumGalleryViewer.class ) );
		resourceServiceSolvers.add( new ResourceServiceSolver( new GyazoService(), resourceLoadListener, null ) );
		resourceServiceSolvers.add( new ResourceServiceSolver( new GfycatService(), resourceLoadListener, null ) );
		resourceServiceSolvers.add( new ResourceServiceSolver( new RedditUploadsService(), resourceLoadListener, null ) );
		resourceServiceSolvers.add( new ResourceServiceSolver( new StreamableService(), resourceLoadListener, null ) );
		resourceServiceSolvers.add( new ResourceServiceSolver( new TwitchClipsService(), resourceLoadListener, null ) );
		resourceServiceSolvers.add( new ResourceServiceSolver( new InstagramService(), resourceLoadListener, ImgurAlbumGalleryViewer.class ) );
		resourceServiceSolvers.add( new ResourceServiceSolver( new VidmeService(), resourceLoadListener, null ) );
		resourceServiceSolvers.add( new ResourceServiceSolver( new FlickrService(), resourceLoadListener, null ) );
		resourceServiceSolvers.add( new ResourceServiceSolver( new GiphyService(), resourceLoadListener, null ) );
		resourceServiceSolvers.add( new ResourceServiceSolver( new RedditVideoService(), resourceLoadListener, null ) );
	}

	public void solve( Uri uri )
	{
		for ( ResourceServiceSolver resourceServiceSolver : resourceServiceSolvers )
		{
			if ( resourceServiceSolver.solve( uri ) )
			{
				return;
			}
		}

		if ( ImageServiceSolver.isVideoUrl( uri ) )
		{
			resourceLoadListener.loadVideo( uri );
		}
		else
		{
			resourceLoadListener.loadImage( uri, null );
		}
	}
}
