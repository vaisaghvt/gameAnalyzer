package gui;

import database.Database;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: vaisagh
 * Date: 1/8/13
 * Time: 4:10 PM
 * <p/>
 * This is the main frame and panel.
 */
public class Main extends JFrame implements ActionListener, MouseListener, MouseMotionListener, ChangeListener {
    private static final long serialVersionUID = 1L;

    private static final String APPLICATION_TITLE = "Environment to Network converter";

    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu mFile = new JMenu("File");
    private final JMenuItem miNew = new JMenuItem("New...");
    private final JMenuItem miOpen = new JMenuItem("Open...");
    private final JMenuItem miSave = new JMenuItem("Save");

    private final JMenuItem miClose = new JMenuItem("Close");
    private final JMenuItem miExit = new JMenuItem("Exit");

    private final JMenu mView = new JMenu("View");
    private final JMenuItem miViewImage = new JCheckBoxMenuItem("View Image");
    private final JMenuItem miViewRooms = new JCheckBoxMenuItem("View Rooms");
    private final JMenuItem miNetworkView = new JRadioButtonMenuItem("Network View");
    private final JMenuItem miRoomView = new JRadioButtonMenuItem("Room View");
    private static final int MIN_ZOOM = 1;
    private static final int MAX_ZOOM = 10;
    private static final int DEFAULT_ZOOM = 5;
    private final SliderMenuItem miZoom = new SliderMenuItem("Zoom", MIN_ZOOM, MAX_ZOOM, DEFAULT_ZOOM);
    private final JMenu mViewDataFor = new JMenu("Choose dataname...");

    private MainPanel mainPanel = null;


    private final JPopupMenu popupMenu = new JPopupMenu();

    private final JMenu mSwitchToFloor = new JMenu("Choose Floor...");

    private final JMenuItem miCreateGroup = new JMenuItem("Group");
    private final JMenuItem miRemoveGroup = new JMenuItem("UnGroup");
    private final JMenuItem miAddRoom = new JMenuItem("Add Room");
    private final JMenuItem miAddLink = new JMenuItem("Add Link");
    private final JMenuItem miAddStaircase = new JMenuItem("Add Staircase");
    private final JMenuItem miEditStaircaseGroup = new JMenuItem("Edit Staircase Group...");
    private final JMenuItem miRemoveSelection = new JMenuItem("Remove Selected Objects");
    private final JMenuItem miRenameArea = new JMenuItem("Rename Area");
    private Set<JRadioButtonMenuItem> floorItems = null;

    //	private Integer buttonPressed = null;
    private Integer buttonReleased = null;
    private Integer actualX1 = null;
    private Integer actualY1 = null;
    private Integer actualX2 = null;
    private Integer actualY2 = null;
    private Integer drawnX1 = null;
    private Integer drawnY1 = null;
    private Integer drawnX2 = null;
    private Integer drawnY2 = null;
//	private boolean isMoving = false;

    private Document current = null;


    private Integer hoveringOverRoomId = null;
    private Integer hoveringOverStaircaseId = null;
    private Set<JRadioButtonMenuItem> dataNameItems = null;

    private Main() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.mainPanel = MapImagePanel.instance();
        this.mainPanel.addMouseListener(this);
        this.mainPanel.addMouseMotionListener(this);

        this.createMainMenu();
        this.createContextMenu();
        this.update();

        this.getContentPane().setLayout(new BorderLayout());


        this.getContentPane().add(mainPanel, BorderLayout.CENTER);


