package com.example.taketwo_partb.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
 * The type Dice activity.
 */
public class DiceActivity extends AppCompatActivity {

    private TextView tvDice1, tvDice2;
    private EditText etBetAmount;
    private Button btnRoll;

    private ImageView back_button;
    private TextView tvSelectedSide;

    private TextView tvWinnings, tvProfit, tvMultiplier, tvJackpot, tvBalance;

    private final String[] diceFaces = {"⚀", "⚁", "⚂", "⚃", "⚄", "⚅"};
    private final Random random = new Random();
    private final Handler handler = new Handler();

    private String playerId;
    private String gameName;
    private String riskLevelText;
    private float balance;
    private float currentBalance = 0f;

    private static final String MASTER_IP = InternalConfig.getBackendIp();
    private static final int MASTER_PORT = InternalConfig.getPort();

    private int requestCounter = 1;

    private MediaPlayer dice_sound;
    private MediaPlayer jackpot_sound;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);

        dice_sound = MediaPlayer.create(this, R.raw.dice);
        jackpot_sound = MediaPlayer.create(this, R.raw.bonus);

        tvDice1 = findViewById(R.id.tvDice1);
        tvDice2 = findViewById(R.id.tvDice2);
        etBetAmount = findViewById(R.id.etBetAmount);
        btnRoll = findViewById(R.id.btnRoll);

        back_button = findViewById(R.id.btnBack);

        tvSelectedSide = findViewById(R.id.tvSelectedSide);

        tvWinnings = findViewById(R.id.tvWinnings);
        tvProfit = findViewById(R.id.tvProfit);
        tvMultiplier = findViewById(R.id.tvMultiplier);
        tvJackpot = findViewById(R.id.tvJackpot);
        tvBalance = findViewById(R.id.tvBalance);

        playerId = getIntent().getStringExtra("playerId");
        gameName = getIntent().getStringExtra("gameName");
        riskLevelText = getIntent().getStringExtra("riskLevel");
        balance = getIntent().getFloatExtra("balance", 0f);

        if (riskLevelText == null || riskLevelText.trim().isEmpty()) {
            riskLevelText = "low";
        }

        currentBalance = balance;

        tvBalance.setText("Balance: " + formatMoney(currentBalance));

        fetchPlayerData();

        btnRoll.setOnClickListener(v -> startRoll());

        back_button.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("playerId", playerId);
            intent.putExtra("gameName", gameName);
            intent.putExtra("riskLevel", riskLevelText);
            intent.putExtra("balance", currentBalance);
            startActivity(intent);
        });
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

    private void startRoll() {

        tvSelectedSide.setText("Spinning...");

        String betText = etBetAmount.getText().toString().trim();

        if (betText.isEmpty()) {
            Toast.makeText(this, "Enter bet amount", Toast.LENGTH_SHORT).show();
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

        btnRoll.setEnabled(false);

        dice_sound.start();

        int duration = 1200;
        int interval = 100;
        int totalRolls = duration / interval;
        final int[] count = {0};

        handler.post(new Runnable() {
            @Override
            public void run() {
                tvDice1.setText(diceFaces[random.nextInt(6)]);
                tvDice2.setText(diceFaces[random.nextInt(6)]);

                count[0]++;
                if (count[0] < totalRolls) {
                    handler.postDelayed(this, interval);
                } else {
                    sendBetRequest(amount);
                }
            }
        });
        tvSelectedSide.setText("Spin complete");
    }

    private void sendBetRequest(float amount) {
        BetGameRequest request = new BetGameRequest(
                requestCounter++,
                gameName,
                playerId,
                amount
        );

        RequestShell shell = new RequestShell(request);

        new Thread(() -> {
            try {
                MasterClient client = new MasterClient(MASTER_IP, MASTER_PORT);
                RequestShell responseShell = client.send(shell);

                if (responseShell == null || responseShell.getBetGameRequest() == null) {
                    runOnUiThread(() -> {
                        btnRoll.setEnabled(true);
                        Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                BetGameRequest response = responseShell.getBetGameRequest();

                if (response.getStatus() == ServerRequest.RequestStatus.ERROR) {
                    runOnUiThread(() -> {
                        btnRoll.setEnabled(true);
                        Toast.makeText(this,
                                response.getErrorMessage() != null ? response.getErrorMessage() : "Bet failed",
                                Toast.LENGTH_SHORT).show();
                        fetchPlayerData();
                    });
                    return;
                }

                runOnUiThread(() -> {
                    btnRoll.setEnabled(true);

                    double multiplier = response.getMultiplier();
                    boolean jackpot = response.isJackpot();

                    int[] dice = getDiceVisual(multiplier, riskLevelText, jackpot);

                    tvDice1.setText(diceFaces[dice[0]]);
                    tvDice2.setText(diceFaces[dice[1]]);

                    tvWinnings.setText("Winnings: " + formatMoney((float) response.getWinnings()));
                    tvProfit.setText("Profit: " + formatMoney((float) response.getPlayerProfit()));
                    tvMultiplier.setText("Multiplier: x" + formatDouble(response.getMultiplier()));
                    if(jackpot){
                        jackpot_sound.start();
                        tvJackpot.setText("Jackpot: YES 🎉");
                    }
                    else{
                        tvJackpot.setText("No Jackpot");
                    }

                    fetchPlayerData();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnRoll.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private int[] getDiceVisual(double multiplier, String risk, boolean jackpot) {
        if (jackpot) {
            return randomChoice(new int[][]{
                    {5, 5}
            });
        }

        String safeRisk = risk == null ? "low" : risk.toLowerCase(Locale.US);

        if (Math.abs(multiplier - 0.0) < 0.0001) {
            switch (safeRisk) {
                case "high":
                    return randomChoice(new int[][]{
                            {0, 2}, {1, 4}, {0, 5}, {2, 4}, {1, 3}
                    });
                case "medium":
                    return randomChoice(new int[][]{
                            {0, 4}, {1, 5}, {0, 3}, {2, 5}, {1, 4}
                    });
                default:
                    return randomChoice(new int[][]{
                            {0, 3}, {1, 4}, {0, 2}, {2, 5}, {1, 5}
                    });
            }
        }

        if (Math.abs(multiplier - 1.0) < 0.0001) {
            switch (safeRisk) {
                case "high":
                    return randomChoice(new int[][]{
                            {3, 3}, {2, 2}, {4, 4}
                    });
                case "medium":
                    return randomChoice(new int[][]{
                            {2, 2}, {3, 3}, {1, 1}, {4, 4}
                    });
                default:
                    return randomChoice(new int[][]{
                            {1, 2}, {2, 3}, {3, 4}, {1, 1}
                    });
            }
        }

        if (Math.abs(multiplier - 2.0) < 0.0001) {
            switch (safeRisk) {
                case "high":
                    return randomChoice(new int[][]{
                            {4, 5}, {5, 4}, {4, 4}, {3, 5}
                    });
                case "medium":
                    return randomChoice(new int[][]{
                            {4, 4}, {3, 4}, {4, 5}, {2, 4}
                    });
                default:
                    return randomChoice(new int[][]{
                            {3, 3}, {2, 3}, {3, 4}, {4, 4}
                    });
            }
        }

        switch (safeRisk) {
            case "high":
                return randomChoice(new int[][]{
                        {5, 5}, {4, 5}, {5, 4}
                });
            case "medium":
                return randomChoice(new int[][]{
                        {4, 5}, {5, 4}, {4, 4}, {3, 5}
                });
            default:
                return randomChoice(new int[][]{
                        {4, 4}, {3, 4}, {4, 3}, {5, 4}
                });
        }
    }

    private int[] randomChoice(int[][] options) {
        return options[random.nextInt(options.length)];
    }

    private String formatMoney(float value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatDouble(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}