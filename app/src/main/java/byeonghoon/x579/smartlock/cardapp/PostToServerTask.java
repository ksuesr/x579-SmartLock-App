package byeonghoon.x579.smartlock.cardapp;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.kt.smcp.gw.ca.comm.exception.SdkException;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.tcp.BaseInfo;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.tcp.IMTcpConnector;
import com.kt.smcp.gw.ca.gwfrwk.adap.stdsys.sdk.tcp.LogIf;
import com.kt.smcp.gw.ca.util.IMUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import byeonghoon.x579.smartlock.cardapp.private_setting.PrivateSettings;

/**
 * Created by Byeonghoon on 26-May-16.
 */
public class PostToServerTask extends AsyncTask<Void, Void, Void> implements LocationListener {

    public static final String TAG = PostToServerTask.class.getSimpleName();

    private Map<String, Double> rows = new HashMap<>();
    private String apdu;
    private LocationManager manager;
    private Context ctx;

    public PostToServerTask(Context c, String apdu, Map<String, Double> rows) {
        this.apdu = apdu;
        this.rows = rows;
        this.manager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        this.ctx = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.i(TAG, "worker thread start");

        final String tag_location = TAG + ".Location";
        Criteria criteria = new Criteria();
        // 정확도
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        // 전원 소비량
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        // 고도, 높이 값을 얻어 올지를 결정
        criteria.setAltitudeRequired(false);
        // provider 기본 정보(방위, 방향)
        criteria.setBearingRequired(false);
        // 속도
        criteria.setSpeedRequired(false);
        // 위치 정보를 얻어 오는데 들어가는 금전적 비용
        criteria.setCostAllowed(true);
        if(manager == null) {
            Toast.makeText(ctx, "Failed to get location", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to get location");
            return null;
        }

        String provider = manager.getBestProvider(criteria, true);

        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            provider = LocationManager.GPS_PROVIDER;
        }

        Log.d(tag_location, "provider : " + provider);

        Location location = null;

        try {
            location = manager.getLastKnownLocation(provider);
        } catch (SecurityException se) {
            Log.w(tag_location, se);
        }

        if (location != null) {
            rows.put("latitude", location.getLatitude());
            rows.put("longitude", location.getLongitude());
        }

        /*
        try {
            manager.requestLocationUpdates(provider, 0, 0, this);
        } catch (SecurityException se) {
            Log.w(tag_location, se);
        }
        */

        // IoTMakers 연동 설정 정보
        BaseInfo info = new BaseInfo();
        // 접속 IP, Port 설정
        info.setIp("220.90.216.90");
        info.setPort(10020);
        //TODO: Modify Gateway connection ID, device ID, and password
        // 디바이스상세정보-> Gateway 연결 ID를 입력한다.
        info.setExtrSysId(PrivateSettings.iotDevice_gatewayID);
        // 디바이스상세정보-> 디바이스 아이디를 입력한다.
        info.setDeviceId(PrivateSettings.iotDevice_deviceID);
        // 디바이스상세정보-> 디바이스 패스워드를 입력한다.
        info.setPassword(PrivateSettings.iotDevice_devicePassword);
        // IoTMakers 연동 TCP Connector 생성
        IMTcpConnector connector = new IMTcpConnector();
        try {
            connector.activate(new LogIf(), info, (long) 3000);

            long transId = IMUtil.getTransactionLongRoundKey4();

            Log.d(TAG, rows.toString());
            // 계측 데이터 HashMap 객체로 전송한다.
            // key는 센싱태그 명 value는 계측값을 넣는다.
            Date d = new Date();
            Map<String, String> anothermap = new HashMap<>();
            anothermap.put("CardKey", apdu);
            connector.requestColecDatas(rows, anothermap, d, transId);

            connector.deactivate();

        } catch (SdkException e) {
            Log.d(TAG, "Connection to kt IoTMakers server is failed");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void _void) {
        super.onPostExecute(_void);
    }



    @Override
    public void onLocationChanged(Location location) {
        rows.put("latitude", location.getLatitude());
        rows.put("longitude", location.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
