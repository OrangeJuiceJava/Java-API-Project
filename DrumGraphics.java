import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.midi.*;
import java.util.concurrent.*;

public class DrumGraphics
{
    private static MidiChannel drumChannel;
    private static JButton[][] drums = new JButton[11][32];//11 sounds, 32 beats
    private static Color[][] colors = new Color[11][32];//Array holds each original color of each drum
    private static boolean[] keyPressed = new boolean[11];
    private JLabel kick1, kick2, stick, snare1, clap, snare2, cHiHat, lTom, oHiHat, hTom, cuica;

    //For timing beats
    private static Timer timer;
    private int currentStep = 0;
    private int numRows = 11;
    private int numCols = 32;
    private static int bpm = (int) MainScreen.getRecordedTempo();

    //Defines percussion
    private static String[] perc = {
        "Kick 1", "Kick 2", "Stick", "Snare 1", "Clap", "Snare 2",
        "C Hi-Hat", "Low Tom", "O Hi-Hat", "Hi Tom", "O Cuica",
    };
    private static char[] keyChars = {
        'z', 'a', 'q', 'x', 'w', 's', 'c', 'v', 'd', 'f', 'e'
    };
    public DrumGraphics() throws Exception
    {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        drumChannel = synth.getChannels()[9];

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        JFrame frame = new JFrame("Drum Kit");
        frame.setSize(screenWidth / 2, screenHeight / 2);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new GridLayout(11, 1, screenWidth / 100, screenHeight / 100));
        frame.add(labelPanel, BorderLayout.WEST);

        kick1 = new JLabel("Kick 1");
        kick2 = new JLabel("Kick 2");
        stick = new JLabel("Stick");
        snare1 = new JLabel("Snare 1");
        clap = new JLabel("Clap");
        snare2 = new JLabel("Snare 2");
        cHiHat = new JLabel("Close HiHat");
        lTom = new JLabel("Low Tom");
        oHiHat = new JLabel("Open HiHat");
        hTom = new JLabel("High Tom");
        cuica = new JLabel("Cuica");

        labelPanel.add(kick1);
        labelPanel.add(kick2);
        labelPanel.add(stick);
        labelPanel.add(snare1);
        labelPanel.add(clap);
        labelPanel.add(snare2);
        labelPanel.add(cHiHat);
        labelPanel.add(lTom);
        labelPanel.add(oHiHat);
        labelPanel.add(hTom);
        labelPanel.add(cuica);

        JPanel playSavePanel = new JPanel();
        playSavePanel.setLayout(new FlowLayout());
        frame.add(playSavePanel, BorderLayout.NORTH);

