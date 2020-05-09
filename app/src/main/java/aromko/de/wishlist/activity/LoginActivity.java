package aromko.de.wishlist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import aromko.de.wishlist.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String GOOGLE_REQUEST_ID_TOKEN = "1002207596437-e45j57olbmk7kpq3q3dpv89c92c7n8sj.apps.googleusercontent.com";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth fFirebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        TextView textView = (TextView) signInButton.getChildAt(0);
        textView.setText(R.string.txtLogin);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GOOGLE_REQUEST_ID_TOKEN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        fFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = fFirebaseAuth.getCurrentUser();
        if (currentUser == null) {
            mGoogleSignInClient.signOut();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                loginToFirebase(account);
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), R.string.txtGoogleSignInFauked, Toast.LENGTH_LONG);
            }
        }
    }

    private void loginToFirebase(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        fFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (fFirebaseAuth.getCurrentUser() != null) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        } else {
                            Snackbar.make(findViewById(R.id.main_layout), R.string.txtAithenticationFailed, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            signIn();
        }
    }

    @Override
    public void onBackPressed() {

    }
}

