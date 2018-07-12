package aromko.de.wishlist.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aromko.de.wishlist.R;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.viewModel.WishViewModel;
import aromko.de.wishlist.viewModel.WishViewModelFactory;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ItemListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private WishViewModel wishViewModel;
    private ArrayList<Wish> listItems = new ArrayList<Wish>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ItemListFragment newInstance(int columnCount) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
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
            //wishViewModel = new WishViewModel(wishlistId.toString());

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
                    recyclerView.setAdapter(new WishRecyclerViewAdapter(listItems, mListener));
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
                    Map<String, Boolean> markAsFavorite = new HashMap<>();
                    markAsFavorite.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), isFavorite);
                    wish.setMarkedAsFavorite(markAsFavorite);
                    wishViewModel.setWishAsFavorite(wish.getWishlistId(), wish.getWishId(), wish);
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
        // TODO: Update argument type and name
        void onListFragmentInteraction(Wish item, int adapterPosition);

        void onFavoriteInteraction(Wish wish, Boolean isFavorite);
    }
}
