package com.example.waterapp.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.waterapp.R;

public class WaterProgressView extends View {

    private static final int ALERT_COLOR = 0xFFE53935; // vermelho do alerta

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);    // parte restante
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // parte realizada
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private int progressColor = 0xFF2196F3;
    private int textColor = 0xFF000000;
    private int maxValue = 2000;          // meta diária em ml

    private int consumed = 0;             // consumo acumulado em ml
    private float animatedPercent = 0;    // percentual mostrado durante a animação
    private boolean absoluteMode = false; // false = "%", true = "x/y ml"
    private ValueAnimator animator; // animação em andamento (para poder cancelar)

    // Construtor usado quando a View é criada pelo código
    public WaterProgressView(Context context) {
        super(context);
        init(context, null);
    }

    // Construtor usado quando a View é criada pelo XML
    public WaterProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Lê os atributos definidos no XML (attrs.xml)
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaterProgressView);
        progressColor = a.getColor(R.styleable.WaterProgressView_progressColor, progressColor);
        textColor = a.getColor(R.styleable.WaterProgressView_textColor, textColor);
        maxValue = a.getInt(R.styleable.WaterProgressView_maxValue, maxValue);
        a.recycle();

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setColor(0xFFE0E0E0);
        progressPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Ao clicar, alterna entre modo percentual e modo absoluto
        setOnClickListener(v -> {
            absoluteMode = !absoluteMode;
            invalidate();
        });
    }

    // ---------- Métodos públicos ----------

    // Atualiza o progresso em percentual da meta (pode passar de 100)
    public void setProgress(int percent) {
        consumed = percent * maxValue / 100;
        animateTo(percent);
    }

    // Atualiza pelo consumo acumulado em ml
    public void setConsumed(int ml) {
        consumed = ml;
        animateTo(consumed * 100f / maxValue);
    }

    // Altera a meta em tempo de execução
    public void setMaxValue(int ml) {
        if (ml <= 0) return;
        maxValue = ml;
        animateTo(consumed * 100f / maxValue);
    }

    // Meta ultrapassada? (usa os valores reais, não a animação)
    public boolean isGoalExceeded() {
        return consumed > maxValue;
    }

    // Anima o percentual até o valor novo e redesenha a cada passo
    private void animateTo(float target) {
        if (animator != null) animator.cancel(); // evita duas animações ao mesmo tempo
        animator = ValueAnimator.ofFloat(animatedPercent, target);
        animator.setDuration(700);
        animator.addUpdateListener(anim -> {
            animatedPercent = (float) anim.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    // ---------- Desenho ----------
// Se o layout usar wrap_content, a View ocupa 200dp por padrão
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int padrao = (int) (200 * getResources().getDisplayMetrics().density);
        setMeasuredDimension(resolveSize(padrao, widthSpec), resolveSize(padrao, heightSpec));
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float size = Math.min(getWidth(), getHeight());
        float stroke = size * 0.08f;
        float cx = getWidth() / 2f;

        // Título no topo
        textPaint.setColor(textColor);
        textPaint.setFakeBoldText(false);
        textPaint.setTextSize(size * 0.07f);
        canvas.drawText("Consumo diário de água", cx, size * 0.08f, textPaint);

        // Retângulo onde o círculo é desenhado (abaixo do título)
        float top = size * 0.14f + stroke;
        float bottom = size - stroke;
        float diameter = bottom - top;
        arcRect.set(cx - diameter / 2f, top, cx + diameter / 2f, bottom);
        float cy = arcRect.centerY();

        boolean exceeded = isGoalExceeded();

        // Círculo cinza (o que falta) e arco colorido (o que foi consumido)
        trackPaint.setStrokeWidth(stroke);
        progressPaint.setStrokeWidth(stroke);
        progressPaint.setColor(exceeded ? ALERT_COLOR : progressColor);

        canvas.drawArc(arcRect, 0, 360, false, trackPaint);
        float sweep = Math.min(animatedPercent, 100f) * 3.6f; // 100% = 360 graus
        canvas.drawArc(arcRect, -90, sweep, false, progressPaint);

        // Texto central: percentual ou absoluto
        String text;
        if (absoluteMode) {
            text = consumed + "/" + maxValue + " ml";
            textPaint.setTextSize(diameter * 0.12f);
        } else {
            text = Math.round(animatedPercent) + "%";
            textPaint.setTextSize(diameter * 0.2f);
        }
        textPaint.setFakeBoldText(true);
        textPaint.setColor(exceeded ? ALERT_COLOR : textColor);
        float textY = cy - (textPaint.descent() + textPaint.ascent()) / 2f; // centraliza na vertical
        canvas.drawText(text, cx, textY, textPaint);

        // Alerta visual quando passa da meta
        if (exceeded) {
            textPaint.setTextSize(diameter * 0.08f);
            canvas.drawText("Meta ultrapassada!", cx, cy + diameter * 0.2f, textPaint);
        }
    }
}