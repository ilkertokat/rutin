package com.ilker.rutin.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Küçük, bağımlılıksız JSON okuyucu/yazıcı ve alan tabanlı (reflection) kodlayıcı.
 * Desteklenen alan türleri: int, long, boolean, double, String, int[], ArrayList<String|Integer|Long|T>, T.
 */
public final class Json {
    private Json() {}

    // ---------------- Yazma ----------------
    public static String write(Object o) {
        StringBuilder sb = new StringBuilder(4096);
        writeValue(sb, o);
        return sb.toString();
    }

    private static void writeValue(StringBuilder sb, Object v) {
        if (v == null) { sb.append("null"); return; }
        if (v instanceof String) { writeString(sb, (String) v); return; }
        if (v instanceof Integer || v instanceof Long || v instanceof Boolean) { sb.append(v); return; }
        if (v instanceof Double) {
            double d = (Double) v;
            if (Double.isNaN(d) || Double.isInfinite(d)) d = 0;
            sb.append(d); return;
        }
        if (v instanceof int[]) {
            int[] a = (int[]) v; sb.append('[');
            for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(','); sb.append(a[i]); }
            sb.append(']'); return;
        }
        if (v instanceof List) {
            List<?> l = (List<?>) v; sb.append('[');
            for (int i = 0; i < l.size(); i++) { if (i > 0) sb.append(','); writeValue(sb, l.get(i)); }
            sb.append(']'); return;
        }
        if (v instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) v; sb.append('{'); boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) sb.append(','); first = false;
                writeString(sb, String.valueOf(e.getKey())); sb.append(':'); writeValue(sb, e.getValue());
            }
            sb.append('}'); return;
        }
        // Nesne: public, statik olmayan, transient olmayan alanlar
        sb.append('{'); boolean first = true;
        for (Field f : v.getClass().getFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) continue;
            try {
                Object fv = f.get(v);
                if (fv == null) continue;
                if (!first) sb.append(','); first = false;
                writeString(sb, f.getName()); sb.append(':'); writeValue(sb, fv);
            } catch (IllegalAccessException ignored) {}
        }
        sb.append('}');
    }

    private static void writeString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }

    // ---------------- Okuma ----------------
    public static Object parse(String s) {
        P p = new P(s);
        p.ws();
        Object v = p.value();
        return v;
    }

    private static final class P {
        final String s; int i;
        P(String s) { this.s = s; }
        void ws() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
        Object value() {
            ws();
            if (i >= s.length()) throw new IllegalArgumentException("JSON bitti");
            char c = s.charAt(i);
            if (c == '{') return obj();
            if (c == '[') return arr();
            if (c == '"') return str();
            if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            if (s.startsWith("null", i)) { i += 4; return null; }
            return num();
        }
        Map<String, Object> obj() {
            LinkedHashMap<String, Object> m = new LinkedHashMap<>();
            i++; ws();
            if (s.charAt(i) == '}') { i++; return m; }
            while (true) {
                ws(); String k = str(); ws();
                if (s.charAt(i) != ':') throw new IllegalArgumentException("':' bekleniyordu @" + i);
                i++;
                m.put(k, value()); ws();
                char c = s.charAt(i++);
                if (c == '}') return m;
                if (c != ',') throw new IllegalArgumentException("',' bekleniyordu @" + i);
            }
        }
        List<Object> arr() {
            ArrayList<Object> l = new ArrayList<>();
            i++; ws();
            if (s.charAt(i) == ']') { i++; return l; }
            while (true) {
                l.add(value()); ws();
                char c = s.charAt(i++);
                if (c == ']') return l;
                if (c != ',') throw new IllegalArgumentException("',' bekleniyordu @" + i);
            }
        }
        String str() {
            StringBuilder sb = new StringBuilder();
            i++;
            while (true) {
                char c = s.charAt(i++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    char e = s.charAt(i++);
                    switch (e) {
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'u': sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                        default: sb.append(e);
                    }
                } else sb.append(c);
            }
        }
        Object num() {
            int st = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            String n = s.substring(st, i);
            if (n.isEmpty()) throw new IllegalArgumentException("Beklenmeyen karakter @" + st);
            if (n.indexOf('.') >= 0 || n.indexOf('e') >= 0 || n.indexOf('E') >= 0) return Double.parseDouble(n);
            return Long.parseLong(n);
        }
    }

    // ---------------- Nesneye dönüştürme ----------------
    @SuppressWarnings("unchecked")
    public static <T> T decode(Object json, Class<T> cls) {
        if (json == null) return null;
        try {
            T o = cls.newInstance();
            if (!(json instanceof Map)) return o;
            Map<String, Object> m = (Map<String, Object>) json;
            for (Field f : cls.getFields()) {
                int mod = f.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isTransient(mod) || Modifier.isFinal(mod)) continue;
                if (!m.containsKey(f.getName())) continue;
                Object v = m.get(f.getName());
                try { f.set(o, convert(v, f.getType(), f.getGenericType())); } catch (Exception ignored) {}
            }
            return o;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object convert(Object v, Class<?> t, Type gt) {
        if (t == int.class || t == Integer.class) return v == null ? 0 : ((Number) v).intValue();
        if (t == long.class || t == Long.class) return v == null ? 0L : ((Number) v).longValue();
        if (t == double.class || t == Double.class) return v == null ? 0.0 : ((Number) v).doubleValue();
        if (t == boolean.class || t == Boolean.class) return v != null && (Boolean) v;
        if (t == String.class) return v == null ? null : String.valueOf(v);
        if (t == int[].class) {
            List<Object> l = (List<Object>) v; int[] a = new int[l.size()];
            for (int k = 0; k < a.length; k++) a[k] = ((Number) l.get(k)).intValue();
            return a;
        }
        if (List.class.isAssignableFrom(t)) {
            ArrayList<Object> out = new ArrayList<>();
            if (v == null) return out;
            Class<?> et = Object.class;
            if (gt instanceof ParameterizedType) et = (Class<?>) ((ParameterizedType) gt).getActualTypeArguments()[0];
            for (Object e : (List<Object>) v) out.add(convert(e, et, et));
            return out;
        }
        if (v instanceof Map) return decode(v, t);
        return null;
    }
}
