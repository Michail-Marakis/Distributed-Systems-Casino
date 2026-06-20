package com.example.taketwo_partb.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taketwo_partb.R;
import com.example.taketwo_partb.domain.RiskLevel;
import com.example.taketwo_partb.network.InternalConfig;
import com.example.taketwo_partb.network.MasterClient;
import com.example.taketwo_partb.protocol.BetGameRequest;
import com.example.taketwo_partb.protocol.GetPlayerRequest;
import com.example.taketwo_partb.protocol.RequestShell;
import com.example.taketwo_partb.protocol.ServerRequest;

import java.util.Locale;
import java.util.Random;

/**
 * The type Wheel activity.
 */
public class WheelActivity extends AppCompatActivity {

    private ImageView imgWheel;
    private TextView tvResult, tvMultiplier, tvJackpot, tvBalance, tvSpinStatus;
    private Button btnSpin;
    private ImageButton btnBack;
    private EditText etBetAmount;

    private String playerId;
    private String gameName;
    private float currentBalance = 0f;
    private RiskLevel riskLevel;

    private static final String MASTER_IP = InternalConfig.getBackendIp();
    private static final int MASTER_PORT = InternalConfig.getPort();

    private int requestCounter = 1;

    private static final int SEGMENTS = 10;
    private static final float DEGREES_PER_SEGMENT = 360f / SEGMENTS;

    private float currentRotation = 0f;

    private final Random random = new Random();

    private MediaPlayer wheel_sound;
    private MediaPlayer jackpot_sound;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel);

        wheel_sound = MediaPlayer.create(this, R.raw.wheel_spin);
        jackpot_sound = MediaPlayer.create(this, R.raw.bonus);

        imgWheel = findViewById(R.id.imgWheel);
        tvResult = findViewById(R.id.tvResult);
        tvMultiplier = findViewById(R.id.tvMultiplier);
        tvJackpot = findViewById(R.id.tvJackpot);
        tvBalance = findViewById(R.id.tvBalance);
        btnSpin = findViewById(R.id.btnSpin);
        etBetAmount = findViewById(R.id.etBetAmount);

        btnBack = findViewById(R.id.btnBack);
        tvSpinStatus = findViewById(R.id.tvSpinStatus);

        playerId = getIntent().getStringExtra("playerId");
        gameName = getIntent().getStringExtra("gameName");
        riskLevel = RiskLevel.valueOf(getIntent().getStringExtra("riskLevel"));
        currentBalance = getIntent().getFloatExtra("balance", 0f);

        if(riskLevel == RiskLevel.high){
            imgWheel.setImageResource(R.drawable.highrisk);
        }else if(riskLevel == RiskLevel.medium){
            imgWheel.setImageResource(R.drawable.mediumlevel2);
        }else{
            imgWheel.setImageResource(R.drawable.lowrisk);
        }

        tvBalance.setText("Balance: " + formatMoney(currentBalance));

        fetchPlayerData();

        btnBack.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("playerId", playerId);
            i.putExtra("balance", currentBalance);
            startActivity(i);
        });

        btnSpin.setOnClickListener(v -> spinWheel());
    }

    private double[] getWheelMultipliers() {
        switch (riskLevel) {
            case low:
                return new double[]{0.0, 0.0, 0.0, 0.1, 0.5, 1.0, 1.1, 1.3, 2.0, 2.5};
            case medium:
                return new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 1.0, 1.5, 2.5, 3.5};
            case high:
                return new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 2.0, 6.5};
            default:
                return new double[]{0.0, 0.0, 0.0, 0.1, 0.5, 1.0, 1.1, 1.3, 2.0, 2.5};
        }
    }

    private int resolveSegment(double multiplier) {
        double[] wheel = getWheelMultipliers();

        java.util.List<Integer> matches = new java.util.ArrayList<>();

        for (int i = 0; i < wheel.length; i++) {
            if (Math.abs(wheel[i] - multiplier) < 0.0001) {
                matches.add(i);
            }
        }

        if (matches.isEmpty()) return 0;

        return matches.get(random.nextInt(matches.size()));
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

    private void spinWheel() {
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

        btnSpin.setEnabled(false);
        wheel_sound.start();
        tvResult.setText("Spinning...");

        new Thread(() -> {
            try {
                BetGameRequest request = new BetGameRequest(
                        requestCounter++,
                        gameName,
                        playerId,
                        amount
                );

                RequestShell shell = new RequestShell(request);

                MasterClient client = new MasterClient(MASTER_IP, MASTER_PORT);
                RequestShell responseShell = client.send(shell);

                if (responseShell == null || responseShell.getBetGameRequest() == null) {
                    runOnUiThread(() -> {
                        btnSpin.setEnabled(true);
                        Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

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

                int targetIndex = resolveSegment(response.getMultiplier());

                float POINTER_OFFSET = -150f;

                float targetAngle = (targetIndex * DEGREES_PER_SEGMENT)
                        + (DEGREES_PER_SEGMENT / 2f)
                        + POINTER_OFFSET;

                runOnUiThread(() -> animateWheel(targetAngle, response));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSpin.setEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void animateWheel(float targetAngle, BetGameRequest response) {
        float spins = 360f * (4 + random.nextInt(2));

        float randomOffset = (random.nextFloat() * (DEGREES_PER_SEGMENT * 0.8f))
                - (DEGREES_PER_SEGMENT * 0.4f);

        float finalAngle = spins + targetAngle + randomOffset;

        RotateAnimation rotate = new RotateAnimation(
                currentRotation,
                finalAngle,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );

        rotate.setDuration(2500);
        rotate.setFillAfter(true);
        rotate.setInterpolator(new DecelerateInterpolator());

        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                currentRotation = finalAngle % 360;
                btnSpin.setEnabled(true);

                tvMultiplier.setText("x" + formatDouble(response.getMultiplier()));
                tvResult.setText("Winnings: " + formatMoney((float) response.getWinnings()));

                if (response.isJackpot()) {
                    jackpot_sound.start();
                    tvJackpot.setText("🎰 JACKPOT!");
                } else {
                    tvJackpot.setText("No Jackpot");
                }

                fetchPlayerData();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        imgWheel.startAnimation(rotate);
    }

    private String formatMoney(float value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatDouble(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}