package com.example.waterapp.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.waterapp.model.ConsumptionRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Guarda e recupera os dados no SharedPreferences

/**
 * Persistência com SharedPreferences. Escolhido porque os dados são poucos
 * (meta, data, total e registros do dia), é nativo do Android, não exige
 * biblioteca nem banco, e os registros de dias anteriores são descartados,
 * o que torna um banco de dados desnecessário.
 */
public class WaterRepository {

    private final SharedPreferences prefs;

    public WaterRepository(Context context) {
        prefs = context.getSharedPreferences("water_prefs", Context.MODE_PRIVATE);
    }

    // Data do sistema no formato 2026-10-13
    private String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // Se a data salva for diferente de hoje, começa um novo dia:
    // apaga os registros e zera o acumulado (a meta é mantida)
    public void resetIfNewDay() {
        if (!today().equals(prefs.getString("date", ""))) {
            prefs.edit()
                    .putString("date", today())
                    .putString("records", "")
                    .putInt("total", 0)
                    .apply();
        }
    }

    // ----- Meta diária -----
    public int getGoal() {
        return prefs.getInt("goal", 2000); // 2000 ml se nunca foi definida
    }

    public void setGoal(int ml) {
        prefs.edit().putInt("goal", ml).apply();
    }

    // ----- Consumo acumulado -----
    public int getTotal() {
        return prefs.getInt("total", 0);
    }

    public boolean isGoalExceeded() {
        return getTotal() > getGoal();
    }

    // ----- Registros -----
    // Os registros ficam em um texto, um por linha, no formato "volume;instante"
    public void addRecord(int ml) {
        resetIfNewDay();
        String saved = prefs.getString("records", "");
        String newLine = ml + ";" + System.currentTimeMillis();
        String updated = saved.isEmpty() ? newLine : saved + "\n" + newLine;

        prefs.edit()
                .putString("records", updated)
                .putInt("total", getTotal() + ml)
                .apply();
    }

    public List<ConsumptionRecord> getRecords() {
        List<ConsumptionRecord> list = new ArrayList<>();
        String saved = prefs.getString("records", "");
        if (saved.isEmpty()) return list;

        for (String line : saved.split("\n")) {
            String[] parts = line.split(";");
            int ml = Integer.parseInt(parts[0]);
            long time = Long.parseLong(parts[1]);
            list.add(new ConsumptionRecord(ml, time));
        }
        return list;
    }
}