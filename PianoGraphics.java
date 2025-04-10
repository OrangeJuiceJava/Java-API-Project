import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import javax.sound.midi.*;
import java.util.*;

public class PianoGraphics extends JPanel
{
    //MidiChannel and all buttons for keyboard
    private static MidiChannel pianoChannel;
    private static int pianoChannelNum;
    private static JButton[] pianoKeys = new JButton[25];//Array to store keys
    private static Color[] colors = new Color[25];//Array to store original colors
    private static boolean[] keyPressed = new boolean[25];//Tracks key states
    private static final int baseOctave = 3;//Defaults to middle C octave (octave 3)
    private static int currentOctave = baseOctave;
    private static JFrame frame;
    private static int programNum;//Determines instrument

    //Stuff for recording
    private Track midiTrack;//Independent stream of MIDI events
    private static Sequence sequence;//Contains musical info
    private static Sequencer sequencer;//Plays back a MIDI sequence
    private long startTime;
    private static ArrayList<Integer> recordedInstruments = new ArrayList<>();
    private static int volume;//Each instrument's volume

    //For playing
    private static final int numKeys = 25;
    private static final int numBeats = 16;
    private static JButton[][] gridKeys = new JButton[numBeats][numKeys];
    private static Timer timer;
    private static int currentStep = numBeats - 1;
    private static int bpm = (int) MainScreen.getRecordedTempo();

    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();
    int screenWidth = (int) screenSize.getWidth();
    int screenHeight = (int) screenSize.getHeight();

