package com.example.taketwo_partb.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taketwo_partb.R;
import com.example.taketwo_partb.network.InternalConfig;
import com.example.taketwo_partb.network.MasterClient;
import com.example.taketwo_partb.protocol.BetGameRequest;
import com.example.taketwo_partb.protocol.GetPlayerRequest;
import com.example.taketwo_partb.protocol.RequestShell;
import com.example.taketwo_partb.protocol.ServerRequest;

import java.util.Locale;
import java.util.Random;


/**
 * The type Coin flip activity.
 */
public class CoinFlipActivity extends AppCompatActivity {


    private static final String MASTER_IP  = InternalConfig.getBackendIp();
    private static final int MASTER_PORT = InternalConfig.getPort();

    private TextView tvCoin, tvCoinResult, tvSelectedSide;
    private TextView tvOutcome, tvWinnings, tvMultiplier, tvJackpot, tvBalance;
    private EditText etBetAmount;
    private Button   btnChooseHeads, btnChooseTails, btnFlip;
    private ImageButton btnBack;

    private MediaPlayer coin_flip_sound;

    private MediaPlayer jackpot_sound;


    private String  playerId;
    private String  gameName;
    private String  riskLevelText;
    private float   currentBalance = 0f;

    private String chosenSide = null;

    private final Handler handler = new Handler();
    private final Random  random = new Random();
    private int requestCounter = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_flip);

        coin_flip_sound = MediaPlayer.create(this, R.raw.coin_flip);
        jackpot_sound = MediaPlayer.create(this, R.raw.bonus);

        bindViews();
        readIntent();
        setupListeners();
        fetchPlayerData();
    }


    private void bindViews() {
        tvCoin = findViewById(R.id.tvCoin);
        tvCoinResult  = findViewById(R.id.tvCoinResult);
        tvSelectedSide = findViewById(R.id.tvSelectedSide);
        tvOutcome = findViewById(R.id.tvOutcome);
        tvWinnings = findViewById(R.id.tvWinnings);
        tvMultiplier = findViewById(R.id.tvMultiplier);
        tvJackpot = findViewById(R.id.tvJackpot);
        tvBalance = findViewById(R.id.tvBalance);
        etBetAmount = findViewById(R.id.etBetAmount);
        btnChooseHeads = findViewById(R.id.btnChooseHeads);
        btnChooseTails = findViewById(R.id.btnChooseTails);
        btnFlip = findViewById(R.id.btnFlip);
        btnBack = findViewById(R.id.btnBack);
    }

    private void readIntent() {
        playerId = getIntent().getStringExtra("playerId");
        gameName = getIntent().getStringExtra("gameName");
        riskLevelText = getIntent().getStringExtra("riskLevel");
        currentBalance = getIntent().getFloatExtra("balance", 0f);

        if (riskLevelText == null || riskLevelText.trim().isEmpty()) {
            riskLevelText = "low";
        }

        tvBalance.setText(format(currentBalance));

    }


    private void setupListeners() {
        btnChooseHeads.setOnClickListener(v -> selectSide("HEADS"));
        btnChooseTails.setOnClickListener(v -> selectSide("TAILS"));
        btnFlip.setOnClickListener(v -> startFlip());
        btnBack.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("playerId", playerId);
            i.putExtra("balance", currentBalance);
            startActivity(i);
        });
    }

    private void selectSide(String side) {
        chosenSide = side;
        tvSelectedSide.setText("Your pick: " + side);

        if ("HEADS".equals(side)) {
            btnChooseHeads.setBackgroundResource(R.drawable.bg_button_gold);
            btnChooseTails.setBackgroundResource(R.drawable.bg_button_outlined);
            tvCoin.setText("🌕");
        } else {
            btnChooseTails.setBackgroundResource(R.drawable.bg_button_gold);
            btnChooseHeads.setBackgroundResource(R.drawable.bg_button_outlined);
            tvCoin.setText("🌑");
        }
        tvCoinResult.setText("Flip to reveal");
    }


    private void startFlip() {
        if (chosenSide == null) {
            Toast.makeText(this, "Choose Heads or Tails first", Toast.LENGTH_SHORT).show();
            return;
        }

        String betText = etBetAmount.getText().toString().trim();
        if (betText.isEmpty()) {
            Toast.makeText(this, "Enter a bet amount", Toast.LENGTH_SHORT).show();
            return;
        }

        float amount;
        try {
            amount = Float.parseFloat(betText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid bet amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Bet must be positive", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentBalance < amount) {
            Toast.makeText(this, "Not enough balance", Toast.LENGTH_SHORT).show();
            return;
        }

        btnFlip.setEnabled(false);
        btnChooseHeads.setEnabled(false);
        btnChooseTails.setEnabled(false);


        coin_flip_sound.start();
        animateCoinFlip(amount);
    }

    private void animateCoinFlip(float amount) {
        final String[] flipFrames = {"🟡", "🌑", "🟡", "🌑", "🟡", "🌑", "🟡"};
        final int[] frame = {0};
        final int totalFrames = flipFrames.length * 3;

        handler.post(new Runnable() {
            @Override
            public void run() {
                tvCoin.setText(flipFrames[frame[0] % flipFrames.length]);
                tvCoinResult.setText("Flipping...");
                frame[0]++;
                if (frame[0] < totalFrames) {
                    handler.postDelayed(this, 80);
                } else {
                    sendBetRequest(amount);
                }
            }
        });
    }

    private void sendBetRequest(float amount) {
        BetGameRequest request = new BetGameRequest(requestCounter++, gameName, playerId, amount);
        RequestShell shell = new RequestShell(request);

        new Thread(() -> {
            try {
                MasterClient client = new MasterClient(MASTER_IP, MASTER_PORT);
                RequestShell responseShell = client.send(shell);

                if (responseShell == null || responseShell.getBetGameRequest() == null) {
                    runOnUiThread(this::onFlipError);
                    return;
                }

                BetGameRequest response = responseShell.getBetGameRequest();

                if (response.getStatus() == ServerRequest.RequestStatus.ERROR) {
                    runOnUiThread(() -> {
                        Toast.makeText(this,
                                response.getErrorMessage() != null
                                        ? response.getErrorMessage()
                                        : "Bet failed",
                                Toast.LENGTH_SHORT).show();
                        onFlipError();
                    });
                    return;
                }

                runOnUiThread(() -> showFlipResult(response));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    onFlipError();
                });
            }
        }).start();
    }

    @SuppressLint("SetTextI18n")
    private void showFlipResult(BetGameRequest response) {
        double multiplier = response.getMultiplier();
        boolean jackpot = response.isJackpot();
        boolean won = multiplier >= 2.0;

        String landedSide = won ? chosenSide
                : ("HEADS".equals(chosenSide) ? "TAILS" : "HEADS");

        tvCoin.setText("HEADS".equals(landedSide) ? "🌕" : "🌑");
        tvCoinResult.setText(landedSide);

        if(won){
            tvOutcome.setText("✅ WIN");
        }
        else{
            tvOutcome.setText("❌ LOSE");
        }
        tvWinnings.setText(format((float) response.getWinnings()));
        tvMultiplier.setText("x" + String.format(Locale.US, "%.2f", multiplier));
        if(jackpot){
            jackpot_sound.start();
            tvJackpot.setText("YES 🎉");
        }
        else{
            tvJackpot.setText("NO");
        }

        btnFlip.setEnabled(true);
        btnChooseHeads.setEnabled(true);
        btnChooseTails.setEnabled(true);

        fetchPlayerData();
    }

    private void onFlipError() {
        tvCoinResult.setText("Error - try again");
        btnFlip.setEnabled(true);
        btnChooseHeads.setEnabled(true);
        btnChooseTails.setEnabled(true);
    }


    private void fetchPlayerData() {
        if (playerId == null) return;

        GetPlayerRequest request = new GetPlayerRequest(requestCounter++, playerId);
        RequestShell shell = new RequestShell(request);

        new Thread(() -> {
            try {
                MasterClient client = new MasterClient(MASTER_IP, MASTER_PORT);
                RequestShell responseShell = client.send(shell);

                if (responseShell == null || responseShell.getGetPlayerRequest() == null) return;

                GetPlayerRequest res = responseShell.getGetPlayerRequest();

                runOnUiThread(() -> {
                    if (res.getStatus() == ServerRequest.RequestStatus.OK && res.getUser() != null) {
                        currentBalance = res.getUser().getTokens();
                        tvBalance.setText(format(currentBalance));
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }



    private String format(float value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
