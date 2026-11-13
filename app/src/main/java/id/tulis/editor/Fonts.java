package id.tulis.editor;

import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.TextPaint;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineHeightSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.ParagraphStyle;
import java.util.HashMap;

/** Font bawaan aplikasi (kompatibel ukuran dengan Calibri, Times New Roman, Arial, Courier New, Cambria). */
final class Fonts {
    static final String[] NAMES = {"Calibri", "Times New Roman", "Arial", "Courier New", "Cambria"};
    private static final String[] FILES = {"Carlito", "LiberationSerif", "LiberationSans", "LiberationMono", "Caladea"};
    private static final HashMap<String, Typeface> CACHE = new HashMap<String, Typeface>();
    static AssetManager am;

    static int family(String name) {
        if (name == null) return 0;
        String n = name.toLowerCase();
        for (int k = 0; k < NAMES.length; k++) if (NAMES[k].toLowerCase().equals(n)) return k;
        if (n.contains("mono") || n.contains("courier") || n.contains("consol")) return 3;
        if (n.contains("sans") || n.contains("arial") || n.contains("helvet") || n.contains("verdana") || n.contains("tahoma")) return 2;
        if (n.contains("times") || n.contains("serif") || n.contains("georgia") || n.contains("garamond") || n.contains("book") || n.contains("palatino")) return 1;
        if (n.contains("cambria")) return 4;
        return 0;
    }

    static Typeface get(String name, boolean bold, boolean italic) {
        int k = family(name);
        String suf = bold ? (italic ? "BoldItalic" : "Bold") : (italic ? "Italic" : "Regular");
        String key = FILES[k] + suf;
        Typeface t = CACHE.get(key);
        if (t == null) {
            try {
                t = Typeface.createFromAsset(am, "fonts/" + FILES[k] + "-" + suf + ".ttf");
            } catch (Exception e) {
                Typeface base = k == 1 || k == 4 ? Typeface.SERIF : k == 3 ? Typeface.MONOSPACE : Typeface.SANS_SERIF;
                t = Typeface.create(base, (bold ? 1 : 0) | (italic ? 2 : 0));
            }
            CACHE.put(key, t);
        }
        return t;
    }
}

/** Jenis huruf + tebal + miring dalam satu span, supaya memakai file font yang sebenarnya. */
class FontSpan extends MetricAffectingSpan {
    final String name;
    final boolean bold, italic;
    FontSpan(String name, boolean bold, boolean italic) { this.name = name; this.bold = bold; this.italic = italic; }
    private void apply(TextPaint p) {
        p.setTypeface(Fonts.get(name, bold, italic));
        p.setFakeBoldText(false);
        p.setTextSkewX(0);
    }
    @Override public void updateDrawState(TextPaint p) { apply(p); }
    @Override public void updateMeasureState(TextPaint p) { apply(p); }
}

/** Ukuran huruf dalam setengah poin (22 = 11 pt), persis seperti di file docx. */
class SizeSpan extends MetricAffectingSpan {
    final int hp;
    SizeSpan(int hp) { this.hp = hp; }
    @Override public void updateDrawState(TextPaint p) { p.setTextSize(hp * Docx.unit); }
    @Override public void updateMeasureState(TextPaint p) { p.setTextSize(hp * Docx.unit); }
}

/** Penanda rata kiri-kanan (Android 4.2 belum bisa menampilkannya, tapi tersimpan di docx). */
class JustifySpan implements ParagraphStyle { }

/** Jarak baris: 240 = 1.0, 276 = 1.15, 360 = 1.5, 480 = 2.0. */
class LineSpacingSpan implements LineHeightSpan {
    final int line;
    LineSpacingSpan(int line) { this.line = line; }
    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
        int h = fm.descent - fm.ascent;
        int extra = Math.round(h * (line / 240f - 1f));
        fm.descent += extra;
        fm.bottom += extra;
    }
}

/** Indentasi kiri dalam twips (720 = 1,27 cm). */
class IndentSpan implements LeadingMarginSpan {
    final int twips;
    IndentSpan(int twips) { this.twips = twips; }
    @Override public int getLeadingMargin(boolean first) { return Math.round(twips * Docx.unit / 10f); }
    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom,
                                  CharSequence text, int start, int end, boolean first, Layout layout) { }
}
