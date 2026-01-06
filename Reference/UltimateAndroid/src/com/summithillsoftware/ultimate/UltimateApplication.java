package com.summithillsoftware.ultimate;

import java.io.File;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieSyncManager;

import com.summithillsoftware.ultimate.model.Team;
import com.summithillsoftware.ultimate.twitter.TweetQueue;
import com.summithillsoftware.ultimate.util.DirectoryZipper;
import com.summithillsoftware.ultimate.util.SoundPlayer;
import com.summithillsoftware.ultimate.util.UltimateLogger;
import com.summithillsoftware.ultimate.workflow.Workflow;

public class UltimateApplication extends Application {
	private static UltimateApplication Current;
	private static final String ULTIMATE_EXT_DRIVE_FOLDER = "ultimate";
	private static final String ULTIMATE_SUPPORT_ZIP_PREFIX = "ultimate-";
	private boolean isAppStartInProgress;
	private Workflow activeWorkflow;
	
	public static UltimateApplication current() {
		if (Current == null) {  // this is for unit testing (should have been populated by onCreate)
			Current = new UltimateApplication();
		}
		return Current;
	}

	public UltimateApplication() {
		super();
		isAppStartInProgress = true;
	}
	
	public final void onCreate() {
        super.onCreate(); 
        Current = this;
        CookieSyncManager.createInstance(this);
        TweetQueue.current();  // gets twitter looper initialized
        ensureOneTeam();
        SoundPlayer.current().loadSounds();
    }  
    
    private void ensureOneTeam() {
    	Team.current();
    }

	public boolean isAppStartInProgress() {
		return isAppStartInProgress;
	}

	public void setAppStartComplete() {
		this.isAppStartInProgress = false;
	}
	
	public boolean isLandscape() {
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	public Workflow getActiveWorkflow() {
		return activeWorkflow;
	}

	public void setActiveWorkflow(Workflow activeWorkflow) {
		this.activeWorkflow = activeWorkflow;
	}

	// returns true if the smallest width >= sw600dp
	public boolean isTablet() {
		int smallestScreenWidthDp = getResources().getConfiguration().smallestScreenWidthDp;
		//UltimateLogger.logInfo( "smallestScreenWidthDp = " + smallestScreenWidthDp);
		return smallestScreenWidthDp >= 600;
	}
	
	public File createZipForSupport(boolean includeTeamsAndGames) {
		File dirToZip = includeTeamsAndGames ? getFilesDir() : UltimateLogger.getLogsDir();
		try {
			String zipFileName = ULTIMATE_SUPPORT_ZIP_PREFIX + java.util.UUID.randomUUID().toString() + ".zip";
			File zipFile = new File(getAppExternalStorageFolder(), zipFileName);
			DirectoryZipper.zipDirectoryToFile(dirToZip, zipFile);
			return zipFile;
		} catch (Exception e) {
			UltimateLogger.logError("Could not create zip for support", e);
			return null;
		} 
	}
	
	public void runOnUiThread(Runnable runnable) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(runnable);
	}
	
	// returns the versionName from the manifest
	public String getAppVersion() {
		String version = "99.99.99";
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (Exception e) {
		    UltimateLogger.logError("Unable to retrieve app version", e);
		}
		return version;
	}
	
	private File getAppExternalStorageFolder() {
		File dir = new File(Environment.getExternalStorageDirectory(), ULTIMATE_EXT_DRIVE_FOLDER);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	@Override
    public void onTrimMemory(final int level) {
		super.onTrimMemory(level);
		
		// if we are called because we got moved to the background...
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
        	handleMovedToBackground();
        }
    }
	
	private void handleMovedToBackground() {
		UltimateLogger.logInfo("Ultimate App moved to background");
	}
}