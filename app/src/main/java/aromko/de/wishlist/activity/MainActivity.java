package aromko.de.wishlist.activity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.util.ArrayList;
import java.util.List;

import aromko.de.wishlist.R;
import aromko.de.wishlist.adapter.WishlistAdapter;
import aromko.de.wishlist.fragment.ItemListFragment;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.model.WishList;
import aromko.de.wishlist.viewModel.WishListViewModel;

public class MainActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentInteractionListener {

    private String favoriteListId = "";
    private FirebaseAuth mAuth;
    private TextView txtUserEmail;
    private TextView txtUserName;
    private ListView listView;
    private ArrayList<WishList> listItems = new ArrayList<WishList>();
    private ImageButton imgBtnAddWishList;
    private WishListViewModel listViewModel;
    private String selectedWishlistId;
    private FloatingActionButton fab;

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

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent wishActivity = new Intent(MainActivity.this, WishActivity.class);
                wishActivity.putExtra("wishlistId", selectedWishlistId);
                startActivity(wishActivity);
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final WishlistAdapter drawListAdapter = new WishlistAdapter(this, listItems);
        listView.setAdapter(drawListAdapter);
        drawListAdapter.setNotifyOnChange(true);
        listViewModel = ViewModelProviders.of(this).get(WishListViewModel.class);
        favoriteListId = checkIfFavoriteListIdExists();

        final LiveData<List<WishList>> listsLiveData = listViewModel.getListsLiveData();

        listsLiveData.observe(this, new Observer<List<WishList>>() {
            @Override
            public void onChanged(@Nullable List<WishList> lists) {
                drawListAdapter.clear();
                for (WishList list : lists) {
                    drawListAdapter.add(list);
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                for (int i = 0; i < listView.getChildCount(); i++) {
                    listView.getChildAt(i).setBackgroundColor(Color.WHITE);
                }

                openFragment(position);

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        listView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (listView.getAdapter().getCount() > 0) {
                    listView.removeOnLayoutChangeListener(this);
                    selectFavoritesOnStartup();
                }
            }
        });

        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData data) {
                        if (data != null) {
                            Uri deepLink = data.getLink();
                            listViewModel.addUserToWishlist(deepLink.getQueryParameter("param"));
                        } else {
                            return;
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("SSSSSSSSSSSSSSSS", "getDynamicLink:onFailure", e);
                    }
                });

    }

    public void openFragment(int position) {
        listView.setSelection(position);
        selectedWishlistId = listItems.get(position).getKey();
        if (selectedWishlistId.equals(favoriteListId)) {
            fab.hide();
        } else {
            fab.show();
        }

        Fragment fragment = null;
        Bundle bundle = new Bundle();
        bundle.putString("wishlistId", selectedWishlistId);
        fragment = new ItemListFragment();
        fragment.setArguments(bundle);

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        listView.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        MainActivity.this.setTitle(listView.getItemAtPosition(position).toString());
    }

    public void selectFavoritesOnStartup() {
        openFragment(0);
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else if (id == R.id.action_shareList) {
            onInviteClicked();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(Wish item, int adapterPosition) {
    }

    @Override
    public void onFavoriteInteraction(Wish wish, Boolean isFavorite) {
    }

    @Override
    public void onMapInteraction(double longitude, double latitude) {

    }

    public void addWishList(View view) {
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
                    listViewModel.insertList(text, false);
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

    public void onInviteClicked() {
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.example.com/page?param=" + selectedWishlistId))
                .setDynamicLinkDomain("aromko.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Intent sendIntent = new Intent();
                            String msg = "Hey, ich möchte meine Liste mit dir teilen. Klicke auf den Link, um ihr beizutreten: " + shortLink.toString();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
                            sendIntent.setType("text/plain");
                            startActivityForResult(sendIntent, 0);
                            Log.i("SHORTLINK", shortLink.toString());
                        } else {
                            // Error
                            // ...
                        }
                    }
                });
    }

    public String checkIfFavoriteListIdExists() {
        SharedPreferences sharedPreferences = this.getPreferences(MODE_PRIVATE);
        String favoriteListId = "";
        if (!sharedPreferences.contains("favoriteListId")) {
            favoriteListId = listViewModel.insertList("Favoriten", true);
            sharedPreferences.edit().putString("favoriteListId", favoriteListId).commit();
        } else {
            favoriteListId = sharedPreferences.getString("favoriteListId", "");
        }
        return favoriteListId;
    }
}
