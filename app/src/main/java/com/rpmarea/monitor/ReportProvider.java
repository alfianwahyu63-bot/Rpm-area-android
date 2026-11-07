package com.rpmarea.monitor;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;

/** Read-only temporary PNG for sharing via ACTION_SEND. No public storage permissions. */
public class ReportProvider extends ContentProvider {
    public static final String SHARE_FILE="rpm_share_image.png";
    @Override public boolean onCreate(){return true;}
    private File verify(Uri uri) throws FileNotFoundException {
        if(uri == null || !("/share/"+SHARE_FILE).equals(uri.getPath()))throw new FileNotFoundException("Path tidak valid");
        File image=new File(getContext().getCacheDir(),SHARE_FILE);
        if(!image.isFile())throw new FileNotFoundException("File belum tersedia");
        return image;
    }
    @Override public String getType(Uri uri){return "image/png";}
    @Override public ParcelFileDescriptor openFile(Uri uri,String mode) throws FileNotFoundException {
        if(!"r".equals(mode))throw new FileNotFoundException("Read only");
        return ParcelFileDescriptor.open(verify(uri),ParcelFileDescriptor.MODE_READ_ONLY);
    }
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] selectionArgs,String sortOrder){
        String[] cols=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;
        MatrixCursor cursor=new MatrixCursor(cols);
        try{
            File f=verify(uri);
            Object[] row=new Object[cols.length];
            for(int i=0;i<cols.length;i++){
                if(OpenableColumns.DISPLAY_NAME.equals(cols[i]))row[i]="Resume_RPM_Area.png";
                else if(OpenableColumns.SIZE.equals(cols[i]))row[i]=f.length();
            }
            cursor.addRow(row);
        }catch(FileNotFoundException ignored){}
        return cursor;
    }
    @Override public Uri insert(Uri uri,ContentValues values){throw new UnsupportedOperationException("Read only");}
    @Override public int update(Uri uri,ContentValues values,String s,String[] a){throw new UnsupportedOperationException("Read only");}
    @Override public int delete(Uri uri,String s,String[] a){throw new UnsupportedOperationException("Read only");}
}
