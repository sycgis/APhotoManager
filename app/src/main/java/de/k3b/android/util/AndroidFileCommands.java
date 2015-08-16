package de.k3b.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import java.io.File;

import de.k3b.android.androFotoFinder.R;
import de.k3b.io.FileCommands;
import de.k3b.io.IDirectory;

/**
 * Api to manipulate files/photos.
 * Same as FileCommands with update media database.
 *
 * Created by k3b on 03.08.2015.
 */
public class AndroidFileCommands extends FileCommands {
    private static final String SETTINGS_KEY_LAST_COPY_TO_PATH = "last_copy_to_path";
    private Activity mContext;
    private AlertDialog mActiveAlert = null;

    public AndroidFileCommands() {
        // setLogFilePath(getDefaultLogFile());
        setContext(null);
    }

    public void closeAll() {
        closeLogFile();
        if (mActiveAlert != null) {
            mActiveAlert.dismiss();
            mActiveAlert = null;
        }
    }
    public String getDefaultLogFile() {
        Boolean isSDPresent = true; // Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        // since android 4.4 Evnvironment.getDataDirectory() and .getDownloadCacheDirectory ()
        // is protected by android-os :-(
        // app will not work on devices with no external storage (sdcard)
        final File rootDir = (isSDPresent) ? Environment.getExternalStorageDirectory() : Environment.getRootDirectory();
        final String zipfile = rootDir.getAbsolutePath() + "/" + mContext.getString(R.string.log_file_path);
        return zipfile;
    }

    /** called for each modified/deleted file */
    @Override
    protected void onPostProcess(String[] paths, int modifyCount, int itemCount, int opCode) {
        super.onPostProcess(paths, modifyCount, itemCount, opCode);

        if (opCode != OP_DELETE) {
            updateMediaDatabase(paths);
        }
    }

    public void updateMediaDatabase(String... pathNames) {
        SelectedFotos deletedItems = new SelectedFotos();
        MediaScannerConnection.scanFile(
                mContext,
                pathNames, // mPathNames.toArray(new String[mPathNames.size()]),
                null, null);
    }

    public boolean onOptionsItemSelected(final MenuItem item, final SelectedFotos selectedFileNames) {
        if ((selectedFileNames != null) && (selectedFileNames.size() > 0)) {
            // Handle item selection
            switch (item.getItemId()) {
                case R.id.cmd_delete:
                    return cmdDeleteFileWithQuestion(selectedFileNames);
            }
        }
        return false;
    }

    public void onMoveOrCopyDirectoryPick(boolean move, IDirectory destFolder, SelectedFotos srcFotos) {
        if (destFolder != null) {
            String copyToPath = destFolder.getAbsolute();
            setLastCopyToPath(copyToPath);
            File destDirFolder = new File(copyToPath);

            String[] selectedFileNames = srcFotos.getFileNames(mContext);
            Long[] ids = (move) ? srcFotos.getIds() : null;
            moveOrCopyFilesTo(move, destDirFolder, SelectedFotos.getFiles(selectedFileNames));

            if (move) {
                // remove from media database after successfull move
                File[] sourceFiles = SelectedFotos.getFiles(selectedFileNames);
                for (int i = 0; i < sourceFiles.length; i++) {
                    File sourceFile = sourceFiles[i];
                    if (!sourceFile.exists()) {
                        onMediaDeleted(sourceFile.getAbsolutePath(), ids[i]);
                    }
                }
            }
        }
    }

    @NonNull
    public String getLastCopyToPath() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getString(SETTINGS_KEY_LAST_COPY_TO_PATH, "/");
    }

    private void setLastCopyToPath(String copyToPath) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putString(SETTINGS_KEY_LAST_COPY_TO_PATH, copyToPath);
        edit.commit();
    }



    public boolean cmdDeleteFileWithQuestion(final SelectedFotos fotos) {
        String[] pathNames = fotos.getFileNames(mContext);
        StringBuffer names = new StringBuffer();
        for (String name : pathNames) {
            names.append(name).append("\n");
        }
        final String message = mContext
                .getString(R.string.format_confirm_delete, names.toString());

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final String title = mContext.getText(R.string.title_confirm_removal)
                .toString();

        builder.setTitle(title + pathNames.length);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    final DialogInterface dialog,
                                    final int id) {
                                mActiveAlert = null; deleteFiles(fotos);
                            }
                        }
                )
                .setNegativeButton(R.string.btn_no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    final DialogInterface dialog,
                                    final int id) {
                                mActiveAlert = null; dialog.cancel();
                            }
                        }
                );

        final AlertDialog alert = builder.create();
        mActiveAlert = alert;
        alert.show();
        return true;
    }

    private int deleteFiles(SelectedFotos fotos) {
        int result = 0;
        String[] fileNames = fotos.getFileNames(mContext);
        openLogfile();
        File[] toBeDeleted = SelectedFotos.getFiles(fileNames);
        Long[] ids = fotos.getIds();

        for (int i = 0; i < ids.length; i++) {
            File file = toBeDeleted[i];
            if (deleteFileWitSidecar(file)) {
                onMediaDeleted(file.getAbsolutePath(), ids[i]);
                result++;
            }
        }

        closeLogFile();
        onPostProcess(fileNames, result, ids.length, OP_DELETE);

        return result;
    }

    private void onMediaDeleted(String absolutePath, Long id) {
        Uri uri = SelectedFotos.getUri(id);
        mContext.getContentResolver().delete(uri, null, null);
        log("rem deleted '" + absolutePath +
                "' as content: ", uri.toString());
    }

    public AndroidFileCommands setContext(Activity mContext) {
        this.mContext = mContext;
        if (mContext != null) {
            closeLogFile();
        }
        return this;
    }

    public static AndroidFileCommands log(Activity context, String... params) {
        AndroidFileCommands cmd = new AndroidFileCommands().setContext(context);
        cmd.setLogFilePath(cmd.getDefaultLogFile());
        cmd.openLogfile();
        cmd.log(params);
        return cmd;
    }
}