        this.setSize(800, 600);
    }

    private void update() {
        if (current != null) {
            this.setTitle(current.name() + " - " + APPLICATION_TITLE);

            miSave.setEnabled(true);
            miClose.setEnabled(true);
            mView.setEnabled(true);
            mViewDataFor.setEnabled(false);

            mainPanel.setDocument(current);

            mainPanel.revalidate();
        } else {
            this.setTitle(APPLICATION_TITLE);
            miSave.setEnabled(false);
            miClose.setEnabled(false);
            mView.setEnabled(false);

            mainPanel.setDocument(null);

            mainPanel.setPreferredSize(new Dimension(0, 0));
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }

    private void createMainMenu() {
        //main menu
        menuBar.removeAll();
        menuBar.add(mFile);
        menuBar.add(mView);

        mFile.add(miNew);
        mFile.add(miOpen);
        mFile.add(miSave);
        mFile.add(miClose);
        mFile.add(miExit);

        ButtonGroup radioGroup = new ButtonGroup();


        this.miNetworkView.setSelected(false);
        radioGroup.add(miNetworkView);
        mView.add(miNetworkView);


        this.miRoomView.setSelected(true);
        radioGroup.add(miRoomView);
        mView.add(miRoomView);

        miViewImage.setSelected(true);
        miViewRooms.setSelected(true);

        mView.addSeparator();
        mView.add(this.miViewImage);
        mView.add(this.miViewRooms);

        mView.addSeparator();
        mView.add(this.miZoom);

        mView.addSeparator();
        mView.add(this.mViewDataFor);


        miNew.addActionListener(this);
        miOpen.addActionListener(this);
        miSave.addActionListener(this);
        miClose.addActionListener(this);
        miExit.addActionListener(this);
        miNetworkView.addActionListener(this);
        miRoomView.addActionListener(this);
        miViewImage.addActionListener(this);
        miViewRooms.addActionListener(this);
        miZoom.addChangeListener(this);

        this.setJMenuBar(menuBar);
    }

    private void createContextMenu() {
        //context menu
        popupMenu.removeAll();


        popupMenu.add(mSwitchToFloor);
        popupMenu.add(miAddRoom);
        popupMenu.add(miAddLink);
        popupMenu.add(miAddStaircase);
        popupMenu.add(miEditStaircaseGroup);
        popupMenu.add(miRemoveSelection);
        popupMenu.add(miRenameArea);
        popupMenu.add(miCreateGroup);
        popupMenu.add(miRemoveGroup);

        miAddRoom.addActionListener(this);
        miAddLink.addActionListener(this);

        miAddStaircase.addActionListener(this);
        miEditStaircaseGroup.addActionListener(this);
        miRemoveSelection.addActionListener(this);
        miRenameArea.addActionListener(this);
        miCreateGroup.addActionListener(this);
        miRemoveGroup.addActionListener(this);

//		popupMenu.addMouseListener(this);
    }

    private void showPopupMenu(MouseEvent event) {
        //is it a point click?
        boolean isPointClick = drawnX1.equals(drawnX2) && drawnY1.equals(drawnY2);


        miEditStaircaseGroup.setEnabled(false);


        if (isPointClick) {
            miAddRoom.setEnabled(false);
            miAddLink.setEnabled(false);
            miAddStaircase.setEnabled(false);
            if (current.selected() != null && !current.selected().isEmpty()) {
                miRemoveSelection.setEnabled(true);
                miRenameArea.setEnabled(true);
            }
            if (current.selected().size() > 1) {
                miRenameArea.setEnabled(false);
            }
            if (current.isGroupCreatePossible()) {
                miCreateGroup.setEnabled(true);
            } else {
                miCreateGroup.setEnabled(false);
            }
            if (current.isGroupRemovePossible()) {
                miRemoveGroup.setEnabled(true);
            } else {
                miRemoveGroup.setEnabled(false);
            }
            if (this.hoveringOverStaircaseId != null) miEditStaircaseGroup.setEnabled(true);
        } else{
            miAddRoom.setEnabled(true);
            miAddLink.setEnabled(true);
            miAddStaircase.setEnabled(true);
            miRemoveSelection.setEnabled(false);
            miRenameArea.setEnabled(false);
            miCreateGroup.setEnabled(false);
            miRemoveGroup.setEnabled(false);
        }

        //update the floors
        mSwitchToFloor.removeAll();
        floorItems = null;
        ButtonGroup group = new ButtonGroup();
        if (Database.getInstance().getNumberOfFloors() > 0)

        {
            floorItems = new HashSet<JRadioButtonMenuItem>();
            for (int i = 0; i < current.numberOfFloors(); ++i) {
                JRadioButtonMenuItem item = new JRadioButtonMenuItem("Floor #" + i);
                item.addActionListener(this);
                mSwitchToFloor.add(item);

                floorItems.add(item);
                group.add(item);
            }
            mSwitchToFloor.setEnabled(true);

        } else

        {
            mSwitchToFloor.setEnabled(false);
        }


        popupMenu.show(event.getComponent(), drawnX2, drawnY2);
    }

    private boolean handleUnsavedChanges() {
        Object[] options = {"Yes", "No", "Cancel"};

        int result = JOptionPane.showOptionDialog(this,
                "'" + current.name() + "' has been modified. Save changes?",
                "Save Document",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);

        if (result == 0) {
            //yes
            this.tryToSave();
            return true;
        } else if (result == 1) {
            //no
            return true;
        } else {
            //cancel
            return false;
        }
    }

    private void tryToSave() {
        try {
            assert (current != null);
            current.save();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "ERROR: the document could not be saved!", "An Error Occured", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private boolean tryToClose() {
        assert (current != null);

        if (current.hasUnsavedChanges()) {
            if (!this.handleUnsavedChanges()) {
                return false;
            }
        }

        current = null;
        this.update();

        return true;
    }

    private void tryToCreateNew() {
        //check whether a document is already open and, if so, try to close it
        if (current != null) {
            if (!this.tryToClose()) return;
        }

        //enter a name for this scenario
        String name = (String) JOptionPane.showInputDialog(
                this,
                "Name of the scenario",
                "Next",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (name == null) return;


        //select a directory name
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select scenario file destination");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showDialog(this, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File dir = chooser.getSelectedFile();

                String path = dir.getAbsolutePath();
                String filename = path + File.separator + (name.replaceAll(" ", "_")) + ".xml";

                current = new Document(filename, name, path, Database.getInstance().getNumberOfFloors());
                current.save();

                this.update();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "ERROR: the document could not be saved!", "An Error Occured", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void tryToOpen() {
        //check whether a document is already open and, if so, try to close it
        if (current != null) {
            if (!this.tryToClose()) return;
        }

        //open an existing file...
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select model file");

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                current = new Document(file);
                this.update();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "ERROR: the file '" + file.getName() + "' could not be opened!", "An Error Occured", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    void invalidateImagePanel() {
        mainPanel.repaint();
        mainPanel.revalidate();
    }


    public void actionPerformed(ActionEvent event) {

        if (event.getSource() == miNew) {
            this.tryToCreateNew();
        } else if (event.getSource() == miOpen) {
            this.tryToOpen();
        } else if (event.getSource() == miSave) {
            this.tryToSave();
        } else if (event.getSource() == miClose) {
            assert (current != null);

            this.tryToClose();
        } else if (event.getSource() == miExit) {
            if (current != null && current.hasUnsavedChanges()) {
                if (this.handleUnsavedChanges()) {
                    System.exit(0);
                }
            } else {
                System.exit(0);
            }
        } else if (event.getSource() == miNetworkView) {
            if (miNetworkView.isSelected()) {
                assert miNetworkView.isSelected() && !miRoomView.isSelected();
                if (!(mainPanel instanceof NetworkModel) && current != null) {
                    changePanel("network");


                }


            }

        } else if (event.getSource() == miRoomView) {
            if (miRoomView.isSelected()) {

                assert !miNetworkView.isSelected() && miRoomView.isSelected();
                if (!(mainPanel instanceof MapImagePanel) && current != null) {
                    changePanel("map");

                }
            }
        } else if (event.getSource() == miViewImage) {
            assert mainPanel instanceof MapImagePanel;
            if (miViewImage.isSelected()) {
                ((MapImagePanel) mainPanel).enableImageView();
            } else {
                ((MapImagePanel) mainPanel).disableImageView();
            }
            invalidateImagePanel();
        } else if (event.getSource() == miViewRooms) {
            assert mainPanel instanceof MapImagePanel;
            if (miViewRooms.isSelected()) {
                ((MapImagePanel) mainPanel).enableRoomView();

            } else {
                ((MapImagePanel) mainPanel).disableRoomView();
            }
            invalidateImagePanel();
        } else if (dataNameItems != null && dataNameItems.contains(event.getSource())) {
            JMenuItem item = (JMenuItem) event.getSource();


            ((NetworkModel) mainPanel).setDisplay(item.getText());


            this.invalidateImagePanel();

        } else {
            assert mainPanel instanceof MapImagePanel;
            Point actualPoint = MapImagePanel.convertToActualCoordinate(new Point(drawnX1, drawnY1), current.getCurrentFloor());
            actualX1 = actualPoint.x;
            actualY1 = actualPoint.y;
            actualPoint = MapImagePanel.convertToActualCoordinate(new Point(drawnX2, drawnY2), current.getCurrentFloor());
            actualX2 = actualPoint.x;
            actualY2 = actualPoint.y;
            if (event.getSource() == miAddRoom) {
                current.addRoom(actualX1, actualY1, actualX2, actualY2);
                unsetMouseSelection();
                this.invalidateImagePanel();
            } else if (event.getSource() == miAddLink) {
                current.addLink(actualX1, actualY1, actualX2, actualY2);
                unsetMouseSelection();
                this.invalidateImagePanel();
            } else if (event.getSource() == miAddStaircase) {
                current.addStaircase(actualX1, actualY1, actualX2, actualY2);
                unsetMouseSelection();
                this.invalidateImagePanel();
            } else if (event.getSource() == miEditStaircaseGroup) {
                ObjectPanelStaircaseGroup dialog = new ObjectPanelStaircaseGroup(current);
                dialog.setVisible(true);

            } else if (event.getSource() == miRemoveSelection) {
                current.removeSelected();
                unsetMouseSelection();
                this.invalidateImagePanel();
            } else if (event.getSource() == miCreateGroup) {
                String newName = JOptionPane.showInternalInputDialog(
                        this.getContentPane(),
                        "What's the name of this group?",
                        "Name group",
                        JOptionPane.QUESTION_MESSAGE);

                current.createGroup(newName);
                this.invalidateImagePanel();
            } else if (event.getSource() == miRemoveGroup) {
                current.removeGroup();
                this.invalidateImagePanel();
            } else if (event.getSource() == miRenameArea) {
                if (current.selected() != null && !current.selected().isEmpty() && current.selected().size() == 1) {
                    String newName = JOptionPane.showInternalInputDialog(
                            this.getContentPane(),
                            "New name for room?",
                            "Rename " + current.selected().iterator().next().getName(),
                            JOptionPane.QUESTION_MESSAGE);
                    current.renameSelected(newName);
                    unsetMouseSelection();
                    this.invalidateImagePanel();
                }
            } else if (floorItems != null && floorItems.contains(event.getSource())) {
                JMenuItem item = (JMenuItem) event.getSource();
                String t1 = item.getText();
                String t2 = t1.substring(t1.indexOf("#") + 1);
                int idx = Integer.parseInt(t2);
                if (current != null) {
                    current.selectFloor(idx);
                }
                ((MapImagePanel) mainPanel).selectFloor(idx);
                unsetMouseSelection();

                this.invalidateImagePanel();

            }
        }
    }

    private void changePanel(String type) {

        this.getContentPane().remove(mainPanel);
        if (type.equals("network")) {
            mainPanel = NetworkModel.instance();
            mainPanel.setDocument(current);
            invalidateImagePanel();
            miViewImage.setEnabled(false);
            miViewRooms.setEnabled(false);
            miZoom.setEnabled(false);

            //update the floors
            mViewDataFor.removeAll();


            dataNameItems = null;


            ButtonGroup radioGroup = new ButtonGroup();
            dataNameItems = new HashSet<JRadioButtonMenuItem>();
            JRadioButtonMenuItem item = new JRadioButtonMenuItem("default");
            dataNameItems.add(item);
            item.addActionListener(this);
            mViewDataFor.add(item);
            dataNameItems.add(item);
            radioGroup.add(item);
            item.setSelected(true);

            Collection<String> dataNames = Database.getInstance().getDataNames();

            for (String dataName : dataNames) {
                item = new JRadioButtonMenuItem(dataName);
                item.addActionListener(this);
                mViewDataFor.add(item);
                dataNameItems.add(item);
                radioGroup.add(item);
            }
            mViewDataFor.setEnabled(true);


        } else if (type.equals("map")) {
            mainPanel = MapImagePanel.instance();
            mainPanel.setDocument(current);
            invalidateImagePanel();
            miViewImage.setEnabled(true);
            miViewRooms.setEnabled(true);
            miZoom.setEnabled(true);
            mViewDataFor.setEnabled(false);
        } else {
        }
        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();

    }


    public void mouseClicked(MouseEvent event) {
    }


    public void mouseEntered(MouseEvent event) {
    }


    public void mouseExited(MouseEvent event) {
    }


    public void mousePressed(MouseEvent event) {
        if (current != null) {

            drawnX1 = event.getX();
            drawnY1 = event.getY();
            drawnX2 = drawnX1;
            drawnY2 = drawnY1;
//			buttonPressed = event.getButton();
            buttonReleased = null;
//
//			//left button?
//			if(buttonPressed == 1) {
//				//is there already a selection of objects?
////				if(current.selection() != null) {
////					//mark this point as the reference point for moving...
////					current.setMovingReferencePoint(x1, y1);
////				}
//			}
//			//right button?
//			else if(buttonPressed == 3) {
//
//			}
        }
    }


    @Override
    public void mouseReleased(MouseEvent event) {
        if (current != null && this.miViewRooms.isSelected() && this.miViewImage.isSelected()) {

            drawnX2 = event.getX();
            drawnY2 = event.getY();

            Point actualPoint = MapImagePanel.convertToActualCoordinate(new Point(drawnX1, drawnY1), current.getCurrentFloor());
            actualX1 = actualPoint.x;
            actualY1 = actualPoint.y;
            actualPoint = MapImagePanel.convertToActualCoordinate(new Point(drawnX2, drawnY2), current.getCurrentFloor());
            actualX2 = actualPoint.x;
            actualY2 = actualPoint.y;
//			buttonPressed = null;
            buttonReleased = event.getButton();


            if (drawnX2.equals(drawnX1) && drawnY1.equals(drawnY2)) {


                current.selectObject(actualX1, actualY1, event.isControlDown());


            }
            //right button?
            if (buttonReleased == 3) {

                this.showPopupMenu(event);
            }
            invalidateImagePanel();
        }


    }


    void unsetMouseSelection() {
        assert mainPanel instanceof MapImagePanel;
        ((MapImagePanel) mainPanel).unsetSelection();
        mainPanel.repaint();
    }

    public void mouseDragged(MouseEvent event) {
        if (current != null && mainPanel instanceof MapImagePanel) {
            drawnX2 = event.getX();
            drawnY2 = event.getY();

            //no, draw the selection frame...
            ((MapImagePanel) mainPanel).setSelection(drawnX1, drawnY1, drawnX2, drawnY2);
            this.invalidateImagePanel();
        }
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        if (current != null) {
            drawnX1 = event.getX();
            drawnY1 = event.getY();

            Point actualPoint = MapImagePanel.convertToActualCoordinate(new Point(drawnX1, drawnY1), current.getCurrentFloor());
            actualX1 = actualPoint.x;
            actualY1 = actualPoint.y;
            hoveringOverRoomId = current.isHoveringOverRoom(actualX1, actualY1);
            hoveringOverStaircaseId = current.isHoveringOverStaircase(actualX1, actualY1);

            this.invalidateImagePanel();
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.setVisible(true);
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {

        SliderMenuItem source = (SliderMenuItem) changeEvent.getSource();


        MapImagePanel.setZoom(source.getValue());
        this.invalidateImagePanel();


    }


}
