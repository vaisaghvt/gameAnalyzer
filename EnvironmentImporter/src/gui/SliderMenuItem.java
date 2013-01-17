package gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/10/13
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
class SliderMenuItem extends JSlider implements MenuElement {

    public SliderMenuItem(String label, int min, int max, int value) {
        super(min, max, value);
        setBorder(new CompoundBorder(new TitledBorder(label),
                new EmptyBorder(10, 10, 10, 10)));

        setMajorTickSpacing((max - min) / 4);
        setMinorTickSpacing(1);
        setSnapToTicks(true);
        setLabelTable(createStandardLabels(min, 1));
        setPaintLabels(true);
        setPaintTicks(true);

    }

    @Override
    public void processMouseEvent(MouseEvent e, MenuElement path[],
                                  MenuSelectionManager manager) {

    }


    public void processKeyEvent(KeyEvent e, MenuElement path[],
                                MenuSelectionManager manager) {
    }


    public void menuSelectionChanged(boolean isIncluded) {
    }

    public MenuElement[] getSubElements() {
        return new MenuElement[0];
    }

    public Component getComponent() {
        return this;
    }


}