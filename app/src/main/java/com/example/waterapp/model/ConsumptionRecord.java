package com.example.waterapp.model;

// Um registro de consumo: quanto foi bebido e em que instante
public class ConsumptionRecord {
    private final int volumeMl;
    private final long timestamp; // instante em milissegundos

    public ConsumptionRecord(int volumeMl, long timestamp) {
        this.volumeMl = volumeMl;
        this.timestamp = timestamp;
    }

    public int getVolumeMl() { return volumeMl; }
    public long getTimestamp() { return timestamp; }
}