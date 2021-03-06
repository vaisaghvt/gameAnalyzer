package gui;

import database.Database;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
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
 * This is the main currentFrame and panel.
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
    private final JMenuItem miViewLinks = new JCheckBoxMenuItem("View Links");
    private final JMenuItem miNetworkView = new JRadioButtonMenuItem("Network View");
    private final JMenuItem miRoomView = new JRadioButtonMenuItem("Room View");
    private static final int MIN_ZOOM = 1;
    private static final int MAX_ZOOM = 10;
    private static final int DEFAULT_ZOOM = 5;
    private final SliderMenuItem miZoom = new SliderMenuItem("Zoom", MIN_ZOOM, MAX_ZOOM, DEFAULT_ZOOM);
    private final JMenu mViewDataFor = new JMenu("Choose dataname...");
    private final JMenu mChoosePhase = new JMenu("Choose phase...");
    private final JMenuItem miExplorationPhase = new JCheckBoxMenuItem("Exploration");
    private final JMenuItem miTask1 = new JCheckBoxMenuItem("Task 1");
    private final JMenuItem miTask2 = new JCheckBoxMenuItem("Task 2");
    private final JMenuItem miTask3 = new JCheckBoxMenuItem("Task 3");

    private final JMenu mAnalysis = new JMenu("Analyze");
    private final JMenuItem miStatistics = new JMenuItem("Statistics");


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


        this.setSize(1000, 1000);
    }

    private void update() {
        if (current != null) {
            this.setTitle(current.name() + " - " + APPLICATION_TITLE);

            miSave.setEnabled(true);
            miClose.setEnabled(true);
            mView.setEnabled(true);
            mAnalysis.setEnabled(false);
            mViewDataFor.setEnabled(false);
            mChoosePhase.setEnabled(false);

            mainPanel.setDocument(current);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    System.out.println("here");
                    mainPanel.repaint();
                    mainPanel.revalidate();
                }
            });

        } else {
            this.setTitle(APPLICATION_TITLE);
            miSave.setEnabled(false);
            miClose.setEnabled(false);
            mView.setEnabled(false);
            mAnalysis.setEnabled(false);

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
        menuBar.add(mAnalysis);

        mFile.add(miNew);
        mFile.add(miOpen);
        mFile.add(miSave);
        mFile.add(miClose);
        mFile.add(miExit);


        mChoosePhase.add(miExplorationPhase);

        mChoosePhase.add(miTask1);

        mChoosePhase.add(miTask2);

        mChoosePhase.add(miTask3);


        ButtonGroup radioGroup = new ButtonGroup();


        this.miNetworkView.setSelected(false);
        radioGroup.add(miNetworkView);
        mView.add(miNetworkView);


        this.miRoomView.setSelected(true);
        radioGroup.add(miRoomView);
        mView.add(miRoomView);

        miViewImage.setSelected(true);
        miViewRooms.setSelected(true);
        miViewLinks.setSelected(true);

        mView.addSeparator();
        mView.add(this.miViewImage);
        mView.add(this.miViewRooms);
        mView.add(this.miViewLinks);

        mView.addSeparator();
        mView.add(this.miZoom);

        mView.addSeparator();
        mView.add(this.mViewDataFor);
        mView.add(this.mChoosePhase);

        mAnalysis.add(miStatistics);


        miNew.addActionListener(this);
        miOpen.addActionListener(this);
        miSave.addActionListener(this);
        miClose.addActionListener(this);
        miExit.addActionListener(this);
        miNetworkView.addActionListener(this);
        miRoomView.addActionListener(this);
        miViewImage.addActionListener(this);
        miViewRooms.addActionListener(this);
        miViewLinks.addActionListener(this);
        miZoom.addChangeListener(this);
        miExplorationPhase.addActionListener(this);
        miTask1.addActionListener(this);
        miTask2.addActionListener(this);
        miTask3.addActionListener(this);
        miStatistics.addActionListener(this);


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
        } else {
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
        JFileChooser chooser = new JFileChooser(new File("."));
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
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Select model file");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Only xml files","xml"
        ));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                current = new Document(file);
                this.update();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "ERROR: the file '" + file.getName() + "' could not be opened!", "An Error Occured", JOptionPane.ERROR_MESSAGE);
