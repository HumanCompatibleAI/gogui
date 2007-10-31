// GameInfoPanel.java

package net.sf.gogui.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import net.sf.gogui.game.ConstClock;
import net.sf.gogui.game.ConstGame;
import net.sf.gogui.game.ConstGameInfo;
import net.sf.gogui.game.ConstGameTree;
import net.sf.gogui.game.ConstNode;
import net.sf.gogui.game.Clock;
import net.sf.gogui.game.Game;
import net.sf.gogui.game.StringInfoColor;
import net.sf.gogui.go.BlackWhiteSet;
import net.sf.gogui.go.ConstBoard;
import net.sf.gogui.go.GoColor;
import static net.sf.gogui.go.GoColor.BLACK;
import static net.sf.gogui.go.GoColor.BLACK_WHITE;
import static net.sf.gogui.go.GoColor.WHITE_BLACK;
import net.sf.gogui.util.StringUtil;

/** Panel displaying information about the current position. */
public class GameInfoPanel
    extends JPanel
{
    public GameInfoPanel(Game game)
    {
        setBorder(GuiUtil.createEmptyBorder());
        JPanel panel =
            new JPanel(new GridLayout(0, 2, GuiUtil.PAD, GuiUtil.PAD));
        add(panel, BorderLayout.CENTER);
        m_game = game;
        for (GoColor c : WHITE_BLACK)
        {
            Box box = Box.createVerticalBox();
            panel.add(box);
            ImageIcon icon;
            if (c == BLACK)
                icon = GuiUtil.getIcon("gogui-black-32x32", "Black");
            else
                icon = GuiUtil.getIcon("gogui-white-32x32", "White");
            m_icon.set(c, new JLabel(icon));
            m_icon.get(c).setAlignmentX(Component.CENTER_ALIGNMENT);
            box.add(m_icon.get(c));
            box.add(GuiUtil.createFiller());
            m_clock.set(c, new GuiClock(c));
            m_clock.get(c).setAlignmentX(Component.CENTER_ALIGNMENT);
            box.add(m_clock.get(c));
            GoColor otherColor = c.otherColor();
            m_prisoners.set(otherColor, new Prisoners(otherColor));
            box.add(m_prisoners.get(otherColor));
        }
        Clock.Listener listener = new Clock.Listener() {
                public void clockChanged()
                {
                    SwingUtilities.invokeLater(m_updateTime);
                }
            };
        game.setClockListener(listener);
    }

    public void update(ConstGame game)
    {
        ConstBoard board = game.getBoard();
        ConstNode node = game.getCurrentNode();
        ConstGameTree tree = game.getTree();
        ConstGameInfo info = tree.getGameInfoConst(node);
        for (GoColor c : BLACK_WHITE)
        {
            String name = info.get(StringInfoColor.NAME, c);
            String rank = info.get(StringInfoColor.RANK, c);
            updatePlayerToolTip(m_icon.get(c), name, rank,
                                c.getCapitalizedName());
            m_prisoners.get(c).setCount(board.getCaptured(c));
        }
        // Usually time left information is stored in a node only for the
        // player who moved, so we check the father node too
        ConstNode father = node.getFatherConst();
        if (father != null)
            updateTimeFromNode(father);
        updateTimeFromNode(node);
    }

    public void updateTimeFromClock(ConstClock clock)
    {
        for (GoColor c : BLACK_WHITE)
            updateTimeFromClock(clock, c);
    }

    private class UpdateTimeRunnable
        implements Runnable
    {
        public void run()
        {
            updateTimeFromClock(m_game.getClock());
        }
    }

    private final BlackWhiteSet<GuiClock> m_clock
        = new BlackWhiteSet<GuiClock>();

    private final BlackWhiteSet<JLabel> m_icon
        = new BlackWhiteSet<JLabel>();

    private final BlackWhiteSet<Prisoners> m_prisoners
        = new BlackWhiteSet<Prisoners>();

    private final Game m_game;

    private final UpdateTimeRunnable m_updateTime = new UpdateTimeRunnable();

    private void updatePlayerToolTip(JLabel label, String player, String rank,
                                     String color)
    {
        StringBuilder buffer = new StringBuilder(128);
        buffer.append(color);
        buffer.append(" player (");
        if (StringUtil.isEmpty(player))
            buffer.append("unknown");
        else
        {
            buffer.append(player);
            if (! StringUtil.isEmpty(rank))
            {
                buffer.append(' ');
                buffer.append(rank);
            }
        }
        buffer.append(')');
        label.setToolTipText(buffer.toString());
    }

    private void updateTimeFromClock(ConstClock clock, GoColor c)
    {
        assert c.isBlackWhite();
        String text = clock.getTimeString(c);
        m_clock.get(c).setText(text);
    }

    private void updateTimeFromNode(ConstNode node)
    {
        for (GoColor c : BLACK_WHITE)
        {
            double timeLeft = node.getTimeLeft(c);
            if (! Double.isNaN(timeLeft))
            {
                int movesLeft = node.getMovesLeft(c);
                String text = Clock.getTimeString(timeLeft, movesLeft);
                m_clock.get(c).setText(text);
            }
        }
    }
}

class GuiClock
    extends JTextField
{
    public GuiClock(GoColor color)
    {
        super(COLUMNS);
        GuiUtil.setEditableFalse(this);
        setHorizontalAlignment(SwingConstants.CENTER);
        setMinimumSize(getPreferredSize());
        m_color = color;
        setText("00:00");
    }

    public final void setText(String text)
    {
        super.setText(text);
        String toolTip;
        if (m_color == BLACK)
            toolTip = "Time for Black";
        else
            toolTip = "Time for White";
        if (text.length() > COLUMNS)
            toolTip = toolTip + " (" + text + ")";
        setToolTipText(toolTip);
    }

    private static final int COLUMNS = 8;

    private final GoColor m_color;
}

class Prisoners
    extends JPanel
{
    public Prisoners(GoColor color)
    {
        m_color = color;
        Icon icon;
        if (color == BLACK)
            icon = GuiUtil.getIcon("gogui-black-16x16", "Black");
        else
            icon = GuiUtil.getIcon("gogui-white-16x16", "White");
        JLabel labelStone = new JLabel(icon);
        add(labelStone, BorderLayout.WEST);
        m_text = new JLabel();
        add(m_text, BorderLayout.CENTER);
        setCount(0);
    }

    public final void setCount(int n)
    {
        m_text.setText(Integer.toString(n));
        StringBuilder buffer = new StringBuilder(64);
        buffer.append(n);
        if (m_color == BLACK)
            buffer.append(" black");
        else
            buffer.append(" white");
        if (n == 1)
            buffer.append(" stone captured");
        else
            buffer.append(" stones captured");
        setToolTipText(buffer.toString());
    }

    private final JLabel m_text;

    private final GoColor m_color;
}
