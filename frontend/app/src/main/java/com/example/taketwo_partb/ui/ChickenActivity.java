package com.example.taketwo_partb.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taketwo_partb.R;
import com.example.taketwo_partb.network.InternalConfig;
import com.example.taketwo_partb.network.MasterClient;
import com.example.taketwo_partb.protocol.AddBalanceRequest;
import com.example.taketwo_partb.protocol.BetGameRequest;
import com.example.taketwo_partb.protocol.GetPlayerRequest;
import com.example.taketwo_partb.protocol.RequestShell;
import com.example.taketwo_partb.protocol.ServerRequest;

import java.util.Locale;

/**
 * The type Chicken activity.
 */
public class ChickenActivity extends AppCompatActivity {

    private static final String MASTER_IP = InternalConfig.getBackendIp();
    private static final int MASTER_PORT = InternalConfig.getPort();

    private LinearLayout roadContainer;
    private HorizontalScrollView scrollView;
    private MediaPlayer chicken_dies_sound;
    private MediaPlayer chicken_goes_sound;

    private TextView tvProgress, tvCurrentMultiplier;
    private TextView tvOutcome, tvWinnings, tvBalance;

    private EditText etBetAmount;
    private Button btnMove, btnStop, btnPlay, btnReset;
    private ImageButton btnBack;

    private String playerId, gameName;

    private float currentBalance = 0f;

    private int step = 0;
    private float initialBet = 0f;
    private double cumulativeMultiplier = 1.0;

