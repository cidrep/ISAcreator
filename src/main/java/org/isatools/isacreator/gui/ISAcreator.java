/**
 ISAcreator is a component of the ISA software suite (http://www.isa-tools.org)

 License:
 ISAcreator is licensed under the Common Public Attribution License version 1.0 (CPAL)

 EXHIBIT A. CPAL version 1.0
 �The contents of this file are subject to the CPAL version 1.0 (the �License�);
 you may not use this file except in compliance with the License. You may obtain a
 copy of the License at http://isa-tools.org/licenses/ISAcreator-license.html.
 The License is based on the Mozilla Public License version 1.1 but Sections
 14 and 15 have been added to cover use of software over a computer network and
 provide for limited attribution for the Original Developer. In addition, Exhibit
 A has been modified to be consistent with Exhibit B.

 Software distributed under the License is distributed on an �AS IS� basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is ISAcreator.
 The Original Developer is the Initial Developer. The Initial Developer of the
 Original Code is the ISA Team (Eamonn Maguire, eamonnmag@gmail.com;
 Philippe Rocca-Serra, proccaserra@gmail.com; Susanna-Assunta Sansone, sa.sanson@gmail.com;
 http://www.isa-tools.org). All portions of the code written by the ISA Team are
 Copyright (c) 2007-2011 ISA Team. All Rights Reserved.

 EXHIBIT B. Attribution Information
 Attribution Copyright Notice: Copyright (c) 2008-2011 ISA Team
 Attribution Phrase: Developed by the ISA Team
 Attribution URL: http://www.isa-tools.org
 Graphic Image provided in the Covered Code as file: http://isa-tools.org/licenses/icons/poweredByISAtools.png
 Display of Attribution Information is required in Larger Works which are defined in the CPAL as a work which combines Covered Code or portions thereof with code not governed by the terms of the CPAL.

 Sponsors:
 The ISA Team and the ISA software suite have been funded by the EU Carcinogenomics
 project (http://www.carcinogenomics.eu), the UK BBSRC (http://www.bbsrc.ac.uk), the UK NERC-NEBC (http://nebc.nerc.ac.uk) and in part by the EU NuGO consortium (http://www.nugo.org/everyone).
 */

package org.isatools.isacreator.gui;

import org.apache.log4j.Logger;
import org.isatools.isacreator.archiveoutput.ArchiveOutputUtil;
import org.isatools.isacreator.archiveoutput.ArchiveOutputWindow;
import org.isatools.isacreator.autofiltercombo.AutoFilterComboCellEditor;
import org.isatools.isacreator.common.MappingObject;
import org.isatools.isacreator.common.UIHelper;
import org.isatools.isacreator.configuration.io.ConfigXMLParser;
import org.isatools.isacreator.effects.AniSheetableJFrame;
import org.isatools.isacreator.effects.FooterPanel;
import org.isatools.isacreator.effects.TitlePanel;
import org.isatools.isacreator.filechooser.FTPManager;
import org.isatools.isacreator.gui.menu.ISAcreatorMenu;
import org.isatools.isacreator.gui.modeselection.Mode;
import org.isatools.isacreator.io.OutputISAFiles;
import org.isatools.isacreator.io.UserProfile;
import org.isatools.isacreator.io.UserProfileIO;
import org.isatools.isacreator.model.Investigation;
import org.isatools.isacreator.ontologiser.adaptors.InvestigationAdaptor;
import org.isatools.isacreator.ontologiser.ui.OntologiserUI;
import org.isatools.isacreator.ontologyselectiontool.OntologyObject;
import org.isatools.isacreator.ontologyselectiontool.OntologySourceManager;
import org.isatools.isacreator.qrcode.ui.QRCodeGeneratorUI;
import org.isatools.isacreator.settings.SettingsUtil;
import org.isatools.isacreator.spreadsheet.IncorrectColumnOrderGUI;
import org.isatools.isacreator.spreadsheet.Spreadsheet;
import org.isatools.isacreator.spreadsheet.TableReferenceObject;
import org.isatools.isacreator.utils.IncorrectColumnPositioning;
import org.isatools.isacreator.utils.PropertyFileIO;
import org.jdesktop.fuse.InjectedResource;
import org.jdesktop.fuse.ResourceInjector;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * ISAcreator is a container for the Whole ISAcreator application.
 * Each section of the application is a JLayeredPane which is swapped in and out
 * of the JFrame depending on what the USER wants to do.
 * For example, the data entry pane will contain everything required for the user to enter
 * information pertaining to their experiment. Moreover, the wizard pane will contain all
 * the information required to create as much of the data as possible through the user answering
 * questions on their experiment.
 *
 * @author Eamonn Maguire
 */
public class ISAcreator extends AniSheetableJFrame {

    private static Logger log = Logger.getLogger(ISAcreator.class.getName());

