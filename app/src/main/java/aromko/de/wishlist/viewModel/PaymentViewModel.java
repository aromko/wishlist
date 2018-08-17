package aromko.de.wishlist.viewModel;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import aromko.de.wishlist.model.Payment;
import aromko.de.wishlist.model.Wish;

public class PaymentViewModel {

    public PaymentViewModel() {
    }

    public void buyItem(final String wishId, final double price, final double partialPrice, final String wishlistId) {
        FirebaseDatabase.getInstance().getReference("/payments/" + wishId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Double> partialPayments = new HashMap<>();
                double salvagePrice = 0.00;
                if (dataSnapshot.exists()) {
                    Payment currentPayment = dataSnapshot.getValue(Payment.class);
                    if (currentPayment.getPartialPayments() != null) {
                        partialPayments.putAll(currentPayment.getPartialPayments());
                    }

                    if (partialPayments.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        salvagePrice = currentPayment.getSalvagePrice() + partialPayments.get(FirebaseAuth.getInstance().getCurrentUser().getUid()) - partialPrice;
                    } else {
                        salvagePrice = currentPayment.getSalvagePrice() - partialPrice;
                    }

                    partialPayments.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), partialPrice);
                    currentPayment.setSalvagePrice(salvagePrice);
                    currentPayment.setPartialPayments(partialPayments);
                    dataSnapshot.getRef().setValue(currentPayment);
                    updateSalvagePriceInWish(salvagePrice, wishId, wishlistId);
                } else {
                    salvagePrice = price - partialPrice;
                    partialPayments.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), partialPrice);
                    Payment payment = new Payment(price, salvagePrice, partialPayments);
                    dataSnapshot.getRef().setValue(payment);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateSalvagePriceInWish(final double salvagePrice, String wishId, String wishlistId) {
        FirebaseDatabase.getInstance().getReference("/wishes/" + wishlistId + "/" + wishId).addListenerForSingleValueEvent(new ValueEventListener() {
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