        //Adds play button to play drum beat
        JButton playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (timer.isRunning())
                {
                    currentStep = 0;//Reset to start of beat when pressed
                    timer.stop();
                    stopTimer();
                    playButton.setText("Play");
                }
                else
                {
                    timer.start();
                    playButton.setText("Stop");
                }
            }
        });

        //Adds a save button which saves the pattern as a track in MainScreen
        JButton saveButton = new JButton("Save as track");
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Track drumTrack = createDrumTrack();
                    MainScreen.addTrack(drumTrack, 0, 9);//Adds drum track to MainScreen
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
        playSavePanel.add(playButton);
        playSavePanel.add(saveButton);

        JPanel drumPanel = new JPanel();
        drumPanel.setLayout(new GridLayout(11, 1));//11 keys
        frame.add(drumPanel);

        //Create drum set
        for (int i = 0; i < 11; i++)
        {
            JPanel rowPanel = new JPanel(new GridLayout(1, 32));//Each sound gets a row of 16 buttons
            for (int j = 0; j < 32; j++)
            {
                JButton keyButton = new JButton();
                Color keyColor = Color.LIGHT_GRAY;
                keyButton.setBackground(keyColor);
                keyButton.setForeground(Color.BLACK);
                drums[i][j] = keyButton;
        
                final int soundIndex = i;
                final int beatIndex = j;
        
                keyButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        //Toggle the button ON or OFF
                        if (keyButton.getBackground() == Color.LIGHT_GRAY)
                        {
                            keyButton.setBackground(Color.DARK_GRAY);//Active beat
                        }
                        else
                        {
                            keyButton.setBackground(Color.LIGHT_GRAY);//Inactive beat
                        }
                    }
                });
                rowPanel.add(keyButton);
            }
            drumPanel.add(rowPanel);//Add the row to the panel
        }

        int delay = 60000 / (bpm * 4);//Ms per 8th note

        timer = new Timer(delay, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //Reset the colors of the previous column
                int previousStep = (currentStep - 1 + numCols) % numCols;
                for (int row = 0; row < numRows; row++)
                {
                    JButton button = drums[row][previousStep];
                    if (button.getBackground() == Color.WHITE)//Only reset if it flashed white
                    {
                        //If the beat is active, set it back to DARK_GRAY, otherwise LIGHT_GRAY
                        if (button.isEnabled() && button.getBackground() != Color.LIGHT_GRAY)
                        {
                            button.setBackground(Color.DARK_GRAY);
                        }
                        else
                        {
                            button.setBackground(Color.LIGHT_GRAY);
                        }
                    }
                }
                for (int row = 0; row < numRows; row++)//Play the sounds for the current step
                {
                    JButton button = drums[row][currentStep];
                    if (button.getBackground() == Color.DARK_GRAY)//Only play active beats
                    {
                        playNote(row);
                        button.setBackground(Color.WHITE);//Flash White when playing
                    }
                }
                currentStep = (currentStep + 1) % numCols;
            }
        });

        //Add key press listeners for keyboard input
        frame.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                int index = keyToIndex(e.getKeyChar());
                if (index != -1 && !keyPressed[index])
                {
                    playNote(index);
                    keyPressed[index] = true;
                    int row = index / numCols;//find row
                    int col = index % numCols;//find column
                    drums[row][col].setBackground(Color.GRAY);
                }
            }

            public void keyReleased(KeyEvent e)
            {
                int index = keyToIndex(e.getKeyChar());
                if (index != -1)
                {
                    stopNote(index);
                    keyPressed[index] = false;
                    int row = index / numCols;
                    int col = index % numCols;
                    drums[row][col].setBackground(colors[row][col]);
                }
            }
        });
        frame.setFocusable(true);
        frame.setVisible(true);
    }

    //Stops beat
    private void stopTimer()
    {
        if (timer != null && timer.isRunning())
        {
            timer.stop();
        }
        for (int row = 0; row < numRows; row++)//Reset all buttons and stop all sounds
        {
            for (int col = 0; col < numCols; col++)
            {
                JButton button = drums[row][col];
                if (button.getBackground() == Color.WHITE)//If the button was flashing (white), reset its color
                {
                    if (button.isEnabled())
                    {
                        button.setBackground(Color.DARK_GRAY);
                    }
                    else
                    {
                        button.setBackground(Color.LIGHT_GRAY);
                    }
                }
            }
        }
        for (int i = 0; i < numRows; i++)//Stop all notes
        {
            stopNote(i);
        }
    }

    //Turns drum grid into a playable music sequence
    public Track createDrumTrack() throws Exception
    {
        Track track = new Sequence(Sequence.PPQ, (bpm * 4)).createTrack();//Create a new track
        int ppq = bpm * 4;
        int stepSize = ppq / 4;
        long  currentTick = 0;
        track.add(MainScreen.createTempoEvent(bpm, 0));//Add initial tempo event (use your tempo or bpm value here)
        track.add(MainScreen.createProgramChangeEvent(0, 9, 0));//Add instrument change

        for (int i = 0; i < drums.length; i++)//Iterate over the drum grid and add events
        {
            for (int j = 0; j < drums[i].length; j++)
            {
                JButton button = drums[i][j];
                if (button.getBackground() == Color.DARK_GRAY)//If button has been clicked
                {
                    int drumNote = getMidiGridNote(i);//Map row to drum sound
                    //Add the first note-on event
                    ShortMessage msgOn = new ShortMessage();
                    msgOn.setMessage(ShortMessage.NOTE_ON, 9, drumNote, 100);
                    MidiEvent noteOnEvent = new MidiEvent(msgOn, currentTick);
                    track.add(noteOnEvent);

                    //Add the note-off event
                    ShortMessage msgOff = new ShortMessage();
                    msgOff.setMessage(ShortMessage.NOTE_OFF, 9, drumNote, 100);
                    MidiEvent noteOffEvent = new MidiEvent(msgOff, currentTick + stepSize);
                    track.add(noteOffEvent);
                    /*track.add(makeMidiEvent(ShortMessage.NOTE_ON, 9, drumNote, 100, currentTick));//NOTE_ON event for the drum sound
                    track.add(makeMidiEvent(ShortMessage.NOTE_OFF, 9, drumNote, 100, currentTick + stepSize));//NOTE_OFF event for the drum sound*/
                }
                currentTick += stepSize;
            }
        }
        return track;
    }

    //Play MIDI note
    public static void playNote(int index)
    {
        int midiNote = noteToMidi(perc[index]);
        if (midiNote != -1)
        {
            drumChannel.noteOn(midiNote, 800);
        }
    }

    //Stop playing MIDI note
    public static void stopNote(int index)
    {
        int midiNote = noteToMidi(perc[index]);
        if (midiNote != -1)
        {
            drumChannel.noteOff(midiNote);
        }
    }

    //Convert keyboard key to index
    private static int keyToIndex(char keyChar)
    {
        for (int i = 0; i < keyChars.length; i++)
        {
            if (keyChars[i] == keyChar)
            {
                return i;
            }
        }
        return -1;
    }

    //Builds a simple MIDI event like note on/note off
    public MidiEvent makeMidiEvent(int command, int channel, int note, int velocity, int tick)
    {
        MidiEvent event = null;
        try
        {
            ShortMessage message = new ShortMessage();
            message.setMessage(command, channel, note, velocity);
            event = new MidiEvent(message, tick);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return event;
    }

    //Convert note name to MIDI number
    public static int noteToMidi(String note)
    {
        switch (note)
        {
            case "Kick 1": return 35;
            case "Kick 2": return 36;
            case "Stick":  return 37;
            case "Snare 1": return 38;
            case "Clap": return 39;
            case "Snare 2": return 40;
            case "C Hi-Hat": return 42;
            case "Low Tom": return 45;
            case "O Hi-Hat": return 46;
            case "Hi Tom": return 50;
            case "O Cuica": return 79;
            default: return -1;
        }
    }

    //Converts a row number into a MIDI note
    public int getMidiGridNote(int index)
    {
        int[] drumNotes = {35, 36, 37, 38, 39, 40, 42, 45, 46, 50, 79};
        if (index >= 0 && index < drumNotes.length)
        {
            return drumNotes[index];
        }
        else
        {
            return 35;//Default to kick 1 if index out of bounds
        }
    }
}