    private boolean gameRunning = false;
    private int requestCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chicken);

        chicken_dies_sound = MediaPlayer.create(this, R.raw.chicken_dies);
        chicken_goes_sound = MediaPlayer.create(this, R.raw.chicken_goes);
        bindViews();
        readIntent();
        setupListeners();
        resetGame();
    }

    private void bindViews() {
        roadContainer = findViewById(R.id.roadContainer);
        scrollView = findViewById(R.id.scrollView);

        tvProgress = findViewById(R.id.tvProgress2);
        tvCurrentMultiplier = findViewById(R.id.tvCurrentMultiplier2);

        tvOutcome = findViewById(R.id.tvOutcome2);
        tvWinnings = findViewById(R.id.tvWinnings2);
        tvBalance = findViewById(R.id.tvBalance2);

        etBetAmount = findViewById(R.id.etBetAmount2);

        btnMove = findViewById(R.id.btnChooseMove2);
        btnStop = findViewById(R.id.btnChooseStop2);
        btnPlay = findViewById(R.id.btnFlip2);
        btnReset = findViewById(R.id.btnreset);

        btnBack = findViewById(R.id.btnBack);
    }

    private void readIntent() {
        playerId = getIntent().getStringExtra("playerId");
        gameName = getIntent().getStringExtra("gameName");
        currentBalance = getIntent().getFloatExtra("balance", 0f);

        tvBalance.setText(format(currentBalance));
    }

    private void setupListeners() {

        btnPlay.setOnClickListener(v -> startGame());
        btnMove.setOnClickListener(v -> nextStep());
        btnStop.setOnClickListener(v -> cashout());
        btnReset.setOnClickListener(v -> resetGame());

        btnBack.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("playerId", playerId);
            i.putExtra("balance", currentBalance);
            startActivity(i);
        });
    }

    private void startGame() {

        String betText = etBetAmount.getText().toString().trim();

        if (betText.isEmpty()) {
            toast("Enter bet");
            return;
        }

        float amount;
        try {
            amount = Float.parseFloat(betText);
        } catch (NumberFormatException e) {
            toast("Invalid bet");
            return;
        }

        if (amount <= 0 || currentBalance < amount) {
            toast("Invalid bet");
            return;
        }

        initialBet = amount;

        step = 0;
        cumulativeMultiplier = 1.0;
        gameRunning = true;

        roadContainer.removeAllViews();
        addTile("🐔", "START", false);

        tvOutcome.setText("RUNNING...");
        tvWinnings.setText("0.00");
        tvProgress.setText("Step: 0");
        tvCurrentMultiplier.setText("Multiplier: x1.00");

        btnPlay.setEnabled(false);
        btnMove.setEnabled(true);
        btnStop.setEnabled(true);

        autoScroll();
    }

    private void nextStep() {

        if (!gameRunning) return;

        btnMove.setEnabled(false);
        btnStop.setEnabled(false);

        sendStepRequest();
    }

    private void cashout() {

        if (!gameRunning) return;

        gameRunning = false;
        tvOutcome.setText("💰 CASHOUT");

        btnMove.setEnabled(false);
        btnStop.setEnabled(false);
        btnPlay.setEnabled(true);
    }

    private void resetGame() {

        gameRunning = false;

        step = 0;
        initialBet = 0f;
        cumulativeMultiplier = 1.0;

        roadContainer.removeAllViews();
        addTile("🐔", "START", false);

        tvOutcome.setText("READY");
        tvWinnings.setText("0.00");
        tvProgress.setText("Step: 0");
        tvCurrentMultiplier.setText("Multiplier: x1.00");

        etBetAmount.setText("");

        btnMove.setEnabled(false);
        btnStop.setEnabled(false);
        btnPlay.setEnabled(true);
    }

    private void sendStepRequest() {

        BetGameRequest request = new BetGameRequest(
                requestCounter++,
                gameName,
                playerId,
                initialBet
        );

        RequestShell shell = new RequestShell(request);

        new Thread(() -> {
            try {

                MasterClient client = new MasterClient(MASTER_IP, MASTER_PORT);
                RequestShell responseShell = client.send(shell);

                if (responseShell == null || responseShell.getBetGameRequest() == null) {
                    runOnUiThread(() -> {
                        toast("Invalid server response");
                        btnMove.setEnabled(true);
                        btnStop.setEnabled(true);
                    });
                    return;
                }

                BetGameRequest response = responseShell.getBetGameRequest();

                if (response.getStatus() == ServerRequest.RequestStatus.ERROR) {
                    runOnUiThread(() -> {
                        toast("Bet failed");
                        btnMove.setEnabled(true);
                        btnStop.setEnabled(true);
                    });
                    return;
                }

                runOnUiThread(() -> handleResponse(response));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    toast("Error: " + e.getMessage());
                    btnMove.setEnabled(true);
                    btnStop.setEnabled(true);
                });
            }
        }).start();
    }
    private void fetchPlayerData() {
        if (playerId == null) return;

        GetPlayerRequest request = new GetPlayerRequest(requestCounter++, playerId);
        RequestShell shell = new RequestShell(request);

        new Thread(() -> {
            try {
                MasterClient client = new MasterClient(MASTER_IP, MASTER_PORT);
                RequestShell responseShell = client.send(shell);

                if (responseShell == null || responseShell.getGetPlayerRequest() == null) {
                    runOnUiThread(() ->
                            tvBalance.setText("Balance: ERROR (no response)")
                    );
                    return;
                }

                GetPlayerRequest response = responseShell.getGetPlayerRequest();

                runOnUiThread(() -> {
                    if (response.getStatus() == ServerRequest.RequestStatus.OK
                            && response.getUser() != null) {

                        currentBalance = response.getUser().getTokens();
                        tvBalance.setText("Balance: " + formatMoney(currentBalance));

                    } else {
                        tvBalance.setText("Balance: ERROR");
                        Toast.makeText(this,
                                response.getErrorMessage() != null ? response.getErrorMessage() : "User fetch failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        tvBalance.setText("Balance: ERROR " + e.getMessage())
                );
            }
        }).start();
    }
    private void handleResponse(BetGameRequest response) {

        if (!gameRunning) return;

        double serverMultiplier = response.getMultiplier();

        if (serverMultiplier == 0) {

            gameRunning = false;

            addTile("💣", "BUST", true);

            fetchPlayerData();
            chicken_dies_sound.start();
            tvOutcome.setText("❌ BUST -" + format(initialBet));
            tvWinnings.setText("0.00");

            btnMove.setEnabled(false);
            btnStop.setEnabled(false);
            btnPlay.setEnabled(true);

            updateUI();
            return;
        }

        step++;
        chicken_goes_sound.start();
        cumulativeMultiplier = serverMultiplier;

        addTile("🐔", "x" + formatDouble(cumulativeMultiplier), false);

        double payout = response.getWinnings();
        fetchPlayerData();
        tvOutcome.setText("✅ SAFE");
        tvWinnings.setText(format(payout));

        btnMove.setEnabled(true);
        btnStop.setEnabled(true);

        updateUI();
    }

    private void updateUI() {
        tvProgress.setText("Step: " + step);
        tvCurrentMultiplier.setText("Multiplier: x" + formatDouble(cumulativeMultiplier));
        autoScroll();
    }

    private void addTile(String emoji, String text, boolean isBomb) {

        LinearLayout tile = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(180, 220);
        lp.setMargins(8, 0, 8, 0);
        tile.setLayoutParams(lp);

        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);

        TextView icon = new TextView(this);
        icon.setText(emoji);
        icon.setTextSize(24);

        TextView label = new TextView(this);
        label.setText(text);

        tile.addView(icon);
        tile.addView(label);

        roadContainer.addView(tile);
    }

    private void autoScroll() {
        scrollView.post(() ->
                scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT));
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatDouble(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatMoney(float value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
