package com.earth.OsToolkit;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.Toast;

import com.earth.OsToolkit.Fragment.ChargingFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    public void initUI(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        Process process;
        switch (id) {
            case R.id.action_reboot :
                try {
                    Toast.makeText(this, getString(R.string.reboot_getRoot), Toast.LENGTH_SHORT).show();
                    process = Runtime.getRuntime().exec("su -c reboot");
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.reboot_fail), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_recovery :
                try {
                    Toast.makeText(this, getString(R.string.reboot_getRoot), Toast.LENGTH_SHORT).show();
                    process = Runtime.getRuntime().exec(new String[]{"su -c","reboot recovery"});
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.reboot_fail), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_soft :
                try {
                    Toast.makeText(this, getString(R.string.reboot_getRoot), Toast.LENGTH_SHORT).show();
                    process = Runtime.getRuntime().exec(new String[]{"su -c","killall zygote"});
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.reboot_fail), Toast.LENGTH_SHORT).show();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_main :
                break;
            case R.id.nav_charging :
                toolbar.setTitle(R.string.nav_charging);
                fragmentTransaction.replace(R.id.main_fragment,new ChargingFragment());
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
