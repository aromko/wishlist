package aromko.de.wishlist.activity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import aromko.de.wishlist.R;
import aromko.de.wishlist.fragment.ItemListFragment;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.model.WishList;
import aromko.de.wishlist.viewModel.WishListViewModel;

public class MainActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentInteractionListener {

    private FirebaseAuth mAuth;
    private TextView txtUserEmail;
    private TextView txtUserName;
    private ListView listView;
    private ArrayList<String> listItems = new ArrayList<String>();
    private ImageButton imgBtnAddWishList;
    private WishListViewModel listViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgBtnAddWishList = findViewById(R.id.imgBtnAddWishList);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        txtUserEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtUserEmail);
        txtUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtUserName);

        checkIfUserLoggedIn(navigationView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final ArrayAdapter<String> drawListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(drawListAdapter);

        listViewModel = ViewModelProviders.of(this).get(WishListViewModel.class);

        LiveData<List<WishList>> listsLiveData = listViewModel.getListsLiveData();

        listsLiveData.observe(this, new Observer<List<WishList>>() {
            @Override
            public void onChanged(@Nullable List<WishList> lists) {
                listItems.clear();
                for (WishList list : lists) {
                    listItems.add(list.getName());
                }
                drawListAdapter.notifyDataSetChanged();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Fragment fragment = null;
                listView.setSelection(position);

                for (int i = 0; i < listView.getChildCount(); i++) {
                    if (i == position) {
                        listView.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        MainActivity.this.setTitle(listView.getItemAtPosition(position).toString());
                    } else {
                        listView.getChildAt(i).setBackgroundColor(Color.WHITE);
                    }
                }
                //Bundle bundle = new Bundle();
                //bundle.putString("wishlistname", listView.getItemAtPosition(position).toString());
                fragment = new ItemListFragment();
                //fragment.setArguments(bundle);
                switch (position) {
                    case 0:
                        /*FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();*/
                        break;
                    case 1:
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
    public void onListFragmentInteraction(Wish item) {
    }

    @Override
    public void onFavoriteInteraction(Wish item, Boolean isFavorite) {
    }

    public void addWishList(View view) {
        Log.i("test", "ksakdjgaskd");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();

        View viewAddWishlist = inflater.inflate(R.layout.dialog_addwishlist, null);
        builder.setView(viewAddWishlist);
        final EditText txtNewWishlist = viewAddWishlist.findViewById(R.id.txtNewWishlist);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String text = txtNewWishlist.getText().toString();
                if (!text.isEmpty()) {
                    listViewModel.insertList(text);
                }
                dialogInterface.dismiss();
            }
        }).setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builder.create();

        dialog.show();
    }
}
