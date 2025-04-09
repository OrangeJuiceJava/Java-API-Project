import javax.sound.midi.*;
import java.util.concurrent.*;

public class Drums
{
    public static void main(String[] args) throws Exception
    {
        //Set up synthesizer and MIDI channels
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        MidiChannel channel = synth.getChannels()[9];//Channel 10 is drum kit channel

        //Define the percussion sounds
        int bassDrum = 35;
        int snareDrum = 38;
        int hiHatClosed = 42;
        int hiHatOpen = 46;
        
        //Create a Runnable to play overlapping percussion sounds
        //Schedule the overlapping percussion pattern to run repeatedly
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable playOverlapping = new Runnable()
        {
            public void run()
            {
                //Play Bass Drum and Snare at the same time
                channel.noteOn(bassDrum, 800);
                channel.noteOn(hiHatOpen, 800);

                try { Thread.sleep(200); } catch (InterruptedException e) {}
                channel.noteOff(bassDrum);
                channel.noteOff(hiHatOpen);
                channel.noteOn(hiHatClosed, 800);

                try { Thread.sleep(200); } catch (InterruptedException e) {}
                channel.noteOff(hiHatClosed);
                channel.noteOn(snareDrum, 800);
                channel.noteOn(hiHatClosed, 800);

                try { Thread.sleep(200); } catch (InterruptedException e) {}
                channel.noteOff(snareDrum);
                channel.noteOff(hiHatClosed);
                channel.noteOn(hiHatClosed, 800);

                try { Thread.sleep(200); } catch (InterruptedException e) {}
                channel.noteOff(hiHatClosed);
            }
        };
        scheduler.scheduleAtFixedRate(playOverlapping, 0, 500, TimeUnit.MILLISECONDS);//Trigger every 500ms

        //Run for 10 seconds, then stop
        Thread.sleep(5000);
        scheduler.shutdown();
        synth.close();
    }
}
