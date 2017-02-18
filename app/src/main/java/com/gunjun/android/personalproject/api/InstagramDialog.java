package com.gunjun.android.personalproject.api;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by gunjunLee on 2017-02-15.
 */

public class InstagramDialog extends Dialog {
    static final float[] DIMENSIONS_LANDSCAPE = { 460, 300 };
    static final float[] DIMENSIONS_PORTRAIT = { 320, 420 };
    static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.FILL_PARENT,
            ViewGroup.LayoutParams.FILL_PARENT);
    static final int MARGIN = 4;
    static final int PADDING = 2;
    private String url;
    private OAuthDialogListener listener;
    private ProgressDialog spinner;
    private WebView webView;
    private LinearLayout content;
    private TextView titleView;
    private static final String TAG = "Instagram-WebView";
    public InstagramDialog(Context context, String url,
                           OAuthDialogListener listener) {
        super(context);
        this.url = url;
        listener = listener;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spinner = new ProgressDialog(getContext());
        spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        spinner.setMessage("Loading...");
        content = new LinearLayout(getContext());
        content.setOrientation(LinearLayout.VERTICAL);
        setUpTitle();
        setUpWebView();
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        final float scale = getContext().getResources().getDisplayMetrics().density;
        float[] dimensions = (display.getWidth() < display.getHeight()) ? DIMENSIONS_PORTRAIT : DIMENSIONS_LANDSCAPE;
        addContentView(content, new FrameLayout.LayoutParams( (int) (dimensions[0] * scale + 0.5f), (int) (dimensions[1] * scale + 0.5f)));
        CookieSyncManager.createInstance(getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    private void setUpTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        titleView = new TextView(getContext());
        titleView.setText("Instagram");
        titleView.setTextColor(Color.WHITE);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setBackgroundColor(Color.BLACK);
        titleView.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
        content.addView(titleView);
    }

    private void setUpWebView() {
        webView = new WebView(getContext());
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new OAuthWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        webView.setLayoutParams(FILL);
        content.addView(webView);
    }

    private class OAuthWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Redirecting URL " + url);
            if (url.startsWith(InstagramApp.callbackUrl)) {
                String urls[] = url.split("=");
                listener.onComplete(urls[1]);
                InstagramDialog.this.dismiss();
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.d(TAG, "Page error: " + description);
            super.onReceivedError(view, errorCode, description, failingUrl);
            listener.onError(description); InstagramDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "Loading URL: " + url);
            super.onPageStarted(view, url, favicon); spinner.show();
        }

        @Override public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String title = webView.getTitle();
            if (title != null && title.length() > 0) {
                titleView.setText(title);
            }
            Log.d(TAG, "onPageFinished URL: " + url);
            spinner.dismiss();
        }
    }

    public interface OAuthDialogListener {
        public abstract void onComplete(String accessToken);
        public abstract void onError(String error);
    }
}