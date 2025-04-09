import javax.sound.midi.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MelodyAndDrums
{
    public static void main(String[] args) throws Exception
    {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        MidiChannel melody = synth.getChannels()[0];
        MidiChannel drums = synth.getChannels()[9];

        //Drums
        int kick = 35;
        int snare = 38;
        int hiHat = 42;

        //Melody Notes
        int[] melodyNotes = {64, 60, 65, 62, 67, 64, 69, 65};//E, C, F, D, G, E, A, F
        AtomicInteger melodyIndex = new AtomicInteger(0);//Use AtomicInteger for thread-safe updates

        //Create a Runnable to play simplified drum pattern and melody
        Runnable playPatternAndMelody = new Runnable()
        {
            public void run()
            {
                //Drum pattern: Kick-Snare-Kick-Snare with Hi-Hat
                drums.noteOn(hiHat, 800);//Hi-Hat on every beat
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                drums.noteOff(hiHat);
                
                if (melodyIndex.get() % 2 == 0)
                {
                    drums.noteOn(kick, 800);//Kick drum on beat 1 and 3
                    try { Thread.sleep(50); } catch (InterruptedException e) {}
                    drums.noteOff(kick);
                }
                else
                {
                    drums.noteOn(snare, 800);//Snare drum on beat 2 and 4
                    try { Thread.sleep(50); } catch (InterruptedException e) {}
                    drums.noteOff(snare);
                }     
                
                melody.noteOn(melodyNotes[melodyIndex.get() % melodyNotes.length], 800);//Play current melody note
                try { Thread.sleep(300); } catch (InterruptedException e) {}//Melody duration
                
                //Turn off the melody note
                melody.noteOff(melodyNotes[melodyIndex.get() % melodyNotes.length]);

                //Cycle through the melody notes and drum pattern
                melodyIndex.set((melodyIndex.get() + 1) % melodyNotes.length);
            }
        };
        //Schedule the beat + melody pattern to run repeatedly
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(playPatternAndMelody, 0, 500, TimeUnit.MILLISECONDS);

        Thread.sleep(5000);
        scheduler.shutdown();
        synth.close();
    }
}
