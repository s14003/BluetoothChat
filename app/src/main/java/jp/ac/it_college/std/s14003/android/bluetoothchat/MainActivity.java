package jp.ac.it_college.std.s14003.android.bluetoothchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static final int REQUEST_CONNECT_DEVICE = 1000;
    // Whether the Log Fragment is currently shown
    private boolean mLogShown;
    private Menu myMenu;
    public static final int MENU_TOGGLE_CONNECT = Menu.FIRST;
    private final int MENU_QUIT = Menu.FIRST + 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
        selectDevice();
    }

    private void selectDevice() {
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        myMenu = menu;
        menu.add(0, MENU_TOGGLE_CONNECT, 1, "connect");
        menu.add(0, MENU_QUIT, 2, "Quit");
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_TOGGLE_CONNECT:
                selectDevice();
                break;
            case MENU_QUIT:
                finish();

        }
        return true;
    }
}