//                e.printStackTrace();
            }
        }
    }

    void invalidateImagePanel() {
        mainPanel.repaint();
        mainPanel.revalidate();
    }


    public void actionPerformed(ActionEvent event) {

        if (event.getSource() == miNew) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tryToCreateNew();
                }
            });
        } else if (event.getSource() == miOpen) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tryToOpen();
                }
            });
        } else if (event.getSource() == miSave) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tryToSave();
                }
            });
        } else if (event.getSource() == miClose) {
            assert (current != null);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tryToClose();
                }
            });
        } else if (event.getSource() == miExit) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (current != null && current.hasUnsavedChanges()) {
                        if (handleUnsavedChanges()) {
                            System.exit(0);
                        }
                    } else {
                        System.exit(0);
                    }
                }
            });
        } else if (event.getSource() == miStatistics) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    StatsDialog dialog = new StatsDialog();
                    dialog.setVisible(true);
                }
            });
        } else if (event.getSource() == miNetworkView) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (miNetworkView.isSelected()) {
                        assert miNetworkView.isSelected() && !miRoomView.isSelected();
                        if (!(mainPanel instanceof NetworkModel) && current != null) {
                            changePanel("network");


                        }


                    }
                }
            });

        } else if (event.getSource() == miRoomView) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (miRoomView.isSelected()) {

                        assert !miNetworkView.isSelected() && miRoomView.isSelected();
                        if (!(mainPanel instanceof MapImagePanel) && current != null) {
                            changePanel("map");

                        }
                    }
                }
            });
        } else if (event.getSource() == miViewImage) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    assert mainPanel instanceof MapImagePanel;
                    if (miViewImage.isSelected()) {
                        ((MapImagePanel) mainPanel).enableImageView();
                    } else {
                        ((MapImagePanel) mainPanel).disableImageView();
                    }
                    invalidateImagePanel();
                }
            });
        } else if (event.getSource() == miViewRooms) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    assert mainPanel instanceof MapImagePanel;
                    if (miViewRooms.isSelected()) {
                        ((MapImagePanel) mainPanel).enableRoomView();

                    } else {
                        ((MapImagePanel) mainPanel).disableRoomView();
                    }
                    invalidateImagePanel();
                }
            });
        } else if (event.getSource() == miViewLinks) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    assert mainPanel instanceof MapImagePanel;
                    if (miViewLinks.isSelected()) {
                        ((MapImagePanel) mainPanel).enableLinkView();
                    } else {
                        ((MapImagePanel) mainPanel).disableLinkView();
                    }
                    invalidateImagePanel();
                }
            });
        }else if (dataNameItems != null && dataNameItems.contains(event.getSource())) {
            final JMenuItem item = (JMenuItem) event.getSource();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {

                    ((NetworkModel) mainPanel).setDisplay(item.getText(), false);
                    setTitle(item.getText() + " - Network Display");
                    if (item.getText().equalsIgnoreCase("default")) {
                        mChoosePhase.setEnabled(false);

                        menuBar.revalidate();
                    } else {
                        mChoosePhase.setEnabled(true);
                        miExplorationPhase.setSelected(true);
                        miTask1.setSelected(true);
                        miTask2.setSelected(true);
                        miTask3.setSelected(true);

                        menuBar.revalidate();

                    }


                    invalidateImagePanel();
                }
            });
        } else if (event.getSource() == miExplorationPhase) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (miExplorationPhase.isSelected()) {
                        ((NetworkModel) mainPanel).switchOnPhase(Phase.EXPLORATION);
                    } else {
                        ((NetworkModel) mainPanel).switchOffPhase(Phase.EXPLORATION);

                    }
                }
            });
//            ((NetworkModel) mainPanel).recreateContextMenu();
        } else if (event.getSource() == miTask1) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (miTask1.isSelected()) {
                        ((NetworkModel) mainPanel).switchOnPhase(Phase.TASK_1);
                    } else {
                        ((NetworkModel) mainPanel).switchOffPhase(Phase.TASK_1);

                    }
                }
            });