    public static String DEFAULT_ISATAB_SAVE_DIRECTORY = "isatab files";
    public static String DEFAULT_CONFIGURATIONS_DIRECTORY = "Configurations";
    public static String DEFAULT_USER_PROFILE_DIRECTORY = "";

    public static final int APP_WIDTH = 999;
    public static final int APP_HEIGHT = 700;

    @InjectedResource
    private Image isacreatorIcon;
    @InjectedResource
    private ImageIcon saveIcon, saveMenuIcon, saveLogoutIcon, saveExitIcon,
            logoutIcon, menuIcon, exitIcon, exportArchiveIcon, addStudyIcon,
            removeStudyIcon, fullScreenIcon, defaultScreenIcon, aboutIcon, helpIcon, mgRastIcon, qrCodeIcon,
            supportIcon, feedbackIcon, confirmLogout, confirmMenu, confirmExit;

    private AboutPanel aboutPanel;
    private Properties programSettings;

    private List<TableReferenceObject> assayDefinitions;
    private List<MappingObject> mappings;

    private DataEntryEnvironment curDataEntryEnvironment;
    private GridBagConstraints c;
    private JLayeredPane currentPage = null;
    private JPanel glass;

    private ISAcreatorMenu lp = null;
    private UserProfile currentUser = null;
    private JMenuBar menuBar;

    private Map<String, JMenu> menusRequiringStudyIds;

    private String lastExport = "";

    private OutputISAFiles outputISATAB;
    private IncorrectColumnOrderGUI incorrectGUI;
    private UserProfileIO userProfileIO;
    private String loadedConfiguration;
    private Mode mode;

    static {
        UIManager.put("Panel.background", UIHelper.BG_COLOR);
        UIManager.put("ToolTip.foreground", Color.white);
        UIManager.put("ToolTip.background", UIHelper.DARK_GREEN_COLOR);
        UIManager.put("Tree.background", UIHelper.BG_COLOR);

        UIManager.put("Container.background", UIHelper.BG_COLOR);
        UIManager.put("PopupMenuUI", "org.isatools.isacreator.common.CustomPopupMenuUI");
        UIManager.put("MenuItemUI", "org.isatools.isacreator.common.CustomMenuItemUI");
        UIManager.put("MenuUI", "org.isatools.isacreator.common.CustomMenuUI");
        UIManager.put("SeparatorUI", "org.isatools.isacreator.common.CustomSeparatorUI");
        UIManager.put("MenuBarUI", "org.isatools.isacreator.common.CustomMenuBarUI");


        ResourceInjector.addModule("org.jdesktop.fuse.swing.SwingModule");

        ResourceInjector.get("archiveoutput-package.style").load(
                ArchiveOutputWindow.class.getResource("/dependency-injections/archiveoutput-package.properties"));
        ResourceInjector.get("gui-package.style").load(
                ISAcreator.class.getResource("/dependency-injections/gui-package.properties"));
        ResourceInjector.get("common-package.style").load(
                ISAcreator.class.getResource("/dependency-injections/common-package.properties"));
        ResourceInjector.get("filechooser-package.style").load(
                ISAcreator.class.getResource("/dependency-injections/filechooser-package.properties"));
        ResourceInjector.get("longtexteditor-package.style").load(
                ISAcreator.class.getResource("/dependency-injections/longtexteditor-package.properties"));
        ResourceInjector.get("mergeutil-package.style").load(
                ISAcreator.class.getResource("/dependency-injections/mergeutil-package.properties"));
        ResourceInjector.get("publicationlocator-package.style").load(
                ISAcreator.class.getResource("/dependency-injections/publicationlocator-package.properties"));
        ResourceInjector.get("wizard-package.style").load(
                ISAcreator.class.getResource("/dependency-injections/wizard-package.properties"));
        ResourceInjector.get("formatmappingutility-package.style").load(
                ISAcreator.class.getResource("/dependency-injections/formatmappingutility-package.properties"));
        ResourceInjector.get("arraydesignbrowser-package.style").load(ISAcreator.class.getResource
                ("/dependency-injections/arraydesignbrowser-package.properties"));

        ResourceInjector.get("effects-package.style").load(ISAcreator.class.getResource
                ("/dependency-injections/effects-package.properties"));

        ResourceInjector.get("calendar-package.style").load(
                ISAcreator.class.getResource("/dependency-injections/calendar-package.properties"));
    }

    public ISAcreator(Mode mode) {
        this(mode, null);
    }

    public ISAcreator(Mode mode, String configDir) {

        ResourceInjector.get("gui-package.style").inject(this);

        this.mode = mode;

        outputISATAB = new OutputISAFiles(this);
        userProfileIO = new UserProfileIO(this);
        menusRequiringStudyIds = new HashMap<String, JMenu>();


        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.gridheight = 2;
        c.gridx = 1;
        c.gridy = 3;

        // this is a way of loading the Configurations through the API.
        if (configDir != null) {
            ConfigXMLParser cp = new ConfigXMLParser(configDir);
            cp.loadConfiguration();
            mappings = cp.getMappings();
            assayDefinitions = cp.getTables();
        }

    }

