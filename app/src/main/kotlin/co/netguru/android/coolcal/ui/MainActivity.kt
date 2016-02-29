package co.netguru.android.coolcal.ui

import android.content.IntentSender
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import co.netguru.android.coolcal.BuildConfig
import co.netguru.android.coolcal.R
import co.netguru.android.coolcal.utils.logDebug
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.UpdateManager

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    companion object {
        const val REQUEST_RESOLVE_ERROR = 1000
    }

    private var mResolvingError: Boolean = false

    private val googleApiClient: GoogleApiClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    private val fragments: List<BaseFragment?> by lazy {
        arrayListOf(supportFragmentManager
                .findFragmentById(R.id.weather_fragment) as BaseFragment?,
                supportFragmentManager
                        .findFragmentById(R.id.events_fragment) as BaseFragment?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        slidingLayout.setPanelSlideListener(fragments[1] as SlidingUpPanelLayout.PanelSlideListener)

        checkForUpdates()
    }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        if (googleApiClient.isConnected) {
            googleApiClient.disconnect()
        }
    }

    override fun onResume() {
        super.onResume()
        checkForCrashes()
    }

    override fun onPause() {
        super.onPause()
        UpdateManager.unregister()
    }

    private fun checkForCrashes() {
        CrashManager.register(this, BuildConfig.HOCKEYAPP_APP_ID)
    }

    private fun checkForUpdates() {
        // TODO Remove this for store / production builds!
        UpdateManager.register(this, BuildConfig.HOCKEYAPP_APP_ID)
    }

    /*
        Google API callbacks
     */
    private fun showErrorDialog(errorCode: Int) {
        val dialog = GoogleApiAvailability.getInstance()
                .getErrorDialog(this, errorCode, REQUEST_RESOLVE_ERROR)
        dialog.setOnDismissListener({ mResolvingError = false })
        dialog.show()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        logDebug("Connection Failed with code ${result.errorCode}")
        if (mResolvingError) {
            return
        }
        if (result.hasResolution()) {
            try {
                mResolvingError = true
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR)
            } catch (e: IntentSender.SendIntentException) {
                // todo: signal error to user
            }
        } else {
            showErrorDialog(result.errorCode)
            mResolvingError = true
        }
    }

    override fun onConnected(p0: Bundle?) {
        val location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        if (location != null) {
            fragments.forEach { fragment -> fragment?.onLocationChanged(location) }
        } else {
            logDebug("Null location!")
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        googleApiClient.connect()
    }

    override fun onBackPressed() {
        when (slidingLayout.panelState) {
            SlidingUpPanelLayout.PanelState.EXPANDED,
            SlidingUpPanelLayout.PanelState.ANCHORED,
            SlidingUpPanelLayout.PanelState.DRAGGING -> {
                slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
            else -> super.onBackPressed()
        }
    }
}