import javax.sound.midi.*;
import java.util.concurrent.*;

public class InstrumentChanger
{
    public static void main(String[] args) throws Exception
    {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        MidiChannel[] channels = synth.getChannels();

        channels[0].programChange(89);
        channels[1].programChange(90);
        channels[2].programChange(91);
        channels[3].programChange(92);
        channels[4].programChange(93);
        channels[5].programChange(94);
        channels[6].programChange(95);
        channels[7].programChange(96);

        channels[0].noteOn(60, 800);
        Thread.sleep(500);
        channels[0].noteOff(60);

        Thread.sleep(500);
        channels[1].noteOn(60, 800);
        Thread.sleep(500);
        channels[1].noteOff(60);

        Thread.sleep(500);
        channels[2].noteOn(60, 800);
        Thread.sleep(500);
        channels[2].noteOff(60);

        Thread.sleep(500);
        channels[3].noteOn(60, 800);
        Thread.sleep(500);
        channels[3].noteOff(60);

        Thread.sleep(500);
        channels[4].noteOn(60, 800);
        Thread.sleep(500);
        channels[4].noteOff(60);

        Thread.sleep(500);
        channels[5].noteOn(60, 800);
        Thread.sleep(500);
        channels[5].noteOff(60);
        Thread.sleep(500);

        channels[6].noteOn(60, 800);
        Thread.sleep(500);
        channels[6].noteOff(60);

        Thread.sleep(500);
        channels[7].noteOn(60, 800);
        Thread.sleep(500);
        channels[7].noteOff(60);
        Thread.sleep(500);
    }
}
