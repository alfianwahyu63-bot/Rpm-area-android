package com.rpmarea.monitor;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.webkit.*;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebResourceRequest;
import android.webkit.WebChromeClient;
import android.webkit.PermissionRequest;
import android.webkit.JavascriptInterface;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.Toast;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import android.util.Base64;
import android.provider.DocumentsContract;

public class MainActivity extends Activity {
    private static final String BASE = "https://appassets.androidplatform.net/assets/";
    private static final int PICK_FILE = 1601, SAVE_FILE = 1602, REQ_CAMERA = 1603;
    private WebView web;
    private ValueCallback<Uri[]> filePicker;
    private byte[] pendingSave;
    private PermissionRequest cameraRequest;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);
        web.setBackgroundColor(0xff101a34);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setSupportMultipleWindows(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setMediaPlaybackRequiresUserGesture(true);
        CookieManager.getInstance().setAcceptCookie(true);
        web.addJavascriptInterface(new NativeBridge(), "RPMNative");
        web.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                Uri uri = r.getUrl();
                if ("appassets.androidplatform.net".equals(uri.getHost()) && "https".equals(uri.getScheme())) {
                    String path = uri.getPath();
                    if(path==null || !path.startsWith("/assets/")) return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", java.util.Collections.emptyMap(), new java.io.ByteArrayInputStream(new byte[0]));
                    String rel = path.substring("/assets/".length());
                    if(rel.isEmpty())rel="index.html";
                    if(rel.contains("..") || rel.startsWith("/"))return null;
                    try {
                        InputStream in = getAssets().open("web/"+rel);
                        String mime = rel.endsWith(".js")?"text/javascript":rel.endsWith(".css")?"text/css":rel.endsWith(".html")?"text/html":rel.endsWith(".png")?"image/png":rel.endsWith(".webmanifest")?"application/manifest+json":"application/octet-stream";
                        return new WebResourceResponse(mime, "UTF-8", in);
                    } catch (IOException e) {
                        return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", java.util.Collections.emptyMap(), new java.io.ByteArrayInputStream(new byte[0]));
                    }
                }
                return null;
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                Uri url=req.getUrl();
                if ("appassets.androidplatform.net".equals(url.getHost()) && "https".equals(url.getScheme()))return false;
                if("https".equals(url.getScheme()) || "http".equals(url.getScheme())){
                    openExternal(url.toString());return true;
                }
                return true;
            }
            @Override public void onPageFinished(WebView v, String url) {
                if(!url.startsWith(BASE))return;
                // Browser blob:<...> downloads do not work in Android WebView. Intercept the
                // app's generated download anchors and open Android's Save As picker.
                String hook="(function(){if(window._rpmNativeDownloadHook)return;window._rpmNativeDownloadHook=true;"+
                    "document.addEventListener('click',async function(e){"+
                    "var a=e.target.closest&&e.target.closest('a[download]');if(!a||!a.href||!a.href.startsWith('blob:'))return;"+
                    "e.preventDefault();e.stopImmediatePropagation();try{var b=await(await fetch(a.href)).blob();"+
                    "var fr=new FileReader();fr.onload=function(){var data=String(fr.result).split(',')[1];"+
                    "RPMNative.saveFile(a.download||'laporan',b.type||'application/octet-stream',data);};"+
                    "fr.readAsDataURL(b);}catch(ex){alert('Gagal menyimpan: '+ex.message);}"+
                    "},true);"+
                    "window.open=function(url){if(typeof url==='string'&&url.startsWith('https://'))RPMNative.openExternal(url);return null;};"+
                    "})();";
                web.evaluateJavascript(hook,null);
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if(filePicker!=null)filePicker.onReceiveValue(null);
                filePicker=callback;
                Intent i=new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
                try{startActivityForResult(Intent.createChooser(i,"Pilih file Excel / JSON"),PICK_FILE);return true;}
                catch(Exception ex){filePicker=null;callback.onReceiveValue(null);return false;}
            }
            @Override public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(()->{
                    if(!"appassets.androidplatform.net".equals(request.getOrigin().getHost())) {request.deny();return;}
                    boolean isCamera=false;
                    for(String resource:request.getResources())if(PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource))isCamera=true;
                    if(!isCamera){request.deny();return;}
                    cameraRequest=request;
                    if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.CAMERA},REQ_CAMERA);
                    else {request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});cameraRequest=null;}
                });
            }
            @Override public void onPermissionRequestCanceled(PermissionRequest request){if(cameraRequest==request)cameraRequest=null;}
        });
        web.loadUrl(BASE+"index.html");
    }

    private void openExternal(String url) {
        try {Uri uri=Uri.parse(url);if(!"https".equals(uri.getScheme()))return;
            startActivity(new Intent(Intent.ACTION_VIEW,uri));}
        catch(Exception e){Toast.makeText(this,"Tidak ada aplikasi untuk membuka tautan",Toast.LENGTH_LONG).show();}
    }

    public class NativeBridge {
        @JavascriptInterface public void openExternal(String url){runOnUiThread(()->MainActivity.this.openExternal(url));}
        @JavascriptInterface public void saveFile(String filename,String mime,String base64){
            try{
                final byte[] bytes=Base64.decode(base64,Base64.DEFAULT);
                runOnUiThread(()->{
                    pendingSave=bytes;
                    Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType(mime==null||mime.isEmpty()?"application/octet-stream":mime);
                    i.putExtra(Intent.EXTRA_TITLE,filename.replaceAll("[/\\]","_"));
                    try{startActivityForResult(i,SAVE_FILE);}catch(Exception ex){pendingSave=null;Toast.makeText(MainActivity.this,"Gagal membuka penyimpanan",Toast.LENGTH_LONG).show();}
                });
            }catch(Exception e){runOnUiThread(()->Toast.makeText(MainActivity.this,"File tidak dapat diproses",Toast.LENGTH_LONG).show());}
        }
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK_FILE){if(filePicker!=null){filePicker.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode,data));filePicker=null;}return;}
        if(requestCode==SAVE_FILE){if(resultCode==RESULT_OK && data!=null&&data.getData()!=null && pendingSave!=null){
            try(OutputStream out=getContentResolver().openOutputStream(data.getData())){if(out==null)throw new IOException("No output stream");out.write(pendingSave);Toast.makeText(this,"File tersimpan",Toast.LENGTH_SHORT).show();}
            catch(Exception e){Toast.makeText(this,"Gagal menyimpan file: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
            pendingSave=null;
        }
    }
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results){super.onRequestPermissionsResult(requestCode,permissions,results);
        if(requestCode==REQ_CAMERA && cameraRequest!=null){
            if(results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED)cameraRequest.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
            else cameraRequest.deny();cameraRequest=null;
        }
    }
    @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){if(web!=null){web.destroy();web=null;}super.onDestroy();}
}
