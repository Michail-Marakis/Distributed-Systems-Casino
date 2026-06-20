package com.example.taketwo_partb.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taketwo_partb.R;
import com.example.taketwo_partb.network.InternalConfig;
import com.example.taketwo_partb.network.MasterClient;
import com.example.taketwo_partb.protocol.GetPlayerRequest;
import com.example.taketwo_partb.protocol.RequestShell;
import com.example.taketwo_partb.protocol.ServerRequest;

import java.util.Locale;

/**
 * The type Splash activity.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final String MASTER_IP = InternalConfig.getBackendIp();
    private static final int MASTER_PORT = InternalConfig.getPort();

    private EditText etSplashPlayerId;
    private Button btnSplashEnter;
    private Button btnSplashPlay;
    private TextView tvSplashBalance;
    private TextView tvSplashUsername;
    private TextView tvSplashStatus;
    private LinearLayout layoutBalanceCard;

    private float fetchedBalance = 0f;

    private int requestCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        etSplashPlayerId = findViewById(R.id.etSplashPlayerId);
        btnSplashEnter = findViewById(R.id.btnSplashEnter);
        btnSplashPlay = findViewById(R.id.btnSplashPlay);
        tvSplashBalance = findViewById(R.id.tvSplashBalance);
        tvSplashUsername = findViewById(R.id.tvSplashUsername);
        tvSplashStatus = findViewById(R.id.tvSplashStatus);
        layoutBalanceCard = findViewById(R.id.layoutBalanceCard);

        btnSplashEnter.setOnClickListener(v -> enterCasino());

    }

    private void fetchBalance() {
        String playerId = etSplashPlayerId.getText().toString().trim();

        if (playerId.isEmpty()) {
            Toast.makeText(this, "Enter your Player ID", Toast.LENGTH_SHORT).show();
            return;
        }

        tvSplashStatus.setText("Connecting to casino...");
        btnSplashEnter.setEnabled(false);
        layoutBalanceCard.setVisibility(View.GONE);

        GetPlayerRequest request = new GetPlayerRequest(requestCounter++, playerId);
        RequestShell shell = new RequestShell(request);

        new Thread(() -> {
            try {
                MasterClient client = new MasterClient(MASTER_IP, MASTER_PORT);
                RequestShell responseShell = client.send(shell);

                if (responseShell == null || responseShell.getGetPlayerRequest() == null) {
                    runOnUiThread(() -> {
                        tvSplashStatus.setText("No response from server");
                        btnSplashEnter.setEnabled(true);
                    });
                    return;
                }

                GetPlayerRequest response = responseShell.getGetPlayerRequest();

                runOnUiThread(() -> {
                    btnSplashEnter.setEnabled(true);

                    if (response.getStatus() == ServerRequest.RequestStatus.ERROR
                            || response.getUser() == null) {
                        tvSplashStatus.setText("Player not found. Check your ID.");
                        layoutBalanceCard.setVisibility(View.GONE);
                        return;
                    }

                    fetchedBalance = response.getUser().getTokens();

                    tvSplashBalance.setText(String.format(Locale.US, "%.2f", fetchedBalance));
                    tvSplashStatus.setText("");
                    layoutBalanceCard.setVisibility(View.VISIBLE);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSplashEnter.setEnabled(true);
                    tvSplashStatus.setText("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void enterCasino() {
        String playerId = etSplashPlayerId.getText().toString().trim();

        if (playerId.isEmpty()) {
            Toast.makeText(this, "Enter your Player ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("playerId", playerId);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}