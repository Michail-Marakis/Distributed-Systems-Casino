package com.example.taketwo_partb.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taketwo_partb.R;
import com.example.taketwo_partb.domain.BetLevel;
import com.example.taketwo_partb.domain.Game;
import com.example.taketwo_partb.domain.RiskLevel;
import com.example.taketwo_partb.network.InternalConfig;
import com.example.taketwo_partb.network.MasterClient;
import com.example.taketwo_partb.protocol.AddBalanceRequest;
import com.example.taketwo_partb.protocol.BetGameRequest;
import com.example.taketwo_partb.protocol.GetGamesRequest;
import com.example.taketwo_partb.protocol.GetPlayerRequest;
import com.example.taketwo_partb.protocol.RateGameRequest;
import com.example.taketwo_partb.protocol.RequestShell;
import com.example.taketwo_partb.protocol.ServerRequest;
import com.example.taketwo_partb.ui.adapter.GameAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity {

    private static final String MASTER_IP = InternalConfig.getBackendIp();
    private static final int MASTER_PORT = InternalConfig.getPort();

    private EditText etPlayerId, etSearchName, etProviderName;
    private CheckBox cbLow, cbMedium, cbHigh, cbDollar, cbDollar2, cbDollar3;
    private TextView tvStatus, tvHeaderBalance;
    private RecyclerView recyclerGames;
    private static float amount;

    private final List<Game> games = new ArrayList<>();
    private GameAdapter adapter;
    private int requestCounter = 1;
    private MediaPlayer deposit_sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deposit_sound = MediaPlayer.create(this, R.raw.deposit);

        etPlayerId = findViewById(R.id.etPlayerId);
        etSearchName = findViewById(R.id.etSearchName);
        etProviderName = findViewById(R.id.etProviderName);
        tvStatus = findViewById(R.id.tvStatus);
        tvHeaderBalance = findViewById(R.id.tvHeaderBalance);

        cbLow = findViewById(R.id.cbLow);
        cbMedium = findViewById(R.id.cbMedium);
        cbHigh = findViewById(R.id.cbHigh);

        cbDollar = findViewById(R.id.cbDollar);
        cbDollar2 = findViewById(R.id.cbDollar2);
        cbDollar3 = findViewById(R.id.cbDollar3);

        Button btnSearch = findViewById(R.id.btnSearch);
        Button btnAddBalance = findViewById(R.id.btnAddBalance);

        recyclerGames = findViewById(R.id.recyclerGames);
        recyclerGames.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GameAdapter(games, this::showGameOptionsDialog);
        recyclerGames.setAdapter(adapter);

        String incomingPlayerId = getIntent().getStringExtra("playerId");
        float  incomingBalance  = getIntent().getFloatExtra("balance", -1f);

        if (incomingPlayerId != null && !incomingPlayerId.isEmpty()) {
            etPlayerId.setText(incomingPlayerId);
            fetchPlayerData(incomingPlayerId);
        }

        if (incomingBalance >= 0) {
            amount = incomingBalance;
            updateHeaderBalance(amount);
        }

        btnSearch.setOnClickListener(v -> {
            String playerId = etPlayerId.getText().toString().trim();
            if (playerId.isEmpty()) {
                Toast.makeText(this, "Player ID is required", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchPlayerData(playerId);
            searchGames();
        });

        btnAddBalance.setOnClickListener(v -> showAddBalanceDialog());
    }


    private void updateHeaderBalance(float value) {
        if (tvHeaderBalance != null) {
            tvHeaderBalance.setText(String.format(Locale.US, "%.2f", value));
        }
    }


    private void searchGames() {
        String searchName    = etSearchName.getText().toString().trim();
        String providerName  = etProviderName.getText().toString().trim();

        HashSet<BetLevel> betLevels = new HashSet<>();
        if (cbDollar.isChecked())  betLevels.add(BetLevel.$);
        if (cbDollar2.isChecked()) betLevels.add(BetLevel.$$);
        if (cbDollar3.isChecked()) betLevels.add(BetLevel.$$$);

        HashSet<RiskLevel> riskLevels = new HashSet<>();
        if (cbLow.isChecked())    riskLevels.add(RiskLevel.low);
        if (cbMedium.isChecked()) riskLevels.add(RiskLevel.medium);
        if (cbHigh.isChecked())   riskLevels.add(RiskLevel.high);

        GetGamesRequest request = new GetGamesRequest(
                requestCounter++,
                searchName,
                providerName,
                1,
                5,
                betLevels,
                riskLevels
        );

        RequestShell shell = new RequestShell(request);
        tvStatus.setText("Searching...");

        new Thread(() -> {
            try {
                MasterClient client = new MasterClient(MASTER_IP, MASTER_PORT);
                RequestShell responseShell = client.send(shell);

                List<Game> returnedGames = responseShell.getGetGamesRequest().getGrabbedGames();

                List<Game> uniqueGames = new ArrayList<>();
                HashSet<String> seen = new HashSet<>();

                if (returnedGames != null) {
                    for (Game g : returnedGames) {
                        String key = g.getGameName() + "|" + g.getProviderName();
                        if (seen.add(key)) {
                            uniqueGames.add(g);
                        }
                    }
                }

                runOnUiThread(() -> {
                    games.clear();
                    games.addAll(uniqueGames);
                    adapter.notifyDataSetChanged();
                    tvStatus.setText("Found " + uniqueGames.size() + " games");
                });

            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("Error: " + e.getMessage()));
            }
        }).start();
    }


    private void showGameOptionsDialog(Game game) {
        String[] options = {"Play", "Rate"};

        new AlertDialog.Builder(this)
                .setTitle(game.getGameName())
                .setItems(options, (dialog, which) -> {
                    String playerId = etPlayerId.getText().toString().trim();
                    if (which == 0) {
                        if (playerId.isEmpty()) {
                            Toast.makeText(this, "Player ID required", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        launchGame(game, playerId);
                    } else if (which == 1) {
                        showRateDialog(game);
                    }
                })
                .show();
    }

    private void launchGame(Game game, String playerId) {
        String name = game.getGameName();
        Intent intent;

        switch (name) {
            case "Lucky Slots":
                intent = new Intent(this, LuckySlotsActivity.class);
                break;
            case "Dice Game":
                intent = new Intent(this, DiceActivity.class);
                break;
            case "Lucky Wheel":
                intent = new Intent(this, WheelActivity.class);
                break;
            case "Coin Flip":
                intent = new Intent(this, CoinFlipActivity.class);
                break;
            case "Chicken Road":
                intent = new Intent(this,ChickenActivity.class);
                break;
            default:
                Toast.makeText(this, "Game not available yet", Toast.LENGTH_SHORT).show();
                return;
        }

        intent.putExtra("playerId", playerId);
        intent.putExtra("gameName", game.getGameName());
        intent.putExtra("riskLevel", game.getRiskLevel().name());
        intent.putExtra("balance", amount);
        startActivity(intent);
    }


    private void showAddBalanceDialog() {
        EditText input = new EditText(this);
        input.setTextColor(getColor(android.R.color.black));
        input.setHintTextColor(getColor(android.R.color.darker_gray));
        input.setHint("Amount");
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        new AlertDialog.Builder(this, R.style.CasinoAlertDialogTheme)
                .setTitle("Add Balance")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String playerId  = etPlayerId.getText().toString().trim();
                    String amountText = input.getText().toString().trim();

                    if (playerId.isEmpty() || amountText.isEmpty()) {
                        Toast.makeText(this, "Player ID and amount required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    amount = Float.parseFloat(amountText);
                    addBalance(playerId, amount);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Gets amount.
     *
     * @return the amount
     */
    public static float getAmount() {
        return amount;
    }

    private void addBalance(String playerId, float amount) {
        AddBalanceRequest request = new AddBalanceRequest(requestCounter++, playerId, amount);
        RequestShell shell = new RequestShell(request);

        tvStatus.setText("Adding balance...");

        new Thread(() -> {
            try {
                MasterClient client = new MasterClient(MASTER_IP, MASTER_PORT);
                client.send(shell);

                runOnUiThread(() -> {
                    deposit_sound.start();
                    tvStatus.setText("Balance added");
                    Toast.makeText(this, "Balance added successfully", Toast.LENGTH_SHORT).show();
                    fetchPlayerData(playerId);
                });

            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("Error: " + e.getMessage()));
            }
        }).start();
    }


    private void showRateDialog(Game game) {
        final EditText input = new EditText(this);
        input.setTextColor(getColor(android.R.color.black));
        input.setHintTextColor(getColor(android.R.color.darker_gray));
        input.setHint("Enter rating (1-5)");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setPadding(40, 20, 40, 30);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CasinoAlertDialogTheme)
                .setTitle("Rate " + game.getGameName())
                .setMessage("Give a rating from 1 to 5")
                .setView(input)
                .setPositiveButton("Submit", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button submitButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            submitButton.setOnClickListener(v -> {
                String text = input.getText().toString().trim();
                if (text.isEmpty()) { input.setError("Rating is required"); return; }

                int rating;
                try {
                    rating = Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    input.setError("Enter a valid number");
                    return;
                }

                if (rating < 1 || rating > 5) {
                    input.setError("Rating must be between 1 and 5");
                    return;
                }

                dialog.dismiss();
                rateGame(game.getGameName(), rating);
            });
        });

        dialog.show();
    }

    private void rateGame(String gameName, int rating) {
        RateGameRequest request = new RateGameRequest(requestCounter++, gameName, rating);
        RequestShell shell = new RequestShell(request);
        tvStatus.setText("Submitting rating...");

        new Thread(() -> {
            try {
                MasterClient client = new MasterClient(MASTER_IP, MASTER_PORT);
                client.send(shell);
                runOnUiThread(() -> {
                    tvStatus.setText("Rating submitted");
                    Toast.makeText(this, "Rating submitted", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("Error: " + e.getMessage()));
            }
        }).start();
    }


    private void fetchPlayerData(String playerId) {
        GetPlayerRequest request = new GetPlayerRequest(requestCounter++, playerId);
        RequestShell shell = new RequestShell(request);

        new Thread(() -> {
            try {
                MasterClient client = new MasterClient(MASTER_IP, MASTER_PORT);
                RequestShell responseShell = client.send(shell);

                GetPlayerRequest response = responseShell.getGetPlayerRequest();

                runOnUiThread(() -> {
                    if (response.getStatus() == ServerRequest.RequestStatus.ERROR) {
                        tvStatus.setText("User not found");
                        return;
                    }

                    float balance = response.getUser().getTokens();
                    amount = balance;
                    tvStatus.setText("Balance: " + String.format(Locale.US, "%.2f", balance));
                    updateHeaderBalance(balance);
                });

            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
}