//            ((NetworkModel) mainPanel).recreateContextMenu();
        } else if (event.getSource() == miTask2) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (miTask2.isSelected()) {
                        ((NetworkModel) mainPanel).switchOnPhase(Phase.TASK_2);
                    } else {
                        ((NetworkModel) mainPanel).switchOffPhase(Phase.TASK_2);

                    }
                }
            });
//            ((NetworkModel) mainPanel).recreateContextMenu();
        } else if (event.getSource() == miTask3) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (miTask3.isSelected()) {
                        ((NetworkModel) mainPanel).switchOnPhase(Phase.TASK_3);
                    } else {
                        ((NetworkModel) mainPanel).switchOffPhase(Phase.TASK_3);

                    }
                }
            });
//            ((NetworkModel) mainPanel).recreateContextMenu();
        } else {
            assert mainPanel instanceof MapImagePanel;
            Point actualPoint = MapImagePanel.convertToActualCoordinate(new Point(drawnX1, drawnY1), current.getCurrentFloor());
            actualX1 = actualPoint.x;
            actualY1 = actualPoint.y;
            actualPoint = MapImagePanel.convertToActualCoordinate(new Point(drawnX2, drawnY2), current.getCurrentFloor());
            actualX2 = actualPoint.x;
            actualY2 = actualPoint.y;
            if (event.getSource() == miAddRoom) {
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    public final Void doInBackground() {
                        current.addRoom(actualX1, actualY1, actualX2, actualY2);
                        return null;
                    }

                    public final void done() {
                        invalidateImagePanel();
                    }


                };
                worker.execute();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        unsetMouseSelection();
                    }
                });

            } else if (event.getSource() == miAddLink) {
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    public final Void doInBackground() {
                        current.addLink(actualX1, actualY1, actualX2, actualY2);
                        return null;
                    }

                    public final void done() {
                        invalidateImagePanel();
                    }


                };

                worker.execute();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        unsetMouseSelection();
                    }
                });
            } else if (event.getSource() == miAddStaircase) {
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    public final Void doInBackground() {
                        current.addStaircase(actualX1, actualY1, actualX2, actualY2);
                        return null;
                    }

                    public final void done() {
                        invalidateImagePanel();
                    }


                };

                worker.execute();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        unsetMouseSelection();
                    }
                });


            } else if (event.getSource() == miEditStaircaseGroup) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ObjectPanelStaircaseGroup dialog = new ObjectPanelStaircaseGroup(current);
                        dialog.setVisible(true);
                    }
                });

            } else if (event.getSource() == miRemoveSelection) {
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    public final Void doInBackground() {
                        current.removeSelected();
                        return null;
                    }

                    public final void done() {
                        invalidateImagePanel();
                    }


                };

                worker.execute();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        unsetMouseSelection();
                    }
                });


            } else if (event.getSource() == miCreateGroup) {
//                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
//                    @Override
//                    public final Void doInBackground() {
                        String newName = JOptionPane.showInternalInputDialog(
                                getContentPane(),
                                "What's the name of this group?",
                                "Name group",
                                JOptionPane.QUESTION_MESSAGE);

                        current.createGroup(newName);
//                        return null;
//                    }

//                    public final void done() {
                        invalidateImagePanel();
//                    }


//                };

//                worker.execute();


            } else if (event.getSource() == miRemoveGroup) {
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    public final Void doInBackground() {
                        current.removeGroup();
                        return null;
                    }

                    public final void done() {
                        invalidateImagePanel();
                    }


                };

                worker.execute();


            } else if (event.getSource() == miRenameArea) {
//                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
//                    @Override
//                    public final Void doInBackground() {
                        if (current.selected() != null && !current.selected().isEmpty() && current.selected().size() == 1) {
                            String newName = JOptionPane.showInternalInputDialog(
                                    getContentPane(),
                                    "New name for room?",
                                    "Rename " + current.selected().iterator().next().getName(),
                                    JOptionPane.QUESTION_MESSAGE);
                            current.renameSelected(newName);
                            unsetMouseSelection();
                            invalidateImagePanel();
                        }
//                        return null;
//                    }

//                };

//                worker.execute();

            } else if (floorItems != null && floorItems.contains(event.getSource())) {
                final JMenuItem item = (JMenuItem) event.getSource();
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                    @Override
                    public final Void doInBackground() {

                        String t1 = item.getText();
                        String t2 = t1.substring(t1.indexOf("#") + 1);
                        int idx = Integer.parseInt(t2);
                        if (current != null) {
                            current.selectFloor(idx);
                        }
                        ((MapImagePanel) mainPanel).selectFloor(idx);
                        return null;
                    }

                    public final void done() {
                        invalidateImagePanel();
                    }


                };

                worker.execute();


                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        unsetMouseSelection();
                    }
                });


            }
        }
    }

    private void changePanel(String type) {

        this.getContentPane().remove(mainPanel);
        if (type.equals("network")) {

            mainPanel = NetworkModel.instance();

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                public final Void doInBackground() {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setTitle("processing network...");
                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        }
                    });
                    NetworkModel.instance().setDocument(current);

                    return null;
                }

                public final void done() {
                    invalidateImagePanel();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            setTitle("default - Network Display");
                            miViewImage.setEnabled(false);
                            miViewRooms.setEnabled(false);
                            miViewLinks.setEnabled(false);
                            miZoom.setEnabled(false);

                            //update the floors
                            mViewDataFor.removeAll();


                            dataNameItems = null;


                            ButtonGroup radioGroup = new ButtonGroup();
                            dataNameItems = new HashSet<JRadioButtonMenuItem>();
                            JRadioButtonMenuItem item = new JRadioButtonMenuItem("default");
                            dataNameItems.add(item);
                            item.addActionListener(Main.this);
                            mViewDataFor.add(item);
                            dataNameItems.add(item);
                            radioGroup.add(item);
                            item.setSelected(true);

                            Collection<String> dataNames = Database.getInstance().getDataNames();

                            for (String dataName : dataNames) {
                                item = new JRadioButtonMenuItem(dataName);
                                item.addActionListener(Main.this);
                                mViewDataFor.add(item);
                                dataNameItems.add(item);
                                radioGroup.add(item);
                            }
                            mViewDataFor.setEnabled(true);
                            mAnalysis.setEnabled(true);


                            menuBar.revalidate();
                            getContentPane().add(mainPanel, BorderLayout.CENTER);
                        }
                    });
                }


            };
            worker.execute();


        } else if (type.equals("map")) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {

                    menuBar.revalidate();
                    mainPanel = MapImagePanel.instance();
                    mainPanel.setDocument(current);
                    invalidateImagePanel();
                    miViewImage.setEnabled(true);
                    miViewRooms.setEnabled(true);
                    miViewLinks.setEnabled(true);
                    miZoom.setEnabled(true);
                    mViewDataFor.setEnabled(false);
                    mAnalysis.setEnabled(false);
                    mChoosePhase.setEnabled(false);

                    setTitle(current.name() + " - " + APPLICATION_TITLE);
                    getContentPane().add(mainPanel, BorderLayout.CENTER);
                }
            });

//            ((NetworkModel)mainPanel).getContextMenu().setEnabled(true);
//            ((NetworkModel)mainPanel).getContextMenu().setVisible(true);
        } else {
        }


//        mainPanel.revalidate();
//        mainPanel.repaint();

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

            //no, draw the selection currentFrame...
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Set cross-platform Java L&F (also called "Metal")
                    UIManager.setLookAndFeel(
                            UIManager.getSystemLookAndFeelClassName());
                }
                catch (UnsupportedLookAndFeelException e) {
                    // handle exception
                }
                catch (ClassNotFoundException e) {
                    // handle exception
                }
                catch (InstantiationException e) {
                    // handle exception
                }
                catch (IllegalAccessException e) {
                    // handle exception
                }

                Main main = new Main();
                main.setVisible(true);
            }
        });

    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {

        SliderMenuItem source = (SliderMenuItem) changeEvent.getSource();


        MapImagePanel.setZoom(source.getValue());
        this.invalidateImagePanel();


    }


}
