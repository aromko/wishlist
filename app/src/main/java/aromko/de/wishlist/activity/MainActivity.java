package aromko.de.wishlist.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.util.ArrayList;
import java.util.List;

import aromko.de.wishlist.R;
import aromko.de.wishlist.adapter.WishlistAdapter;
import aromko.de.wishlist.fragment.ItemListFragment;
import aromko.de.wishlist.model.UserSetting;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.model.Wishlist;
import aromko.de.wishlist.repositories.UserSettingRepository;
import aromko.de.wishlist.services.UploadService;
import aromko.de.wishlist.utilities.PhotoHelper;
import aromko.de.wishlist.viewModel.WishlistViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentInteractionListener {

    public static final String AROMKO_PAGE_LINK = "https://aromko.page.link";
    public static final String EXAMPLE_LINK = "https://www.example.com/page?param=";
    public static final String TEXT_PLAIN = "text/plain";
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final String HIDE_INVITE_PEOPLE = "hide_invite_people";
    PhotoHelper photoHelper;
    private String favoriteListId = "";
    private FirebaseAuth fFirebaseAuth;
    private FirebaseUser fFirebaseUser;
    private TextView etUserEmail;
    private TextView etUserName;
    private ListView listView;
    private ArrayList<Wishlist> listItems = new ArrayList<Wishlist>();
    private ImageButton ibAddWishList;
    private WishlistViewModel listViewModel;
    private String selectedWishlistId;
    private FloatingActionButton fab;
    private TextView tvInfo;
    private CircleImageView civImage;
    private ImageButton ibDeleteWishlist;
    private String hideMenuItem;
    private String sharedText;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(UploadService.RESULT);
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.txtPictureSuccesfulUploaded),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.txtPictureNotSuccesfulUploaded),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(MainActivity.this, R.style.Theme_MaterialComponents_Dialog_Alert)
                        .setMessage(getString(R.string.txtGetPermissions))
                        .setPositiveButton(R.string.txtOk, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                                        MY_PERMISSIONS_REQUEST);
                            }
                        })
                        .setNegativeButton(R.string.txtCancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                signOut();
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST);
            }
        }

        ibAddWishList = findViewById(R.id.ibAddWishList);
        ibDeleteWishlist = findViewById(R.id.ibDeleteWishList);
        tvInfo = findViewById(R.id.tvInfo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        etUserEmail = navigationView.getHeaderView(0).findViewById(R.id.etUserEmail);
        etUserName = navigationView.getHeaderView(0).findViewById(R.id.etUserName);
        civImage = navigationView.getHeaderView(0).findViewById(R.id.civImage);

        photoHelper = new PhotoHelper(this);

        checkIfUserLoggedIn();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent wishActivity = new Intent(MainActivity.this, WishActivity.class);
                wishActivity.putExtra("wishlistId", selectedWishlistId);
                wishActivity.putExtra("sharedText", sharedText);
                sharedText = null;
                startActivity(wishActivity);
            }
        });

        listView = findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        final WishlistAdapter drawListAdapter = new WishlistAdapter(this, listItems);
        listView.setAdapter(drawListAdapter);
        drawListAdapter.setNotifyOnChange(true);
        listViewModel = ViewModelProviders.of(this).get(WishlistViewModel.class);
        try {
            checkIfFavoriteListIdExists();
        } catch (Exception e) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        final LiveData<List<Wishlist>> listsLiveData = listViewModel.getListsLiveData();
        listsLiveData.observe(this, new Observer<List<Wishlist>>() {
            @Override
            public void onChanged(@Nullable List<Wishlist> lists) {
                drawListAdapter.clear();
                for (Wishlist list : lists) {
                    if(list.getName().equalsIgnoreCase("Favoriten")) {
                        drawListAdapter.insert(list, 0);
                    } else {
                        drawListAdapter.add(list);
                    }
                }
            }
        });

        addListeners();

        processFirebaseDynamicLink();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), getString(R.string.txtHaveFunWithTheApp), Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.txtPermissionDenied), Toast.LENGTH_SHORT).show();
                    signOut();
                    finish();
                }
                return;
            }
        }
    }


    private void handleSendText(Intent intent) {
        sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Toast.makeText(getApplicationContext(), R.string.txtTextFromIntent, Toast.LENGTH_LONG).show();
        }
    }

    private void processFirebaseDynamicLink() {
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
                        Toast.makeText(getApplicationContext(), R.string.txtNoInvitationFound, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                for (int i = 0; i < listView.getChildCount(); i++) {
                    listView.getChildAt(i).setBackgroundColor(Color.WHITE);
                }

                if (listItems.get(position).getWishCounter() > 0) {
                    tvInfo.setVisibility(View.INVISIBLE);
                } else {
                    tvInfo.setVisibility(View.VISIBLE);
                }
                openFragment(position);

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                listView.setSelection(position);
                selectedWishlistId = listItems.get(position).getKey();

                showAlertDialog(position, R.layout.dialog_editwishlist);

                return true;
            }
        });
    }

    public void showAlertDialog(int position, final int layoutId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();

        View viewAddWishlist = inflater.inflate(layoutId, null);

        builder.setView(viewAddWishlist);
        final EditText txtNewWishlist = viewAddWishlist.findViewById(R.id.txtNewWishlist);
        if (position != -1) {
            txtNewWishlist.setText(listItems.get(position).getName());
        }
        builder.setPositiveButton(R.string.txtOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String text = txtNewWishlist.getText().toString();
                if (!text.isEmpty() || layoutId == R.layout.dialog_deletewishlist) {
                    switch (layoutId) {
                        case R.layout.dialog_editwishlist:
                            listViewModel.updateList(selectedWishlistId, text);
                            break;
                        case R.layout.dialog_addwishlist:
                            listViewModel.insertList(text, false);
                            break;
                        case R.layout.dialog_deletewishlist:
                            listViewModel.deleteList(selectedWishlistId);
                            break;
                    }
                }
                dialogInterface.dismiss();
            }
        }).setNegativeButton(R.string.txtCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void openFragment(int position) {
        listView.setSelection(position);
        selectedWishlistId = listItems.get(position).getKey();
        hideMenuItem = "";
        if (selectedWishlistId.equals(favoriteListId)) {
            fab.hide();
            hideMenuItem = HIDE_INVITE_PEOPLE;
        } else {
            fab.show();
        }
        invalidateOptionsMenu();

        Bundle bundle = new Bundle();
        bundle.putString("wishlistId", selectedWishlistId);
        ItemListFragment fragment = (ItemListFragment) Fragment.instantiate(this, ItemListFragment.class.getName(), bundle);

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        listView.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        Wishlist selectedWishlist = (Wishlist) listView.getItemAtPosition(position);
        MainActivity.this.setTitle(selectedWishlist.getName());
    }

    public void selectFavoritesOnStartup() {
        for (Wishlist favoriteWishlist : listItems) {
            if (favoriteWishlist.isFavoriteList()) {
                if(favoriteWishlist.getWishCounter() > 0) {
                    tvInfo.setVisibility(View.INVISIBLE);
                }
                openFragment(listItems.indexOf(favoriteWishlist));
                break;
            }
        }
    }

    public void checkIfUserLoggedIn() {
        fFirebaseAuth = FirebaseAuth.getInstance();
        fFirebaseUser = fFirebaseAuth.getCurrentUser();
        if (fFirebaseUser != null) {
            etUserEmail.setText(fFirebaseUser.getEmail());
            etUserName.setText(fFirebaseUser.getDisplayName());
            loadProfilePicture(fFirebaseAuth);
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }
    }

    public void loadProfilePicture(FirebaseAuth mAuth) {
        photoHelper.requestProfilePicture(mAuth.getCurrentUser().getUid());
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
        getMenuInflater().inflate(R.menu.main, menu);
        if (hideMenuItem == HIDE_INVITE_PEOPLE) {
            menu.findItem(R.id.action_invitePeople).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            signOut();
            finish();
            return true;
        } else if (id == R.id.action_invitePeople) {
            onInviteClicked();
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(MainActivity.this, ProfilActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    public void signOut() {
        fFirebaseAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
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

    @Override
    public void onUrlInteraction(String url) {
    }

    @Override
    public void onPaymentInteraction(String wishId, double price, double partialPrice, String wishlistId) {
    }

    @Override
    public void onChatInteraction(String wishId) {
    }

    @Override
    public void onDeleteWishInteraction(String wishId, String wishlistId) {
    }

    public void addWishList(View view) {
        showAlertDialog(-1, R.layout.dialog_addwishlist);
    }

    public void onInviteClicked() {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(EXAMPLE_LINK + selectedWishlistId))
                .setDomainUriPrefix(AROMKO_PAGE_LINK)
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            Intent sendIntent = new Intent();
                            String msg = getString(R.string.txtInvitationMessage) + shortLink.toString();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
                            sendIntent.setType(TEXT_PLAIN);
                            startActivityForResult(sendIntent, 0);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.txtInvitationLinkCouldNotBeCreated, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void checkIfFavoriteListIdExists() {
        final UserSettingRepository userSettingRepository = new UserSettingRepository();
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(fFirebaseAuth.getCurrentUser().getUid(), MODE_PRIVATE);
        if (sharedPreferences.getString("favoriteListId", "").isEmpty()) {
            userSettingRepository.get(fFirebaseAuth.getCurrentUser().getUid(), new UserSettingRepository.FirebaseCallback() {
                @Override
                public void onCallback(UserSetting userSetting) {
                    String favoriteListId = "";
                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(fFirebaseAuth.getCurrentUser().getUid(), MODE_PRIVATE);
                    if(userSetting.getFavoriteListId().isEmpty()) {
                        favoriteListId = listViewModel.insertList("Favoriten", true);
                        userSettingRepository.insert(fFirebaseAuth.getCurrentUser().getUid(), favoriteListId);
                    } else {
                        favoriteListId = userSetting.getFavoriteListId();
                    }
                    sharedPreferences.edit().putString("favoriteListId", favoriteListId).commit();
                }
            });
        } else {
            favoriteListId = sharedPreferences.getString("favoriteListId", "");
        }
    }

    public void deleteWishlist(View view) {
        if (selectedWishlistId.equals(favoriteListId)) {
            Toast.makeText(this, R.string.txtFavoritelistCantBeDeleted, Toast.LENGTH_LONG).show();
        } else {
            showAlertDialog(-1, R.layout.dialog_deletewishlist);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(
                UploadService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
