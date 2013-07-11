package gui;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 3/28/13
 * Time: 1:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressVisualizer implements PropertyChangeListener {

    public void setProgress(final int progress) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue(progress);
            }


        });

    }

    public enum DeterminateType{
        DETERMINATE,INDETERMINATE
    }

    private JFrame progressFrame;
    private JTextArea taskOutput;
    private double stability;
    private JProgressBar progressBar;
    private static Point currentLocation = new Point(25, 25);

    public ProgressVisualizer(final String title, final DeterminateType type) {
        SwingUtilities.invokeLater(new Runnable() {


            @Override
            public void run() {
                progressBar = new JProgressBar(0, 100);
                if(type==DeterminateType.DETERMINATE){
                    progressBar.setValue(0);
                    progressBar.setStringPainted(true);
                }else{
                    progressBar.setIndeterminate(true);

                }

                taskOutput = new JTextArea(5, 20);
                taskOutput.setMargin(new Insets(5, 5, 5, 5));
                taskOutput.setEditable(false);
                DefaultCaret caret = (DefaultCaret) taskOutput.getCaret();
                caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                progressFrame = new JFrame(title);
                progressFrame.setLayout(new BorderLayout());
                progressFrame.add(progressBar, BorderLayout.NORTH);
                progressFrame.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
                progressFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();




                progressFrame.setSize(400, 200);

                synchronized (currentLocation){
                    currentLocation = new Point((int) Math.floor((currentLocation.x+10) %(screenSize.getWidth()-progressFrame.getWidth())), (int) Math.floor((currentLocation.y+50) %(screenSize.getHeight()-progressFrame.getHeight())));
                }
                progressFrame.setLocation(currentLocation);
                progressFrame.setVisible(true);
            }
        });
    }


    public void finish() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                progressFrame.dispose();
            }
        });
    }

    public void displayStability(int count) {
        NumberFormat doubleFormat = new DecimalFormat("##.00000000");
        final String lastStabilityValue = doubleFormat.format(stability);
        NumberFormat integerFormat = new DecimalFormat("0000");
        final String lastCount = integerFormat.format(count);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {


                taskOutput.append((lastCount) + " : " + lastStabilityValue + "\n");
                progressFrame.revalidate();
            }
        });
    }

    public void setStability(double stability) {
        this.stability = stability;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("progress".equals(event.getPropertyName())) {
            final int progress = (Integer) event.getNewValue();
//            progressBar.setIndeterminate(false);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    progressBar.setValue(progress);
                }


            });
        }
    }

    public void print(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                taskOutput.append(s);
                taskOutput.validate();
            }
        });
    }
}
