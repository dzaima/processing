package processing.app.ui;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class DarkScrollBarUI extends BasicScrollBarUI {
  public DarkScrollBarUI() { }
  
  
  @Override
  protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
    Graphics2D g2g = (Graphics2D) g;
    g2g.setColor(new Color(0xff444444));
    g2g.fill(thumbBounds);
    //super.paintThumb(g, c, thumbBounds);
  }
  
  @Override
  protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
    Graphics2D g2g = (Graphics2D) g;
    g2g.setColor(new Color(0xff292929));
    g2g.fill(trackBounds);
    // super.paintTrack(g, c, trackBounds);
    // g2g.dispose();
  }
  private JButton createZeroButton() {
    JButton button = new JButton("zero button");
    Dimension zeroDim = new Dimension(0,0);
    button.setPreferredSize(zeroDim);
    button.setMinimumSize(zeroDim);
    button.setMaximumSize(zeroDim);
    return button;
  }
  
  @Override
  protected JButton createDecreaseButton(int orientation) {
    return createZeroButton();
  }
  
  @Override
  protected JButton createIncreaseButton(int orientation) {
    return createZeroButton();
  }
}