    public void createGUI() {
        setPreferredSize(new Dimension(APP_WIDTH, APP_HEIGHT));
        setIconImage(isacreatorIcon);
        setBackground(UIHelper.BG_COLOR);
        setTitle("ISAcreator 1.4");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setResizable(true);
        setLayout(new BorderLayout());
        // load user profiles into the system
        userProfileIO.loadUserProfiles();
        loadProgramSettings();
        // create the top menu bar

        createTopPanel();

        FooterPanel fp = new FooterPanel(this);
        add(fp, BorderLayout.SOUTH);

        ((JComponent) getContentPane()).setBorder(new LineBorder(UIHelper.LIGHT_GREEN_COLOR, 1));

        setupAboutPanel();
        // check that java version is supported!
        if (!checkSystemRequirements()) {
            lp = new ISAcreatorMenu(ISAcreator.this, ISAcreatorMenu.SHOW_UNSUPPORTED_JAVA);
        } else {
            lp = new ISAcreatorMenu(ISAcreator.this, ISAcreatorMenu.SHOW_IMPORT_CONFIGURATION);
        }
        setCurrentPage(lp);
        pack();
        setVisible(true);

    }

    public void setMappings(List<MappingObject> mappings) {
        this.mappings = mappings;
    }

    public void setAssayDefinitions(List<TableReferenceObject> assayDefinitions) {
        this.assayDefinitions = assayDefinitions;
    }



    private void checkMenuRequired() {
        boolean menuRequired = currentPage instanceof DataEntryEnvironment;
        menuBar.getParent().setVisible(menuRequired);
    }


