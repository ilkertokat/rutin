import com.ilker.rutin.core.Synth;

import java.io.File;
import java.io.FileOutputStream;

/** Derleme sırasında bildirim seslerini res/raw altına WAV olarak üretir. */
public class GenSounds {
    public static void main(String[] a) throws Exception {
        File dir = new File(a[0]);
        dir.mkdirs();
        write(dir, "notif_soft", Synth.N_SOFT);
        write(dir, "notif_push", Synth.N_PUSH);
        write(dir, "notif_crit", Synth.N_CRIT);
        write(dir, "notif_morning", Synth.N_MORNING);
    }

    static void write(File dir, String name, int id) throws Exception {
        try (FileOutputStream o = new FileOutputStream(new File(dir, name + ".wav"))) {
            o.write(Synth.wav(Synth.render(id, 0)));
        }
    }
}
