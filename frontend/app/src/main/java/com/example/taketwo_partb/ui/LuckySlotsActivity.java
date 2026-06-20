package com.example.taketwo_partb.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taketwo_partb.R;
import com.example.taketwo_partb.domain.CasinoPlayer;
import com.example.taketwo_partb.network.InternalConfig;
import com.example.taketwo_partb.network.MasterClient;
import com.example.taketwo_partb.protocol.AddBalanceRequest;
import com.example.taketwo_partb.protocol.BetGameRequest;
import com.example.taketwo_partb.protocol.GetPlayerRequest;
import com.example.taketwo_partb.protocol.RequestShell;
import com.example.taketwo_partb.protocol.ServerRequest;

import org.w3c.dom.Text;

import java.util.Locale;
import java.util.Random;

/**
 * The type Lucky slots activity.
 */
public class LuckySlotsActivity extends AppCompatActivity {

    private TextView tvSlot1, tvSlot2, tvSlot3;
    private EditText etBetAmount;
    private Button btnSpin;

    private final String[] symbols = {"🍒", "🍋", "🍊", "🍉", "⭐"};
    private final Random random = new Random();
    private TextView tvWinnings, tvProfit, tvMultiplier, tvJackpot;
    private final Handler handler = new Handler();

    private ImageView back_button;
    private TextView tvSpinStatus;

    private String playerId;
    private String gameName;
    private TextView personalBalance;

    private static final String MASTER_IP = InternalConfig.getBackendIp();
    private static final int MASTER_PORT = InternalConfig.getPort();

    private int requestCounter = 1;

