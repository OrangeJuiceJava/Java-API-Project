import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.*;

public class Samples extends JPanel implements ActionListener
{
    private static Clip sample;
    private static Clip currentClip;
    private static JFrame frame;
    private JPanel samplePanel;
    private JButton sample1, sample2, sample3, sample4;//One button for each sample to play
    private static boolean isPlaying = false;

    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();
    int screenWidth = (int) screenSize.getWidth();
    int screenHeight = (int) screenSize.getHeight();

    public Samples()
    {
        frame = new JFrame("Samples");
        frame.setSize(screenWidth / 3, screenHeight / 3);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        samplePanel = new JPanel();
        samplePanel.setLayout(new GridLayout(1, 4));
        frame.add(samplePanel);

        sample1 = createButton("Sample 1", this);
        sample2 = createButton("Sample 2", this);
        sample3 = createButton("Sample 3", this);
        sample4 = createButton("Sample 4", this);

        samplePanel.add(sample1);
        samplePanel.add(sample2);
        samplePanel.add(sample3);
        samplePanel.add(sample4);

        frame.setVisible(true);
    }

    private JButton createButton(String text, ActionListener listener)
    {
        JButton button = new JButton(text);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.addActionListener(listener); 
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e)//Plays the sample based on what button is pressed
    {
        if (e.getSource() == sample1)
        {
            if (currentClip != null && currentClip.isRunning())//If a sample is currently playing
            {
                currentClip.stop();//Stops playing sample
                currentClip.setFramePosition(0);//Rewinds sample to beginning
            }
            try
            {
                AudioInputStream song = AudioSystem.getAudioInputStream(new File("Sample-1.wav"));//Gets the wav file
                sample = AudioSystem.getClip();

                if (!isPlaying)//If not playing currently, starts playing
                {
                    isPlaying = true;
                    sample1.setText("Playing...");
                    sample.open(song);//Starts playing sample
                    sample.loop(Clip.LOOP_CONTINUOUSLY);//Loops forever
                    sample.start();//Starts playing sample
                    currentClip = sample;
                }
                else//If currently playing, stops playing
                {
                    isPlaying = false;
                    sample1.setText("Sample 1");
                    sample.close();
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else if (e.getSource() == sample2)
        {
            if (currentClip != null && currentClip.isRunning())
            {
                currentClip.stop();
                currentClip.setFramePosition(0);
            }
            try
            {
                AudioInputStream song = AudioSystem.getAudioInputStream(new File("Sample-2.wav"));//Gets the wav file
                sample = AudioSystem.getClip();

                if (!isPlaying)//If not playing currently, starts playing
                {
                    isPlaying = true;
                    sample2.setText("Playing...");
                    sample.open(song);
                    sample.loop(Clip.LOOP_CONTINUOUSLY);//Loops forever
                    sample.start();
                    currentClip = sample;
                }
                else//If currently playing, stops playing
                {
                    isPlaying = false;
                    sample2.setText("Sample 2");
                    sample.close();
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else if (e.getSource() == sample3)
        {
            if (currentClip != null && currentClip.isRunning())
            {
                currentClip.stop();
                currentClip.setFramePosition(0);
            }
            try
            {
                AudioInputStream song = AudioSystem.getAudioInputStream(new File("Sample-3.wav"));//Gets the wav file
                sample = AudioSystem.getClip();

                if (!isPlaying)//If not playing currently, starts playing
                {
                    isPlaying = true;
                    sample3.setText("Playing...");
                    sample.open(song);//Starts playing sample
                    sample.loop(Clip.LOOP_CONTINUOUSLY);//Loops forever
                    sample.start();
                    currentClip = sample;
                }
                else//If currently playing, stops playing
                {
                    isPlaying = false;
                    sample3.setText("Sample 3");
                    sample.close();
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else if (e.getSource() == sample4)
        {
            if (currentClip != null && currentClip.isRunning())
            {
                currentClip.stop();
                currentClip.setFramePosition(0);
            }
            try
            {
                AudioInputStream song = AudioSystem.getAudioInputStream(new File("Sample-4.wav"));//Gets the wav file
                sample = AudioSystem.getClip();

                if (!isPlaying)//If not playing currently, starts playing
                {
                    isPlaying = true;
                    sample4.setText("Playing...");
                    sample.open(song);//Starts playing sample
                    sample.loop(Clip.LOOP_CONTINUOUSLY);//Loops forever
                    sample.start();
                    currentClip = sample;
                }
                else//If currently playing, stops playing
                {
                    isPlaying = false;
                    sample4.setText("Sample 4");
                    sample.close();
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
