package aromko.de.wishlist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import aromko.de.wishlist.R;
import aromko.de.wishlist.fragment.ItemListFragment;
import aromko.de.wishlist.fragment.dummy.DummyContent;

public class MainActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentInteractionListener{

    private FirebaseAuth mAuth;
    private TextView txtUserEmail;
    private TextView txtUserName;
    private ListView listView;
    private String[] listentxt =  {"Seite 1", "Seite 2", "Seite 2", "Seite 2", "Seite 2", "Seite 2", "Seite 2", "Seite 2", "Seite 2", "Seite 2", "Seite 2", "Seite 2"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        listView =  (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> drawListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listentxt);
        listView.setAdapter(drawListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Fragment fragment = null;

                switch (i) {
                    case 0:
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                        break;
                    case 1:
                        fragment = new ItemListFragment();
                        break;
                    case 2:
                        startActivity(new Intent(MainActivity.this, ProfilActivity.class));
                }
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_frame, fragment);
                    ft.commit();
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });
        txtUserEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtUserEmail);
        txtUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtUserName);



        checkIfUserLoggedIn(navigationView);

    }

    public void checkIfUserLoggedIn(NavigationView navigationView) {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            txtUserEmail.setText(mAuth.getCurrentUser().getEmail());
            txtUserName.setText(mAuth.getCurrentUser().getDisplayName());
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
