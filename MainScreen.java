import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.midi.*;
import java.util.*;
import java.util.concurrent.*;

public class MainScreen extends JPanel implements ActionListener
{
    private static JFrame main, home, instrument;
    private JPanel buttonPanel, musicPanel, dynamicsPanel, instrPanel;
    private JButton keys, guitars, synths, drums, bass, brass, wind, pad, playButton, stopButton, tempoDown, tempoUp;
    private JLabel tempoLabel;
    private static int selectedTrack = 0;

    private static JPanel trackPanel = new JPanel();
    private static Sequencer seq;
    private static ArrayList<Track> recordedTracks = new ArrayList<Track>();
    private static float recordedTempo;//Sets tempo

    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();
    int screenWidth = (int) screenSize.getWidth();
    int screenHeight = (int) screenSize.getHeight();

    public MainScreen() throws Exception
    {
        recordedTempo = 120.0f;//Default Tempo

        //Creates frame, panel, and buttons for main software making screen
        home = new JFrame("Software");
        home.setSize(screenWidth / 2, screenHeight / 2);
        home.setLocationRelativeTo(null);
        home.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        musicPanel = new JPanel();
        musicPanel.setLayout(new GridLayout(6, 1));
        home.add(musicPanel, BorderLayout.WEST);

        dynamicsPanel = new JPanel();
        dynamicsPanel.setLayout(new FlowLayout());
        home.add(dynamicsPanel, BorderLayout.NORTH);

        trackPanel.setLayout(new BoxLayout(trackPanel, BoxLayout.Y_AXIS));
        home.add(trackPanel, BorderLayout.EAST);

        //Creates instrument select frame
        instrument = new JFrame("Instrument Select");
        instrument.setSize(screenWidth / 3, screenHeight / 3);
        instrument.setLocationRelativeTo(null);
        instrument.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Creates instrPanel
        instrPanel = new JPanel();
        instrPanel.setLayout(new GridLayout(2, 4, screenWidth / 100, screenHeight / 100));

        //Creates instrument buttons
        keys = createButton("Keys", this);
        guitars = createButton("Guitars", this);
        synths = createButton("Synths", this);
        drums = createButton("Drums", this);
        bass = createButton("Bass", this);
        brass = createButton("Brass", this);
        wind = createButton("Wind", this);
        pad = createButton("Pad", this);

        //Adds buttons to instrPanel and instrPanel to instrument frame
        instrPanel.add(keys);
        instrPanel.add(guitars);
        instrPanel.add(synths);
        instrPanel.add(drums);
        instrPanel.add(bass);
        instrPanel.add(brass);
        instrPanel.add(wind);
        instrPanel.add(pad);

        instrument.add(instrPanel);

        playButton = createButton("Play", this);
        stopButton = createButton("Stop", this);
        tempoUp = createButton("+", this);
        tempoDown = createButton("-", this);
        tempoLabel = new JLabel("Tempo: " + recordedTempo);

        dynamicsPanel.add(playButton);
        dynamicsPanel.add(stopButton);
        dynamicsPanel.add(tempoDown);
        dynamicsPanel.add(tempoLabel);
        dynamicsPanel.add(tempoUp);

        //Creates and adds buttons to buttonPanel
        for (int i = 1; i < 7; i++)
        {
            JButton addTrack = new JButton("Add"  + i);
            final int trackNumber = i;//Gets track number for channels
            addTrack.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    selectedTrack = trackNumber;
                    instrument.setVisible(true);
                }
            });
            musicPanel.add(addTrack);
        }

        //Creates first frame with buttons asking user for samples or making own music
        main = new JFrame("Home");
        main.setSize(screenWidth / 4, screenHeight / 4);
        main.setLocationRelativeTo(null);
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        main.add(buttonPanel);

        JButton create = new JButton("Create your own");
        JButton samples = new JButton("Play Sample");
        
        create.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                main.dispose();
                home.setFocusable(true);
                home.setVisible(true);
            }
        });
        samples.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                main.dispose();
                Samples frame = new Samples();//Opens Samples class
            }
        });

        buttonPanel.add(create);
        buttonPanel.add(samples);

        main.setVisible(true);
    }

    //Creates each JButton
    private JButton createButton(String text, ActionListener listener)
    {
        JButton button = new JButton(text);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);//Puts text under icon in button
        button.setHorizontalTextPosition(SwingConstants.CENTER);//Centers text in button
        button.addActionListener(listener); 
        return button;
    }

    //Adds a new recorded track to the home screen
    public static void addTrack(Track track, int recordedInstrument, int channelNum)
    {
        int trackIndex = recordedTracks.size();
        int finalTrackIndex;
        if (trackIndex > 0 && selectedTrack - 1 < recordedTracks.size())//Check if replacing an existing track
        {
            recordedTracks.set(selectedTrack - 1, track);
            finalTrackIndex = selectedTrack - 1;
        }
        else
        {
            recordedTracks.add(track);
            finalTrackIndex = recordedTracks.size() - 1;
        }
        try
        {
            Sequence sequence = new Sequence(Sequence.PPQ, ((int) recordedTempo * 4));
            Track seqTrack = sequence.createTrack();
            seqTrack.add(createTempoEvent(getRecordedTempo(), 0));//Add initial tempo event at tick 0
            if (channelNum != 9)//Add program change event at tick 0, unless it's a drum channel
            {
                seqTrack.add(createProgramChangeEvent(recordedInstrument, channelNum, 0));
            }
            for (int i = 0; i < track.size(); i++)//Copy all the recorded events into the new track
            {
                MidiEvent event = track.get(i);
                seqTrack.add(event);
            }

            //Create a track button to play this sequence
            JButton trackButton = new JButton("Track " + (finalTrackIndex + 1));
            trackButton.addActionListener(e -> {
                try
                {
                    if (seq != null && seq.isOpen())
                    {
                        seq.stop();
                        seq.close();
                    }
                    seq = MidiSystem.getSequencer(true);
                    seq.open();
                    seq.setSequence(sequence);
                    seq.setTickPosition(0);
                    seq.setTempoInBPM(recordedTempo);
                    seq.start();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            });
            trackPanel.add(trackButton);
            trackPanel.revalidate();
            trackPanel.repaint();
        }
        catch (InvalidMidiDataException e)
        {
            e.printStackTrace();
        }
    }

    //Creates a tempo event at a specific tick
    public static MidiEvent createTempoEvent(float bpm, long tick)
    {
        try
        {
            int tempo = Math.round(60000000 / bpm);
            byte[] data = {
                (byte)((tempo >> 16) & 0xFF),
                (byte)((tempo >> 8) & 0xFF),
                (byte)(tempo & 0xFF)
            };
            MetaMessage tempoMessage = new MetaMessage();
            tempoMessage.setMessage(0x51, data, data.length);
            return new MidiEvent(tempoMessage, tick);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    //Creates a program change event
    public static MidiEvent createProgramChangeEvent(int instrument, int channel, long tick)
    {
        try
        {
            ShortMessage programChange = new ShortMessage();
            programChange.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
            return new MidiEvent(programChange, tick);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == keys)
        {
            instrument.dispose();
            try
            {
                if (selectedTrack != 0)
                {
                    PianoGraphics frame = new PianoGraphics("Piano 1", selectedTrack - 1);//Sets instrument to different MIDI channel so sounds don't overlap
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        else if (e.getSource() == guitars)
        {
            instrument.dispose();
            try
            {
                if (selectedTrack != 0)
                {
                    PianoGraphics frame = new PianoGraphics("Guitar 1", selectedTrack - 1);
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        else if (e.getSource() == synths)
        {
            instrument.dispose();
            try
            {
                if (selectedTrack != 0)
                {
                    PianoGraphics frame = new PianoGraphics("Synth 1", selectedTrack - 1);
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        else if (e.getSource() == drums)
        {
            instrument.dispose();
            try
            {
                DrumGraphics frame = new DrumGraphics();
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        else if (e.getSource() == bass)
        {
            instrument.dispose();
            try
            {
                if (selectedTrack != 0)
                {
                    PianoGraphics frame = new PianoGraphics("Bass 1", selectedTrack - 1);
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        else if (e.getSource() == brass)
        {
            instrument.dispose();
            try
            {
                if (selectedTrack != 0)
                {
                    PianoGraphics frame = new PianoGraphics("Trumpet", selectedTrack - 1);
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        else if (e.getSource() == wind)
        {
            instrument.dispose();
            try
            {
                if (selectedTrack != 0)
                {
                    PianoGraphics frame = new PianoGraphics("Pipe", selectedTrack - 1);
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        else if (e.getSource() == pad)
        {
            instrument.dispose();
            try
            {
                if (selectedTrack != 0)
                {
                    PianoGraphics frame = new PianoGraphics("Pad", selectedTrack - 1);
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        else if (e.getSource() == playButton)
        {
            
        }
        else if (e.getSource() == tempoDown)
        {
            if (recordedTempo > 30 && recordedTempo <= 240)
            {
                recordedTempo = recordedTempo - 1;
                tempoLabel.setText("Tempo: " + recordedTempo);
            }
        }
        else if (e.getSource() == tempoUp)
        {
            if (recordedTempo >= 30 && recordedTempo < 240)
            {
                recordedTempo = recordedTempo + 1;
                tempoLabel.setText("Tempo: " + recordedTempo);
            }
        }
    }

    //Getter for recordedTempo variable
    public static float getRecordedTempo()
    {
        return recordedTempo;
    }
    public static void main(String[] args) throws Exception
    {
        MainScreen home = new MainScreen();
    }
}