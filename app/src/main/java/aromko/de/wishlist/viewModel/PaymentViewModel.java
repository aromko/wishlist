package aromko.de.wishlist.viewModel;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import aromko.de.wishlist.model.Payment;
import aromko.de.wishlist.model.Wish;

public class PaymentViewModel {

    private static final String DB_PATH_PAYMENTS = "/payments/";
    private static final String DB_PATH_WISHES = "/wishes/";

    private FirebaseUser fFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();


    public PaymentViewModel() {
    }

    public void buyItem(final String wishId, final double price, final double partialPrice, final String wishlistId) {
        FirebaseDatabase.getInstance().getReference(DB_PATH_PAYMENTS + wishId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Double> partialPayments = new HashMap<>();
                double salvagePrice = 0.00;
                if (dataSnapshot.exists()) {
                    Payment currentPayment = dataSnapshot.getValue(Payment.class);
                    if (currentPayment.getPartialPayments() != null) {
                        partialPayments.putAll(currentPayment.getPartialPayments());
                    }
                    if (partialPayments.containsKey(fFirebaseUser.getUid())) {
                        salvagePrice = currentPayment.getSalvagePrice() + partialPayments.get(fFirebaseUser.getUid()) - partialPrice;
                    } else {
                        salvagePrice = currentPayment.getSalvagePrice() - partialPrice;
                    }
                    if (salvagePrice <= 0.00) {
                        salvagePrice = 0.00;
                    }
                    partialPayments.put(fFirebaseUser.getUid(), partialPrice);
                    currentPayment.setSalvagePrice(salvagePrice);
                    currentPayment.setPartialPayments(partialPayments);
                    dataSnapshot.getRef().setValue(currentPayment);
                } else {
                    salvagePrice = price - partialPrice;
                    if (salvagePrice <= 0.00) {
                        salvagePrice = 0.00;
                    }
                    partialPayments.put(fFirebaseUser.getUid(), partialPrice);
                    Payment payment = new Payment(price, salvagePrice, partialPayments);
                    dataSnapshot.getRef().setValue(payment);
                }
                updateSalvagePriceInWish(salvagePrice, wishId, wishlistId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateSalvagePriceInWish(final double salvagePrice, String wishId, String wishlistId) {
        FirebaseDatabase.getInstance().getReference(DB_PATH_WISHES + wishlistId + "/" + wishId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Wish currentWish = dataSnapshot.getValue(Wish.class);
                    currentWish.setSalvagePrice(salvagePrice);
                    dataSnapshot.getRef().setValue(currentWish);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
