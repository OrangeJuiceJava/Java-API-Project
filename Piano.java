import javax.sound.midi.*;
import java.util.concurrent.*;

public class Piano
{
    public static void main(String[] args) throws Exception
    {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        MidiChannel channel = synth.getChannels()[0];//Channel 1 is piano

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable playNote = new Runnable()
        {
            int note = 60;//Start from Middle C
            public void run() {
                channel.noteOn(note, 600);
                try
                {
                    Thread.sleep(200);
                }
                catch (InterruptedException e)
                {

                }
                channel.noteOff(note);
                note += 1;//Move up by 2 semitones each beat
                if (note > 72) note = 60;//Loop back
            }
        };
        scheduler.scheduleAtFixedRate(playNote, 0, 250, TimeUnit.MILLISECONDS);

        //Run for 5 seconds, then stop
        Thread.sleep(5000);
        scheduler.shutdown();
        synth.close();
    }
}