    private MediaPlayer spin_sound;
    private MediaPlayer jackpot_sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lucky_slots);

        spin_sound = MediaPlayer.create(this, R.raw.spin);
        jackpot_sound = MediaPlayer.create(this, R.raw.bonus);

        tvSlot1 = findViewById(R.id.tvSlot1);
        tvSlot2 = findViewById(R.id.tvSlot2);
        tvSlot3 = findViewById(R.id.tvSlot3);
        etBetAmount = findViewById(R.id.etBetAmount);
        btnSpin = findViewById(R.id.btnSpin);
        tvWinnings = findViewById(R.id.tvWinnings);
        tvProfit = findViewById(R.id.tvProfit);
        tvMultiplier = findViewById(R.id.tvMultiplier);
        personalBalance = findViewById(R.id.tvBalance);
        tvJackpot = findViewById(R.id.tvJackpot);

        back_button = findViewById(R.id.btnBack);
        tvSpinStatus = findViewById(R.id.tvSpinStatus);

        playerId = getIntent().getStringExtra("playerId");
        gameName = getIntent().getStringExtra("gameName");

        //na exoume to playerid prwta
        fetchPlayerData();

        back_button.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("playerId", playerId);
            i.putExtra("balance", currentBalance);
            startActivity(i);
        });

        btnSpin.setOnClickListener(v -> startSpin());
    }
    private float currentBalance = 0;

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
                            personalBalance.setText("Balance: ERROR (no response)")
                    );
                    return;
                }

                GetPlayerRequest response = responseShell.getGetPlayerRequest();

                runOnUiThread(() -> {
                    if (response.getStatus() == ServerRequest.RequestStatus.OK
                            && response.getUser() != null) {

                        currentBalance = response.getUser().getTokens();

                        personalBalance.setText("Balance: " + format(currentBalance));

                    } else {
                        personalBalance.setText("Balance: ERROR (server rejected)");
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        personalBalance.setText("Balance: ERROR " + e.getMessage())
                );
            }
        }).start();
    }
    private void startSpin() {
        tvSpinStatus.setText("Spinning...");
        String betText = etBetAmount.getText().toString().trim();
        if (betText.isEmpty()) {
            Toast.makeText(this, "Enter bet amount", Toast.LENGTH_SHORT).show();
            return;
        }

        float amount = Float.parseFloat(betText);

        btnSpin.setEnabled(false);

        spin_sound.start();

        //Animation
        int spinDuration = 1500;
        int interval = 100;
        int totalSpins = spinDuration / interval;
        final int[] count = {0};

        handler.post(new Runnable() {
            @Override
            public void run() {
                tvSlot1.setText(symbols[random.nextInt(symbols.length)]);
                tvSlot2.setText(symbols[random.nextInt(symbols.length)]);
                tvSlot3.setText(symbols[random.nextInt(symbols.length)]);

                count[0]++;
                if (count[0] < totalSpins) {
                    handler.postDelayed(this, interval);
                } else {
                    //otan teleiwsei animation, stelnme request
                    sendBetRequest(amount);
                }
            }
        });
        tvSpinStatus.setText("Spin complete");
    }



    private void sendBetRequest(float amount) {

        if (currentBalance < amount) {
            Toast.makeText(this,"Not enough money. Fill your balance", Toast.LENGTH_SHORT).show();
            btnSpin.setEnabled(true);
            return;
        }

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

                BetGameRequest response = responseShell.getBetGameRequest();

                if (response.getStatus() == ServerRequest.RequestStatus.ERROR) {
                    runOnUiThread(() -> {
                        btnSpin.setEnabled(true);
                        Toast.makeText(this,
                                response.getErrorMessage() != null ? response.getErrorMessage() : "Bet failed",
                                Toast.LENGTH_SHORT).show();
                        fetchPlayerData();
                    });
                    return;
                }
                runOnUiThread(() -> {
                    btnSpin.setEnabled(true);

                    double multiplier = response.getMultiplier();
                    boolean isJackpot = response.isJackpot();

                    String s1, s2, s3;

                    if (isJackpot) {
                        s1 = "⭐"; s2 = "⭐"; s3 = "⭐";
                    } else {
                        int key = (int) Math.round(multiplier * 10);

                        switch (key) {

                            case 0:
                                int r = random.nextInt(3);
                                if (r == 0) {
                                    s1 = "🍒";
                                    s2 = "🍋";
                                    s3 = "🍊";
                                } else if (r == 1) {
                                    s1 = "🍋";
                                    s2 = "🍉";
                                    s3 = "🍒";
                                } else {
                                    s1 = "🍊";
                                    s2 = "🍒";
                                    s3 = "🍋";
                                }
                                break;

                            case 1:
                                s1 = "🍒";
                                s2 = "🍋";
                                s3 = "🍒";
                                break;

                            case 5:
                                s1 = "🍒";
                                s2 = "🍒";
                                s3 = "🍋";
                                break;

                            case 10:
                                s1 = "🍋";
                                s2 = "🍋";
                                s3 = "🍊";
                                break;

                            case 11:
                                s1 = "🍊";
                                s2 = "🍊";
                                s3 = "🍋";
                                break;

                            case 13:
                                s1 = "🍉";
                                s2 = "🍉";
                                s3 = "🍒";
                                break;

                            case 15:
                                s1 = "🍉";
                                s2 = "🍉";
                                s3 = "🍋";
                                break;

                            case 20:
                                s1 = "🍉";
                                s2 = "🍉";
                                s3 = "🍉";
                                break;

                            case 25:
                                s1 = "⭐";
                                s2 = "🍉";
                                s3 = "⭐";
                                break;

                            case 35:
                                s1 = "⭐";
                                s2 = "⭐";
                                s3 = "🍉";
                                break;

                            case 65:
                                s1 = "⭐";
                                s2 = "⭐";
                                s3 = "⭐";
                                break;

                            default:
                                s1 = "🍒";
                                s2 = "🍋";
                                s3 = "🍊";
                                break;
                        }
                    }

                    tvSlot1.setText(s1);
                    tvSlot2.setText(s2);
                    tvSlot3.setText(s3);

                    tvWinnings.setText("Winnings: " + format((float) response.getWinnings()));
                    tvProfit.setText("Profit: " + format((float) response.getPlayerProfit()));
                    tvMultiplier.setText("Multiplier: x" + format((float) response.getMultiplier()));

                    fetchPlayerData();

                    if (response.isJackpot()) {
                        jackpot_sound.start();
                        tvJackpot.setText("🎰 JACKPOT!");
                        tvJackpot.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    } else {
                        tvJackpot.setText("Jackpot: No");
                        tvJackpot.setTextColor(getResources().getColor(android.R.color.black));
                    }

                });

            } catch (Exception e) {
                runOnUiThread(() -> btnSpin.setEnabled(true));
            }
        }).start();
    }

    private String format(float value) {
        return String.format(Locale.US, "%.2f", value);
    }
}