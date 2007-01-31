//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package net.sf.gogui.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import net.sf.gogui.game.ConstNode;
import net.sf.gogui.game.ConstGameTree;

/** Dialog for displaying the game tree. */
public class GameTreeViewer
    extends JDialog
{
    /** Callback for events generated by GameTreeViewer. */
    public interface Listener
    {
        void actionBackward(int n);

        void actionBeginning();

        void actionEnd();

        void actionForward(int n);

        void actionGotoNode(ConstNode node);

        void actionNextEarlierVariation();

        void actionNextVariation();

        void actionPreviousEarlierVariation();

        void actionPreviousVariation();
    }

    public GameTreeViewer(Frame owner, Listener listener)
    {
        super(owner, "Tree");
        Container contentPane = getContentPane();
        m_listener = listener;
        m_panel = new GameTreePanel(this, listener,
                                    GameTreePanel.LABEL_NUMBER,
                                    GameTreePanel.SIZE_NORMAL);
        m_scrollPane =
            new JScrollPane(m_panel,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        GuiUtil.removeKeyBinding(m_scrollPane, "control END");
        KeyAdapter keyAdapter = new KeyAdapter()
            {
                public void keyReleased(KeyEvent e) 
                {
                    int c = e.getKeyCode();        
                    int mod = e.getModifiers();
                    if ((mod & m_shortcut) == 0)
                    {
                        if (c == KeyEvent.VK_HOME)
                            scrollToCurrent();
                        return;
                    }
                    boolean shift = ((mod & ActionEvent.SHIFT_MASK) != 0);
                    if (c == KeyEvent.VK_ENTER && ! shift)
                        m_panel.showPopup();
                    else if (c == KeyEvent.VK_LEFT && ! shift)
                        m_listener.actionBackward(1);
                    else if (c == KeyEvent.VK_LEFT && shift)
                        m_listener.actionBackward(10);
                    else if (c == KeyEvent.VK_RIGHT && ! shift)
                        m_listener.actionForward(1);
                    else if (c == KeyEvent.VK_RIGHT && shift)
                        m_listener.actionForward(10);
                    else if (c == KeyEvent.VK_DOWN && ! shift)
                        m_listener.actionNextVariation();
                    else if (c == KeyEvent.VK_DOWN && shift)
                        m_listener.actionNextEarlierVariation();
                    else if (c == KeyEvent.VK_UP && ! shift)
                        m_listener.actionPreviousVariation();
                    else if (c == KeyEvent.VK_UP && shift)
                        m_listener.actionPreviousEarlierVariation();
                    else if (c == KeyEvent.VK_HOME && ! shift)
                        m_listener.actionBeginning();
                    else if (c == KeyEvent.VK_END && ! shift)
                        m_listener.actionEnd();
                }

                private final int m_shortcut
                    = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            };
        m_scrollPane.addKeyListener(keyAdapter);
        m_panel.setScrollPane(m_scrollPane);
        m_scrollPane.setFocusable(true);
        m_scrollPane.setFocusTraversalKeysEnabled(false);
        JViewport viewport = m_scrollPane.getViewport();
        viewport.setBackground(GameTreePanel.BACKGROUND);
        contentPane.add(m_scrollPane, BorderLayout.CENTER);
        viewport.setFocusTraversalKeysEnabled(false);
        setFocusTraversalKeysEnabled(false);
        m_scrollPane.requestFocusInWindow();
        // Necessary for Mac Java 1.4.2, otherwise scrollpane will not have
        // focus after window is re-activated
        addWindowListener(new WindowAdapter() {
                public void windowActivated(WindowEvent e) {
                    m_scrollPane.requestFocusInWindow();
                }
            });
        setMinimumSize(new Dimension(128, 96));
        pack();
    }

    public void addNewSingleChild(ConstNode node)
    {
        m_panel.addNewSingleChild(node);
    }

    public void redrawCurrentNode()
    {
        m_panel.redrawCurrentNode();
    }

    public void scrollToCurrent()
    {
        m_panel.scrollToCurrent();
    }

    public void setLabelMode(int mode)
    {
        m_panel.setLabelMode(mode);
    }

    public void setSizeMode(int mode)
    {
        m_panel.setSizeMode(mode);
    }

    public void setShowSubtreeSizes(boolean enable)
    {
        m_panel.setShowSubtreeSizes(enable);
    }

    public void update(ConstGameTree gameTree, ConstNode currentNode)
    {
        Dimension size = m_scrollPane.getViewport().getSize();
        m_panel.update(gameTree, currentNode, size.width, size.height);
        repaint();
    }

    public void update(ConstNode currentNode)
    {
        Dimension size = m_scrollPane.getViewport().getSize();
        m_panel.update(currentNode, size.width, size.height);
    }

    /** Serial version to suppress compiler warning.
        Contains a marker comment for serialver.sourceforge.net
    */
    private static final long serialVersionUID = 0L; // SUID

    private final GameTreePanel m_panel;

    private final JScrollPane m_scrollPane;

    private final Listener m_listener;
}


