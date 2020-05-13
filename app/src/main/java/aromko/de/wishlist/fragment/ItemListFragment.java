package aromko.de.wishlist.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aromko.de.wishlist.R;
import aromko.de.wishlist.activity.ChatActivity;
import aromko.de.wishlist.adapter.WishRecyclerViewAdapter;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.viewModel.PaymentViewModel;
import aromko.de.wishlist.viewModel.WishViewModel;
import aromko.de.wishlist.viewModel.WishViewModelFactory;

import static android.content.Context.MODE_PRIVATE;

public class ItemListFragment extends Fragment {

    public static final String GOOGLE_NAVIGATION_Q = "google.navigation:q=";
    public static final String COM_GOOGLE_ANDROID_APPS_MAPS = "com.google.android.apps.maps";
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private WishViewModel wishViewModel;
    private ArrayList<Wish> listItems = new ArrayList<Wish>();

    private String favoriteListId = "";

    public ItemListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        FirebaseAuth fFirebaseAuth = FirebaseAuth.getInstance();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(fFirebaseAuth.getCurrentUser().getUid(), MODE_PRIVATE);
        favoriteListId = sharedPreferences.getString("favoriteListId", "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        String wishlistId = "";

        if (getArguments().size() > 0) {
            wishlistId = getArguments().getString("wishlistId");
        }

        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            final RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            wishViewModel = ViewModelProviders.of(this, new WishViewModelFactory(this.getActivity().getApplication(), wishlistId)).get(WishViewModel.class);

            final LiveData<List<Wish>> listsLiveData = wishViewModel.getListsLiveData();

            listsLiveData.observe(this, new Observer<List<Wish>>() {
                @Override
                public void onChanged(@Nullable List<Wish> lists) {
                    LinearLayoutManager myLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int scrollPosition = myLayoutManager.findFirstVisibleItemPosition();
                    listItems.clear();
                    for (Wish list : lists) {
                        listItems.add(list);
                    }
                    recyclerView.setAdapter(new WishRecyclerViewAdapter(listItems, mListener, favoriteListId));
                    recyclerView.scrollToPosition(scrollPosition);
                }
            });

            mListener = new OnListFragmentInteractionListener() {
                @Override
                public void onListFragmentInteraction(Wish item, int adapterPosition) {
                    recyclerView.scrollToPosition(adapterPosition);
                }

                @Override
                public void onFavoriteInteraction(Wish wish, Boolean isFavorite) {

                    Map<String, Boolean> markedAsFavorite = new HashMap<>();
                    if (wish.getMarkedAsFavorite() != null) {
                        markedAsFavorite.putAll(wish.getMarkedAsFavorite());
                    }
                    markedAsFavorite.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), isFavorite);
                    wish.setMarkedAsFavorite(markedAsFavorite);
                    wishViewModel.setWishAsFavorite(wish.getWishlistId(), wish.getWishId(), wish, favoriteListId);
                }

                @Override
                public void onMapInteraction(double longitude, double latitude) {
                    Uri gmmIntentUri = Uri.parse(GOOGLE_NAVIGATION_Q + String.valueOf(latitude) + "," + String.valueOf(longitude));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage(COM_GOOGLE_ANDROID_APPS_MAPS);
                    if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }

                @Override
                public void onUrlInteraction(String url) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }

                @Override
                public void onPaymentInteraction(String wishId, double price, double partialPrice, String wishlistId) {
                    PaymentViewModel paymentViewModel = new PaymentViewModel();
                    paymentViewModel.buyItem(wishId, price, partialPrice, wishlistId);
                }

                @Override
                public void onChatInteraction(String wishId) {
                    Intent chatActivity = new Intent(getContext(), ChatActivity.class);
                    chatActivity.putExtra("wishId", wishId);
                    startActivity(chatActivity);
                }

                @Override
                public void onDeleteWishInteraction(String wishId, String wishlistId) {
                    wishViewModel.deleteWish(wishId, wishlistId, favoriteListId);
                }
            };
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Wish item, int adapterPosition);

        void onFavoriteInteraction(Wish wish, Boolean isFavorite);

        void onMapInteraction(double longitude, double latitude);

        void onUrlInteraction(String url);

        void onPaymentInteraction(String wishId, double price, double partialPrice, String wishlistId);

        void onChatInteraction(String wishId);

        void onDeleteWishInteraction(String wishId, String wishlistId);
    }
}