    //Define notes for a simple piano range (Two middle octaves)
    private static String[] keyNotes = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G",
        "G#", "A", "A#", "B", "C", "C#", "D", "D#",
        "E", "F", "F#", "G", "G#", "A", "A#", "B", "C"
    };
    private static char[] keyChars = {
        'z', 's', 'x', 'd', 'c', 'v', 'g', 'b', 'h', 'n', 'j', 'm',
        'q', '2', 'w', '3', 'e', 'r', '5', 't', '6', 'y', '7', 'u', 'i'
    };

    public PianoGraphics(String sound, int channelNum) throws Exception
    {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        pianoChannel = synth.getChannels()[channelNum];
        pianoChannelNum = channelNum;

        //Changes sound MIDI channel plays
        if (sound.equals("Piano 1"))
        {
            pianoChannel.programChange(1);
            programNum = 1;
            volume = 80;
        }
        else if (sound.equals("Guitar 1"))
        {
            pianoChannel.programChange(26);
            programNum = 26;
            volume = 100;
        }
        else if (sound.equals("Synth 1"))
        {
            pianoChannel.programChange(81);
            programNum = 81;
            volume = 65;
        }
        else if (sound.equals("Bass 1"))
        {
            pianoChannel.programChange(34);
            programNum = 34;
            volume = 115;
        }
        else if (sound.equals("Trumpet"))
        {
            pianoChannel.programChange(57);
            programNum = 57;
            volume = 65;
        }
        else if (sound.equals("Pipe"))
        {
            pianoChannel.programChange(78);
            programNum = 78;
            volume = 80;
        }
        else if (sound.equals("Pad"))
        {
            pianoChannel.programChange(89);
            programNum = 89;
            volume = 100;
        }

        frame = new JFrame("Piano");
        frame.setSize(screenWidth / 2, screenHeight / 2);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        //Creates a sequence and track
        sequence = new Sequence(Sequence.PPQ, (bpm * 4));//Pulses per quarter note
        midiTrack = sequence.createTrack();
        sequencer = MidiSystem.getSequencer();
        sequencer.open();

        JPanel pianoPanel = new JPanel();
        pianoPanel.setLayout(new GridLayout(1, 25));//25 keys
        frame.add(pianoPanel, BorderLayout.SOUTH);//Panel added to bottom of frame

        //Panel and buttons change octave on piano
        JPanel octavePanel = new JPanel();
        JLabel octaveLabel = new JLabel("Octave" + currentOctave);
        JButton octaveUp = new JButton(">");
        JButton octaveDown = new JButton("<");

        octaveUp.addActionListener(e -> changeOctave(1, octaveLabel));
        octaveDown.addActionListener(e -> changeOctave(-1, octaveLabel));

        //Adds play button to play beat
        JButton playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (timer.isRunning())
                {
                    currentStep = numBeats - 1;//Reset to start of beat when pressed
                    timer.stop();//Stops playing
                    stopTimer();
                    playButton.setText("Play");
                }
                else
                {
                    timer.start();//Starts playing
                    playButton.setText("Stop");
                }
            }
        });

        JButton saveButton = new JButton("Save as track");
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Track keySeq = createKeyTrack(volume);//Creates a track from the grid pattern
                    MainScreen.addTrack(keySeq, programNum, channelNum);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
        octavePanel.add(octaveDown);
        octavePanel.add(octaveLabel);
        octavePanel.add(octaveUp);
        octavePanel.add(playButton);
        octavePanel.add(saveButton);
        frame.add(octavePanel, BorderLayout.NORTH);

        //Make the panel for the piano sequencer
        JPanel gridPanel = new JPanel(new GridLayout(numBeats, numKeys));
        frame.add(gridPanel, BorderLayout.CENTER);

        //Create grid of buttons
        for (int row = 0; row < numBeats; row++)
        {
            for (int col = 0; col < numKeys; col++)
            {
                JButton keyButton = new JButton();
                Color keyColor = Color.LIGHT_GRAY;
                keyButton.setBackground(keyColor);
                keyButton.setForeground(Color.BLACK);
                gridKeys[row][col] = keyButton;
                keyButton.addActionListener(e ->
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
                    frame.requestFocusInWindow();
                });
                gridKeys[row][col] = keyButton;
                gridPanel.add(keyButton);
            }
        }

        //Create piano keys
        for (int i = 0; i < 25; i++)
        {
            JButton keyButton = new JButton();
            keyButton.setPreferredSize(new Dimension(frame.getWidth() / 25, frame.getHeight() / 3));
            //Black Keys
            Color keyColor;
            if ((i % 12 == 1 || i % 12 == 3 || i % 12 == 6 || i % 12 == 8 || i % 12 == 10))
            {
                keyColor = Color.BLACK;
            }
            //White keys
            else
            {
                keyColor = Color.WHITE;
            }
            keyButton.setBackground(keyColor);
            keyButton.setForeground(Color.BLACK);

            //Store key and color in array
            pianoKeys[i] = keyButton;
            colors[i] = keyColor;

            //Add MouseListener for click and release
            final int index = i;
            keyButton.addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent e)
                {
                    playNote(index);
                    keyPressed[index] = true;
                    if (colors[index] == Color.WHITE)
                    {
                        pianoKeys[index].setBackground(Color.LIGHT_GRAY);
                    }
                    else
                    {
                        pianoKeys[index].setBackground(Color.DARK_GRAY);
                    }
                    frame.requestFocusInWindow();//Regains focus on keyboard after clicking
                }

                public void mouseReleased(MouseEvent e)
                {
                    stopNote(index);
                    keyPressed[index] = false;
                    pianoKeys[index].setBackground(colors[index]);
                }
            });
            pianoPanel.add(keyButton);
        }

        int delay = 60000 / (bpm * 2);//Ms per 8th note
        timer = new Timer(delay, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //Reset the colors of the previous column
                int previousStep = (currentStep +1) % numBeats;
                for (int col = 0; col < numKeys; col++)
                {
                    JButton button = gridKeys[previousStep][col];
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
                    stopNote(col);//Stops notes playing after each beat
                }
                for (int col = 0; col < numKeys; col++)//Play the sounds for the current step
                {
                    JButton button = gridKeys[currentStep][col];
                    if (button.getBackground() == Color.DARK_GRAY)//Only play active beats
                    {
                        playNote(col);//Plays dark gray notes beat by beat
                        button.setBackground(Color.WHITE);//Flash White when playing
                    }
                }
                currentStep = (currentStep - 1 + numBeats) % numBeats;
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
                    if (colors[index] == Color.WHITE)
                    {
                        pianoKeys[index].setBackground(Color.LIGHT_GRAY);//Changes color of white keys when note is played
                    }
                    else
                    {
                        pianoKeys[index].setBackground(Color.DARK_GRAY);//Changes color of black keys when note is played
                    }
                }
            }

            public void keyReleased(KeyEvent e)
            {
                int index = keyToIndex(e.getKeyChar());
                if (index != -1)
                {
                    stopNote(index);
                    keyPressed[index] = false;
                    pianoKeys[index].setBackground(colors[index]);//Changes keys to original color
                }
            }
        });
        frame.setFocusable(true);
        frame.setVisible(true);
    }

    //Method to create a track from the grid pattern in PianoGraphics
    public Track createKeyTrack(int volume)
    {
        Track track = sequence.createTrack();
        int stepSize = bpm * 2;
        int i = 0;
        track.add(MainScreen.createTempoEvent(bpm, 0));//Add initial tempo event
        track.add(MainScreen.createProgramChangeEvent(programNum, pianoChannelNum, 0));//Add instrument change at start
        
        for (int row = numBeats - 1; row >= 0; row--)//Loop through each beat and key bottom to top row
        {
            for (int col = 0; col < numKeys; col++)
            {
                if (gridKeys[row][col].getBackground() == Color.DARK_GRAY)//If the key is pressed (Dark Gray indicates active key)
                {
                    int midiNote = getMidiNoteForIndex(col);
                    //Create a new note-on event
                    try
                    {
                        ShortMessage msgOn = new ShortMessage();
                        msgOn.setMessage(ShortMessage.NOTE_ON, pianoChannelNum, midiNote, volume);
                        MidiEvent noteOnEvent = new MidiEvent(msgOn, i * stepSize);
                        track.add(noteOnEvent);

                        ShortMessage msgOff = new ShortMessage();
                        msgOff.setMessage(ShortMessage.NOTE_OFF, pianoChannelNum, midiNote, volume);
                        MidiEvent noteOffEvent = new MidiEvent(msgOff, (i * stepSize) + stepSize);
                        track.add(noteOffEvent);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            i++;
        }
        return track;
    }

    private void stopTimer()
    {
        if (timer != null && timer.isRunning())
        {
            timer.stop();
        }
        for (int row = 0; row < numBeats; row++)//Reset all buttons and stop all sounds
        {
            for (int col = 0; col < numKeys; col++)
            {
                JButton button = gridKeys[row][col];
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
        for (int i = 0; i < numKeys; i++)//Also stop all notes
        {
            stopNote(i);
        }
    }

    //Play MIDI note
    public static void playNote(int index)
    {
        int midiNote = getMidiNoteForIndex(index);
        if (midiNote >= 0 && midiNote <= 127)
        {
            pianoChannel.noteOn(midiNote, 800);
        }
    }

    //Stop playing MIDI note
    public static void stopNote(int index)
    {
        int midiNote = getMidiNoteForIndex(index);
        if (midiNote >= 0 && midiNote <= 127)
        {
            pianoChannel.noteOff(midiNote);
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

    //Convert note name to MIDI number
    public static int noteToMidi(String note)
    {
        switch (note)
        {
            case "C": return 0;
            case "C#": return 1;
            case "D": return 2;
            case "D#": return 3;
            case "E": return 4;
            case "F": return 5;
            case "F#": return 6;
            case "G": return 7;
            case "G#": return 8;
            case "A": return 9;
            case "A#": return 10;
            case "B": return 11;
            default: return -1;
        }
    }

    //Finds right MIDI note octave
    public static int getMidiNoteForIndex(int index)
    {
        int baseNote = noteToMidi(keyNotes[index % 12]);
        int octaveShift = index / 12;//0 for 0-11, 1 for 12-23
        return (currentOctave + octaveShift + 1) * 12 + baseNote;//+1 because MIDI note 0 is C-1
    }

    //Changes octave
    public static void changeOctave(int change, JLabel octaveLabel)
    {
        currentOctave = Math.max(1, Math.min(5, currentOctave + change));//Limit 1-5
        octaveLabel.setText("Octave: " + currentOctave);
        frame.requestFocusInWindow();
    }

    //Returns instrument for a specific track based on its index
    public static int getRecordedInstrumentForTrack(int trackIndex)
    {
        //Ensure the track index is valid
        if (trackIndex >= 0 && trackIndex < recordedInstruments.size())
        {
            return recordedInstruments.get(trackIndex);//Return the instrument for the given track
        }
        else
        {
            return 0;//Default instrument if trackIndex is invalid
        }
    }
}