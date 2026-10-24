package com.example.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.model.SocialPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WebState(
    val url: String = "",
    val title: String = "",
    val progress: Int = 100,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isDarkModeInjected: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = ""
)

class WebViewManager(private val context: Context) {

    private val webViews = mutableMapOf<SocialPlatform, WebView>()
    private val webStates = mutableMapOf<SocialPlatform, MutableStateFlow<WebState>>()

    val desktopUserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
    private var defaultMobileUserAgent: String = ""

    init {
        try {
            defaultMobileUserAgent = WebSettings.getDefaultUserAgent(context)
        } catch (_: Exception) {
            defaultMobileUserAgent = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
        }

        SocialPlatform.entries.forEach { platform ->
            val initialDesktop = platform == SocialPlatform.WHATSAPP
            webStates[platform] = MutableStateFlow(
                WebState(
                    url = platform.getInitialUrl(initialDesktop),
                    isDesktopMode = initialDesktop
                )
            )
        }
    }

    fun getWebStateFlow(platform: SocialPlatform): StateFlow<WebState> {
        return webStates.getOrPut(platform) {
            MutableStateFlow(WebState(url = platform.getInitialUrl(platform == SocialPlatform.WHATSAPP)))
        }.asStateFlow()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun getOrCreateWebView(
        platform: SocialPlatform,
        onPageFinishedCallback: ((String) -> Unit)? = null
    ): WebView {
        return webViews.getOrPut(platform) {
            WebView(context).apply {
                val state = webStates[platform] ?: MutableStateFlow(WebState())

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true

                    // Offline caching setup
                    cacheMode = WebSettings.LOAD_DEFAULT

                    // Media playback without requiring user interaction gesture
                    mediaPlaybackRequiresUserGesture = false

                    // Mixed content & security
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                    // Desktop mode by default for WhatsApp Web
                    val useDesktop = state.value.isDesktopMode
                    userAgentString = if (useDesktop) desktopUserAgent else defaultMobileUserAgent
                }

                // Cookie manager setup
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        state.value = state.value.copy(
                            progress = newProgress,
                            isLoading = newProgress in 1..99
                        )
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        if (!title.isNullOrBlank()) {
                            state.value = state.value.copy(title = title)
                        }
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        state.value = state.value.copy(
                            url = url ?: "",
                            isLoading = true,
                            hasError = false,
                            canGoBack = canGoBack(),
                            canGoForward = canGoForward()
                        )
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        state.value = state.value.copy(
                            url = url ?: "",
                            isLoading = false,
                            canGoBack = canGoBack(),
                            canGoForward = canGoForward()
                        )
                        url?.let { onPageFinishedCallback?.invoke(it) }

                        // Inject dark mode CSS if enabled for night viewing
                        if (state.value.isDarkModeInjected) {
                            injectDarkMode(platform)
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (request?.isForMainFrame == true) {
                            state.value = state.value.copy(
                                hasError = true,
                                errorMessage = "Unable to connect. You can view offline drafts or retry."
                            )
                        }
                    }
                }

                loadUrl(platform.getInitialUrl(state.value.isDesktopMode))
            }
        }
    }

    fun reload(platform: SocialPlatform) {
        webViews[platform]?.reload()
    }

    fun goBack(platform: SocialPlatform): Boolean {
        val webView = webViews[platform] ?: return false
        return if (webView.canGoBack()) {
            webView.goBack()
            true
        } else {
            false
        }
    }

    fun goForward(platform: SocialPlatform): Boolean {
        val webView = webViews[platform] ?: return false
        return if (webView.canGoForward()) {
            webView.goForward()
            true
        } else {
            false
        }
    }

    fun loadUrl(platform: SocialPlatform, url: String) {
        val webView = webViews[platform] ?: getOrCreateWebView(platform)
        webView.loadUrl(url)
    }

    fun toggleDesktopMode(platform: SocialPlatform) {
        val state = webStates[platform] ?: return
        val newDesktop = !state.value.isDesktopMode
        state.value = state.value.copy(isDesktopMode = newDesktop)

        val webView = webViews[platform] ?: return
        webView.settings.userAgentString = if (newDesktop) desktopUserAgent else defaultMobileUserAgent
        webView.settings.useWideViewPort = newDesktop
        webView.reload()
    }

    fun toggleInjectedDarkMode(platform: SocialPlatform) {
        val state = webStates[platform] ?: return
        val newMode = !state.value.isDarkModeInjected
        state.value = state.value.copy(isDarkModeInjected = newMode)
        if (newMode) {
            injectDarkMode(platform)
        } else {
            webViews[platform]?.reload()
        }
    }

    private fun injectDarkMode(platform: SocialPlatform) {
        val webView = webViews[platform] ?: return
        val darkCss = """
            javascript:(function() {
                var style = document.getElementById('omni-dark-style');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'omni-dark-style';
                    style.innerHTML = 'html { filter: invert(90%) hue-rotate(180deg) !important; background: #121212 !important; } img, video, [style*="background-image"] { filter: invert(100%) hue-rotate(180deg) !important; }';
                    document.head.appendChild(style);
                }
            })()
        """.trimIndent()
        webView.evaluateJavascript(darkCss, null)
    }

    fun clearPlatformData(platform: SocialPlatform) {
        val webView = webViews[platform]
        webView?.clearCache(true)
        webView?.clearHistory()
        webView?.clearFormData()
    }

    fun clearAllCookiesAndCache() {
        webViews.values.forEach {
            it.clearCache(true)
            it.clearHistory()
            it.clearFormData()
        }
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }

    fun destroy() {
        webViews.values.forEach {
            it.stopLoading()
            it.clearHistory()
            it.clearCache(true)
            it.destroy()
        }
        webViews.clear()
    }
}