    private void setupAboutPanel() {
        aboutPanel = new AboutPanel();
        aboutPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                glass.setVisible(false);
            }
        });

    }


    private void loadProgramSettings() {
        programSettings = PropertyFileIO.loadSettings(SettingsUtil.PROPERTIES_FILE);
        if (programSettings == null) {
            programSettings = new Properties();
        } else {
            Object isatabLocation = programSettings.get("isacreator.isatabLocation");


            if (isatabLocation != null) {
                // test to ensure directory exists before replacing working directory with an incorrect one
                File isaLocation = new File(isatabLocation.toString());

                if (isaLocation.exists()) {
                    DEFAULT_ISATAB_SAVE_DIRECTORY = isaLocation.getAbsolutePath();
                }
            }

            Object configurationLocation = programSettings.get("isacreator.configurationLocation");

            if (configurationLocation != null) {
                File configurationLocationFile = new File(configurationLocation.toString());
                if (configurationLocationFile.exists()) {
                    DEFAULT_CONFIGURATIONS_DIRECTORY = configurationLocationFile.getAbsolutePath();
                }
            }

            Object profileLocation = programSettings.get("isacreator.userProfileLocation");

            if (profileLocation != null) {
                if (new File(profileLocation.toString()).exists()) {
                    DEFAULT_USER_PROFILE_DIRECTORY = profileLocation.toString();
                }
            }


        }
    }

    private boolean checkSystemRequirements() {
        String version = System.getProperty("java.version");
        log.info("System details");
        log.info(System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + (System.getProperty("os.arch").equals("x86_64") ? "64 bit" : "32 bit"));
        log.info("JRE version: " + version);

        char minorVersion = version.charAt(2);
        return minorVersion >= '6';
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        TitlePanel titlePane = new ISAcreatorTitlePanel();
        getRootPane().setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        titlePane.addPropertyChangeListener(ISAcreatorTitlePanel.CLOSE_EVENT, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                closeISAcreator();
            }
        });

        topPanel.add(titlePane);
        topPanel.add(createMenuBar());
        add(topPanel, BorderLayout.NORTH);
        titlePane.installListeners();
    }

    private Container createMenuBar() {
        // create menu bar
        menuBar = new JMenuBar();
        menuBar.setForeground(UIHelper.DARK_GREEN_COLOR);
        menuBar.setBorder(null);

        JMenu file = new JMenu("file");

        JMenuItem save = new JMenuItem(new SaveAction(SaveAction.SAVE_ONLY,
                "save", saveIcon, "save ISA files", KeyEvent.VK_S));
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                KeyEvent.CTRL_MASK));

        file.add(save);

        JMenuItem saveAs = new JMenuItem(new SaveAction(SaveAction.SAVE_AS,
                "save as", saveIcon, "save as a different set of ISA files",
                KeyEvent.VK_A));

        file.add(saveAs);

        file.add(new JSeparator());

        JMenuItem main = new JMenuItem(new LeaveAction(LeaveAction.MAIN,
                "go to main menu",
                menuIcon,
                "go back to main menu without saving", null));
        file.add(main);

        file.add(new JSeparator());

        JMenuItem exportISArchive = new JMenuItem("create ISArchive",
                exportArchiveIcon);
        exportISArchive.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        ArchiveOutputUtil oISA = new ArchiveOutputUtil(ISAcreator.this);
                        oISA.createGUI();

                        ArchiveOutputWindow oaw = new ArchiveOutputWindow(oISA);
                        oaw.setLocation(((getX() + getWidth()) / 2) - (ArchiveOutputWindow.WIDTH / 2), ((getY() + getHeight()) / 2) - (ArchiveOutputWindow.HEIGHT / 2));
                        oaw.createGUI();
                    }
                });
            }
        });

        file.add(exportISArchive);

        menuBar.add(file);


        // study section

        JMenu studyMenu = new JMenu("study");

        JMenuItem addStudy = new JMenuItem("add study",
                addStudyIcon);
        addStudy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getDataEntryEnvironment().addStudyToTree();
            }
        });
        studyMenu.add(addStudy);

        JMenuItem removeStudy = new JMenuItem("remove study",
                removeStudyIcon);
        removeStudy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getDataEntryEnvironment().removeStudy();
            }
        });
        studyMenu.add(removeStudy);

        menuBar.add(studyMenu);

        // end study section

        JMenu view = new JMenu("view");

        JMenuItem fullScreen = new JMenuItem(new ScreenResizeAction(
                ScreenResizeAction.FULL_SCREEN, "full screen",
                fullScreenIcon,
                "Put application in full screen mode.", KeyEvent.VK_F));
        fullScreen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                KeyEvent.ALT_MASK));

        JMenuItem defaultScreen = new JMenuItem(new ScreenResizeAction(
                ScreenResizeAction.DEFAULT_SIZE, "default screen",
                defaultScreenIcon,
                "Put application in the default screen mode.", KeyEvent.VK_D));
        defaultScreen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                KeyEvent.ALT_MASK));

        view.add(defaultScreen);
        view.add(fullScreen);

        menuBar.add(view);

        JMenu plugins = new JMenu("utilities");


        JMenu sampleTracking = new JMenu("Sample Tracking");
        sampleTracking.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                updateExportList();
            }
        });

        JMenuItem tagInvestigation = new JMenuItem("Autotagging");
        tagInvestigation.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                OntologiserUI ontologiserUI = new OntologiserUI(ISAcreator.this, new InvestigationAdaptor(getDataEntryEnvironment().getInvestigation()));
                ontologiserUI.createGUI();

                showJDialogAsSheet(ontologiserUI);
            }
        });

        JMenu qrCodeExport = new JMenu("generate QR codes for Study samples");
        qrCodeExport.setIcon(qrCodeIcon);
        menusRequiringStudyIds.put("qr", qrCodeExport);
        sampleTracking.add(qrCodeExport);

        plugins.add(sampleTracking);
        plugins.add(tagInvestigation);

        menuBar.add(plugins);

        JMenu help = new JMenu("help");

        JMenuItem about = new JMenuItem("about",
                aboutIcon);
        about.setForeground(UIHelper.DARK_GREEN_COLOR);
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setGlassPanelContents(aboutPanel);
            }
        });
        help.add(about);

        JMenuItem manual = new JMenuItem("user manual",
                helpIcon);
        manual.setForeground(UIHelper.DARK_GREEN_COLOR);
        help.add(manual);
        manual.setEnabled(false);

        JMenuItem contact = new JMenuItem("contact support team",
                supportIcon);
        contact.setForeground(UIHelper.DARK_GREEN_COLOR);
        help.add(contact);
        contact.setEnabled(false);

        JMenuItem feedback = new JMenuItem("submit feedback",
                feedbackIcon);
        feedback.setForeground(UIHelper.DARK_GREEN_COLOR);
        help.add(feedback);
        feedback.setEnabled(false);

        menuBar.add(help);

        return UIHelper.wrapComponentInPanel(menuBar);
    }

    private void updateExportList() {


        Investigation i = curDataEntryEnvironment.getInvestigation();

        Set<String> studies = i.getStudies().keySet();

        String id = "";

        for (String s : studies) {
            id += s;
        }
        // we only want to update when something changes!
        if (lastExport.isEmpty() || !lastExport.equals(id)) {

            lastExport = id;

            for (String menuType : menusRequiringStudyIds.keySet()) {
                Component[] menuComponents = menusRequiringStudyIds.get(menuType).getMenuComponents();
                for (Component c : menuComponents) {
                    if (c instanceof JMenuItem) {
                        menusRequiringStudyIds.get(menuType).remove((JMenuItem) c);
                    }
                }

                for (final String study_id : studies) {

                    JMenuItem item = new JMenuItem(study_id);

                    if (menuType.equalsIgnoreCase("qr")) {
                        item.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {


                                Thread loader = new Thread(new Runnable() {
                                    public void run() {
                                        SwingUtilities.invokeLater(new Runnable() {
                                            public void run() {
                                                QRCodeGeneratorUI qrCodeUI = new QRCodeGeneratorUI(
                                                        ISAcreator.this, study_id);
                                                qrCodeUI.createGUI();
                                                showJDialogAsSheet(qrCodeUI);
                                                maskOutMouseEvents();
                                            }
                                        });
                                    }
                                });
                                loader.start();
                            }
                        });
                    }
                    menusRequiringStudyIds.get(menuType).add(item);
                }
            }
        }


    }

    private void createSubmissionDirectory(String dirPath) {
        File f = new File(dirPath);
        if (!f.exists() && !f.isDirectory()) {
            f.mkdir();
        }
    }

    public DataEntryEnvironment getDataEntryEnvironment() {
        return curDataEntryEnvironment;
    }

    public UserProfile getCurrentUser() {
        return currentUser;
    }

    public List<MappingObject> getMappings() {
        return mappings;
    }

    public List<UserProfile> getUserProfiles() {
        return userProfileIO.getUserProfiles();
    }

    public void hideGlassPane() {
        glass.setVisible(false);
    }

    private void saveProfilesAndExit() {

        if (getCurrentUser() != null) {
            for (UserProfile up : getUserProfiles()) {

                if (up.getUsername().equals(getCurrentUser().getUsername())) {
                    up.setUserHistory(OntologySourceManager.getUserOntologyHistory());
                    userProfileIO.updateUserProfileInformation(up);

                    break;
                }

            }
        }
        userProfileIO.saveUserProfiles();
        dispose();
        //exit with no problems!
        System.exit(0);
    }

    private void saveProfilesAndGoToMain() {

        try {

            for (UserProfile up : getUserProfiles()) {
                if (up.getUsername().equals(getCurrentUser().getUsername())) {
                    up.setUserHistory(OntologySourceManager.getUserOntologyHistory());
                    userProfileIO.updateUserProfileInformation(up);
                    break;
                }
            }

            userProfileIO.saveUserProfiles();
            checkMenuRequired();

            OntologySourceManager.clearReferencedOntologySources();

            curDataEntryEnvironment.removeReferences();
            curDataEntryEnvironment = null;

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Problem occurred when saving user profiles.");
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                lp.showGUI(ISAcreatorMenu.SHOW_MAIN);
                lp.startAnimation();
                setCurrentPage(lp);

            }
        });
    }

    private void saveProfilesAndLogout() {

        for (UserProfile up : getUserProfiles()) {
            if (up.getUsername().equals(getCurrentUser().getUsername())) {
                up.setUserHistory(OntologySourceManager.getUserOntologyHistory());
                userProfileIO.updateUserProfileInformation(up);

                break;
            }
        }

        userProfileIO.saveUserProfiles();

        checkMenuRequired();
        currentUser = null;

        // reset information pertinent to a certain user
        OntologySourceManager.clearReferencedOntologySources();
        OntologySourceManager.clearUserHistory();

        Spreadsheet.fileSelectEditor.setFtpManager(new FTPManager());

        curDataEntryEnvironment = null;
        OntologySourceManager.getUserOntologyHistory().clear();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                lp.showGUI(ISAcreatorMenu.SHOW_LOGIN);
                lp.startAnimation();
                setCurrentPage(lp);
            }
        });
    }


    public void setCurDataEntryPanel(DataEntryEnvironment dataEntryEnvironment) {
       curDataEntryEnvironment = dataEntryEnvironment;
        System.out.println("Data entry panel changed & initialised");
    }

    public void setCurrentPage(JLayeredPane newPage) {

        if (currentPage == null) {
            currentPage = newPage;
        } else {
            System.out.println("removing current page...");
            getContentPane().remove(currentPage);
            currentPage = null;
            currentPage = newPage;
        }

        checkMenuRequired();
        getContentPane().add(currentPage, BorderLayout.CENTER);

        currentPage.setBorder(new EmptyBorder(0, 0, 0, 0));

        repaint();
        validate();
    }

    public void setCurrentUser(UserProfile currentUser) {
        if (currentUser != null) {
            System.err.println("setting user to : " + currentUser.getUsername());
            this.currentUser = currentUser;
        } else {
            System.err.println("Logging out user...");
            this.currentUser = null;
        }
    }

    public void setGlassPanelContents(Container panel) {
        if (glass != null) {
            glass.removeAll();
        }
        glass = (JPanel) getGlassPane();
        glass.setLayout(new GridBagLayout());
        glass.add(panel, c);
        glass.setBackground(new Color(255, 255, 255, 10));
        glass.setVisible(true);
        glass.revalidate();
        glass.repaint();
    }

    public static void setUserOntologyHistory(Map<String, OntologyObject> userOntologyHistory) {
        OntologySourceManager.setOntologySelectionHistory(userOntologyHistory);
    }

    public void saveUserProfiles() {
        userProfileIO.saveUserProfiles();
    }

    public Properties getProgramSettings() {
        return programSettings;
    }

    public void setLoadedConfiguration(String name) {
        this.loadedConfiguration = name;
    }

    public String getLoadedConfiguration() {
        return loadedConfiguration;
    }


    class LeaveAction extends AbstractAction implements PropertyChangeListener {
        static final int LOGOUT = 0;
        static final int MAIN = 1;
        static final int EXIT = 2;
        private int type;
        private Timer closeWindowTimer;

        public LeaveAction(int type, String text, ImageIcon icon, String desc,
                           Integer mnemonic) {
            super(text, icon);
            this.type = type;
            putValue(SHORT_DESCRIPTION, desc);

            if (mnemonic != null) {
                putValue(MNEMONIC_KEY, mnemonic);
            }

            closeWindowTimer = new Timer(500, new CloseEvent());
        }

        public void actionPerformed(ActionEvent event) {

            final JOptionPane[] optionPane = new JOptionPane[1];
            final ImageIcon[] icon = new ImageIcon[1];
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {

                    String leaveText = "";

                    switch (type) {
                        case LOGOUT:
                            leaveText = "Are you sure you want to logout without saving?";
                            icon[0] = confirmLogout;
                            break;

                        case MAIN:
                            leaveText = "Are you sure you want to go to the main menu without saving?";
                            icon[0] = confirmMenu;
                            break;

                        case EXIT:

                            leaveText = "Are you sure you want to exit ISAcreator?";
                            icon[0] = confirmExit;
                            break;

                        default:

                            //
                    }

                    optionPane[0] = new JOptionPane(leaveText,
                            JOptionPane.INFORMATION_MESSAGE,
                            JOptionPane.YES_NO_OPTION,
                            icon[0]);
                    UIHelper.applyOptionPaneBackground(optionPane[0], UIHelper.BG_COLOR);
                    optionPane[0].addPropertyChangeListener(LeaveAction.this);

                    showJDialogAsSheet(optionPane[0].createDialog(
                            ISAcreator.this, "Confirm"));
                }
            });
        }

        public void propertyChange(PropertyChangeEvent event) {

            if (event.getPropertyName()
                    .equals(JOptionPane.VALUE_PROPERTY)) {
                int lastOptionAnswer = Integer.valueOf(event.getNewValue()
                        .toString());
                hideSheet();
                if (lastOptionAnswer == JOptionPane.YES_OPTION) {
                    closeWindowTimer.start();
                }
            }
        }

        class CloseEvent implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                doEvent();

                if (closeWindowTimer != null) {
                    closeWindowTimer.stop();
                }
            }

            private void doEvent() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        switch (type) {
                            case MAIN:
                                saveProfilesAndGoToMain();

                                break;

                            case LOGOUT:
                                saveProfilesAndLogout();

                                break;

                            case EXIT:
                                saveProfilesAndExit();

                                break;

                            default:
                                //
                        }
                    }
                });
            }
        }

    }

    class SaveAction extends AbstractAction {
        static final int SAVE_ONLY = 0;
        static final int SAVE_AS = 1;
        static final int SAVE_MAIN = 2;
        static final int SAVE_LOGOUT = 3;
        static final int SAVE_EXIT = 4;
        private int type;
        private Timer closeWindowTimer;

        /**
         * SaveAction constructor.
         *
         * @param type     - type of screen resize to be performed, either full screen or default screen...
         * @param text     - Text to be displayed in component
         * @param icon     - Icon to be displayed in component
         * @param desc     - description of the components purpose
         * @param mnemonic - shortcut key to be used!
         */
        public SaveAction(int type, String text, ImageIcon icon, String desc,
                          Integer mnemonic) {
            super(text, icon);
            this.type = type;
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);

            closeWindowTimer = new Timer(500, new CloseEvent());
        }

        public void actionPerformed(ActionEvent ae) {
            Calendar c = Calendar.getInstance();


            if ((type != SAVE_AS) && curDataEntryEnvironment.getInvestigation().getReference() != null && !curDataEntryEnvironment.getInvestigation().getReference().trim()
                    .equals("")) {

                if (curDataEntryEnvironment.getInvestigation().getReference().trim()
                        .equals("")) {
                    curDataEntryEnvironment.getInvestigation()
                            .setFileReference(DEFAULT_ISATAB_SAVE_DIRECTORY +
                                    File.separator + "SubmissionOn" +
                                    c.get(Calendar.DAY_OF_MONTH) + "-" +
                                    c.get(Calendar.MONTH) + "-" +
                                    c.get(Calendar.HOUR_OF_DAY) + ":" +
                                    c.get(Calendar.MINUTE) + File.separator +
                                    "Investigation");
                    createSubmissionDirectory(DEFAULT_ISATAB_SAVE_DIRECTORY + File.separator +
                            "SubmissionOn" + c.get(Calendar.DAY_OF_MONTH) + "-" +
                            c.get(Calendar.MONTH) + "-" +
                            c.get(Calendar.HOUR_OF_DAY) + ":" +
                            c.get(Calendar.MINUTE));
                }

                outputISATAB.saveISAFiles(false, getDataEntryEnvironment().getInvestigation());

                closeWindowTimer.start();

                if (type != SAVE_ONLY) {
                    OntologySourceManager.clearReferencedOntologySources();
                }
            } else {
                // need to get a new reference from the user!
                SaveAsDialog sad = new SaveAsDialog();
                sad.addPropertyChangeListener("windowClosed",
                        new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent event) {
                                hideSheet();
                            }
                        });

                sad.addPropertyChangeListener("save",
                        new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent event) {
                                String fileName = DEFAULT_ISATAB_SAVE_DIRECTORY + File.separator +
                                        event.getNewValue().toString() +
                                        File.separator + "Investigation";
                                createSubmissionDirectory(DEFAULT_ISATAB_SAVE_DIRECTORY +
                                        File.separator +
                                        event.getNewValue().toString());
                                curDataEntryEnvironment.getInvestigation()
                                        .setFileReference(fileName);

                                outputISATAB.saveISAFiles(false, getDataEntryEnvironment().getInvestigation());
                                userProfileIO.saveUserProfiles();

                                hideSheet();
                                closeWindowTimer.start();
                            }
                        });
                sad.createGUI();

                showJDialogAsSheet(sad);
            }

        }


        class CloseEvent implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                doEvent();

                if (closeWindowTimer != null) {
                    closeWindowTimer.stop();
                }
            }

            private void doEvent() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        if (outputISATAB.isShouldShowIncorrectOrderGUI()) {


                            Map<String, List<String>> report = new HashMap<String, List<String>>();
                            // highlight columns in each affected spreadsheet
                            for (Spreadsheet s : outputISATAB.getErrorSheets()) {
                                // highlight the columns in the spreadsheet
                                report.putAll(s.getTableConsistencyChecker().getErrorReport());
                                Map<Integer, Color> colorcoding = new HashMap<Integer, Color>();
                                for (IncorrectColumnPositioning icor : s.getTableConsistencyChecker().getRecords()) {
                                    colorcoding.putAll(icor.getColumnColors());
                                }
                                s.highlightSpecificColumns(colorcoding);
                            }

                            // show IncorrectColumnOrderGUI
                            incorrectGUI = new IncorrectColumnOrderGUI(report);
                            incorrectGUI.setLocation((getX() + getWidth()) / 2 - (incorrectGUI.getWidth() / 2), (getY() + getHeight()) / 2 - (incorrectGUI.getHeight() / 2));
                            incorrectGUI.createGUI();

                        } else {
                            // IF EVERYTHING IS OK, RESET ROW COLORS!
                            for (Spreadsheet s : outputISATAB.getAllSheets()) {
                                s.setRowsToDefaultColor();
                            }
                            // proceed to subsequent tasks...
                            switch (type) {
                                case SAVE_MAIN:

                                    saveProfilesAndGoToMain();
                                    break;

                                case SAVE_LOGOUT:
                                    saveProfilesAndLogout();
                                    break;

                                case SAVE_EXIT:
                                    saveProfilesAndExit();
                                    break;

                                default:
                                    //
                            }
                        }
                    }
                });
            }
        }
    }

    class ScreenResizeAction extends AbstractAction {
        static final int FULL_SCREEN = 0;
        static final int DEFAULT_SIZE = 1;
        int type;

        /**
         * ScreenResizeAction constructor.
         *
         * @param type     - type of screen resize to be performed, either full screen or default screen...
         * @param text     - Text to be displayed in component
         * @param icon     - Icon to be displayed in component
         * @param desc     - description of the components purpose
         * @param mnemonic - shortcut key to be used!
         */
        public ScreenResizeAction(int type, String text, ImageIcon icon,
                                  String desc, Integer mnemonic) {
            super(text, icon);
            this.type = type;
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent ae) {
            switch (type) {
                case FULL_SCREEN:

                    if (!glass.isVisible()) {
                        setResizable(true);
                        setSize(Toolkit.getDefaultToolkit().getScreenSize());
                        repaint();
                    }

                    break;

                case DEFAULT_SIZE:

                    if (!glass.isVisible()) {
                        setResizable(true);
                        setSize(ISAcreator.APP_WIDTH, ISAcreator.APP_HEIGHT);
                        repaint();
                    }

                    break;
                default:
                    //go default...
            }
        }
    }

    public String[] getMeasurementEndpoints() {
        List<MappingObject> assayToTypeMapping = getMappings();
        Set<String> measTypeSet = new HashSet<String>();

        for (MappingObject mo : assayToTypeMapping) {
            if (!mo.getTechnologyType().equals("n/a") &&
                    !mo.getMeasurementEndpointType().equalsIgnoreCase("[sample]") && !mo.getMeasurementEndpointType().equalsIgnoreCase("[investigation]")) {
                measTypeSet.add(mo.getMeasurementEndpointType());
            }
        }

        List<String> tempMeasTypes = new ArrayList<String>();
        tempMeasTypes.addAll(measTypeSet);

        Collections.sort(tempMeasTypes);

        return tempMeasTypes.toArray(new String[tempMeasTypes.size()]);

    }

    public String[] getTechnologyTypes() {
        List<MappingObject> assayToTypeMapping = getMappings();
        Set<String> techTypeSet = new HashSet<String>();

        for (MappingObject mo : assayToTypeMapping) {
            if (!mo.getTechnologyType().equals("n/a") && !mo.getTechnologyType().equals("")) {
                techTypeSet.add(mo.getTechnologyType());
            }
        }

        List<String> tempTechTypes = new ArrayList<String>();
        tempTechTypes.addAll(techTypeSet);

        Collections.sort(tempTechTypes);

        tempTechTypes.add(0, AutoFilterComboCellEditor.BLANK_VALUE);

        return tempTechTypes.toArray(new String[tempTechTypes.size()]);
    }

    public Map<String, List<String>> getAllowedTechnologiesPerEndpoint() {
        Map<String, List<String>> measToAllowedTechs = new HashMap<String, List<String>>();

        for (MappingObject mo : mappings) {
            if (!measToAllowedTechs.containsKey(mo.getMeasurementEndpointType())) {
                measToAllowedTechs.put(mo.getMeasurementEndpointType(), new ArrayList<String>());
            }
            measToAllowedTechs.get(mo.getMeasurementEndpointType()).add(mo.getTechnologyType());
        }

        return measToAllowedTechs;
    }

    /**
     * Select the TableReferenceObject which is required for a given measurement endpoint
     * and technology type using the MappingObject.
     *
     * @param measurementEndpoint - e.g. Gene Expression
     * @param techType            e.g. DNA Microarray
     * @return TableReferenceObject if one exists, null otherwise.
     */
    public synchronized TableReferenceObject selectTROForUserSelection(
            String measurementEndpoint, String techType) {
        for (MappingObject mo : mappings) {
            if (mo.getMeasurementEndpointType().equalsIgnoreCase(measurementEndpoint) &&
                    mo.getTechnologyType().equalsIgnoreCase(techType)) {
                for (TableReferenceObject tro : assayDefinitions) {
                    if (tro.getTableName().equalsIgnoreCase(mo.getAssayName())) {
                        return tro;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Select the TableReferenceObject which is required for a given tableType
     *
     * @param tableType - e.g. study sample or investigation
     * @return TableReferenceObject if one exists, null otherwise.
     */
    public synchronized TableReferenceObject selectTROForUserSelection(
            String tableType) {
        for (MappingObject mo : mappings) {

            if (mo.getTableType().equals(tableType)) {
                for (TableReferenceObject tro : assayDefinitions) {
                    if (tro.getTableName().equalsIgnoreCase(mo.getAssayName())) {
                        return new TableReferenceObject(tro.getTableFields());
                    }
                }
            }
        }

        return null;
    }


    public IncorrectColumnOrderGUI getIncorrectGUI() {
        return incorrectGUI;
    }

    public Mode getMode() {
        return mode;
    }

    private void closeISAcreator() {
        // checking for this instance should cover the case of the Wizard, Mapper, and main data entry screen
        if (currentPage instanceof AbstractDataEntryEnvironment) {

            JOptionPane optionPane = new JOptionPane("Are you sure you want to exit ISAcreator?",
                    JOptionPane.INFORMATION_MESSAGE,
                    JOptionPane.YES_NO_OPTION,
                    confirmExit);
            UIHelper.applyOptionPaneBackground(optionPane, UIHelper.BG_COLOR);
            optionPane.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    if (propertyChangeEvent.getPropertyName()
                            .equals(JOptionPane.VALUE_PROPERTY)) {
                        int lastOptionAnswer = Integer.valueOf(propertyChangeEvent.getNewValue()
                                .toString());
                        hideSheet();
                        if (lastOptionAnswer == JOptionPane.YES_OPTION) {
                            System.exit(0);
                        }
                    }
                }
            });

            showJDialogAsSheet(optionPane.createDialog(
                    ISAcreator.this, "Confirm"));
            // currently showing edit view, so ask for confirmation before exiting
        } else {
            saveProfilesAndExit();
        }
    }


    public static void main(String[] args) {


        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ISAcreator main = new ISAcreator(Mode.NORMAL_MODE);
                main.createGUI();
            }
        });
    }


}
