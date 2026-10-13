package com.example.waterapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waterapp.data.WaterRepository;
import com.example.waterapp.model.ConsumptionRecord;
import com.example.waterapp.view.WaterProgressView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private WaterRepository repository;
    private WaterProgressView progressView;
    private TextView tvAlert, tvTotal;
    private EditText etVolume, etGoal;
    private ArrayAdapter<String> adapter;
    private final List<String> lines = new ArrayList<>(); // linhas mostradas na lista

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new WaterRepository(this);

        progressView = findViewById(R.id.waterProgress);
        tvAlert = findViewById(R.id.tvAlert);
        tvTotal = findViewById(R.id.tvTotal);
        etVolume = findViewById(R.id.etVolume);
        etGoal = findViewById(R.id.etGoal);
        ListView lvRecords = findViewById(R.id.lvRecords);
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnGoal = findViewById(R.id.btnGoal);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lines);
        lvRecords.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addConsumption());
        btnGoal.setOnClickListener(v -> changeGoal());
    }

    // Roda sempre que a tela aparece (ao abrir o app e ao voltar para ele).
    // Por isso a tela sempre mostra os dados salvos e detecta virada de dia.
    @Override
    protected void onResume() {
        super.onResume();
        updateScreen();
    }

    private void addConsumption() {
        String text = etVolume.getText().toString();
        if (text.isEmpty() || Integer.parseInt(text) == 0) {
            Toast.makeText(this, "Informe um volume em ml", Toast.LENGTH_SHORT).show();
            return;
        }
        repository.addRecord(Integer.parseInt(text));
        etVolume.setText("");
        updateScreen();
    }

    private void changeGoal() {
        String text = etGoal.getText().toString();
        if (text.isEmpty() || Integer.parseInt(text) == 0) {
            Toast.makeText(this, "Informe uma meta em ml", Toast.LENGTH_SHORT).show();
            return;
        }
        repository.setGoal(Integer.parseInt(text));
        etGoal.setText("");
        updateScreen();
    }

    // Lê os dados salvos e atualiza tudo o que aparece na tela
    private void updateScreen() {
        repository.resetIfNewDay();
        int goal = repository.getGoal();
        int total = repository.getTotal();

        // Componente gráfico
        progressView.setMaxValue(goal);
        progressView.setConsumed(total);

        // Textos e alerta da tela
        tvTotal.setText("Acumulado: " + total + " ml | Meta: " + goal + " ml");
        if (repository.isGoalExceeded()) {
            tvAlert.setVisibility(View.VISIBLE);
        } else {
            tvAlert.setVisibility(View.GONE);
        }

        // Lista de registros do dia
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        lines.clear();
        for (ConsumptionRecord r : repository.getRecords()) {
            lines.add(format.format(new Date(r.getTimestamp())) + " - " + r.getVolumeMl() + " ml");
        }
        adapter.notifyDataSetChanged();
    }
}