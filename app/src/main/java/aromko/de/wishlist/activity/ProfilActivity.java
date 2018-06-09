package aromko.de.wishlist.activity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;

import java.util.Locale;

import aromko.de.wishlist.R;
import aromko.de.wishlist.model.HotStock;
import aromko.de.wishlist.viewModel.HotStockViewModel;

public class ProfilActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ProfilActivity";

    private TextView tvTicker;
    private TextView tvPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
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

        tvTicker = findViewById(R.id.ticker);
        tvPrice = findViewById(R.id.price);

        // Obtain a new or prior instance of HotStockViewModel from the
        // ViewModelProviders utility class.
        HotStockViewModel hotStockViewModel =
                ViewModelProviders.of(this).get(HotStockViewModel.class);


        LiveData<HotStock> hotStockLiveData = hotStockViewModel.getHotStockLiveData();

        hotStockLiveData.observe(this, new Observer<HotStock>() {
            @Override
            public void onChanged(@Nullable HotStock hotStock) {
                if (hotStock != null) {
                    // update the UI here
                    tvTicker.setText(hotStock.getTicker());
                    tvPrice.setText(String.format(Locale.getDefault(), "%.2f",
                            hotStock.getPrice()));
                }
            }
        });
    }
}
