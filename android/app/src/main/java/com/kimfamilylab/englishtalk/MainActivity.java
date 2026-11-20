package com.kimfamilylab.englishtalk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * English Talk: 영어 대화 웹 앱(kim-family-lab.github.io/english-talk)을 크롬 없이 여는 앱.
 * - 영어 앱 주소만 열리고 다른 사이트로는 이동하지 않음 (패밀리 링크에서 이 앱만 무제한으로 둘 수 있게)
 * - 마이크 권한을 받아 웹 앱(Gemini Live)에 넘겨 줌
 * - 대화 중 화면이 꺼지지 않게 함
 */
public class MainActivity extends Activity {
    private static final String HOME = "https://kim-family-lab.github.io/english-talk/";
    private static final String HOST = "kim-family-lab.github.io";
    private static final int MIC_REQ = 1;

    private WebView web;
    private PermissionRequest pending;

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#fff8ef"));
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                // 영어 앱 주소가 아니면 열지 않음
                String host = req.getUrl().getHost();
                return host == null || !host.equals(HOST);
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest req) {
                runOnUiThread(() -> {
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        req.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                    } else {
                        pending = req;
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQ);
                    }
                });
            }
        });

        if (saved != null) web.restoreState(saved);
        else web.loadUrl(HOME);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQ);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (pending != null) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                pending.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
            } else {
                pending.deny();
            }
            pending = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }
}
