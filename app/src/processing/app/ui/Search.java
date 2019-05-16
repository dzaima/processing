/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2012-19 The Processing Foundation
  Copyright (c) 2004-12 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, version 2.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package processing.app.ui;

import processing.app.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


/**
 * Find & Replace window for the Processing editor.
 */
public class Search extends JFrame {
  Editor editor;
  
  private static final int BORDER = Platform.isMacOS() ? 20 : 13;
  
  private final JList<SearchResult> list;
  private final JTextField searchField;



  public Search(Editor editor) {
    super(Language.text("search"));
    this.editor = editor;

    Container pain = getContentPane();

    searchField = new JTextField(20);
  
    pain.setVisible(true);
    pain.setBackground(new Color(0x252525));
    
    GroupLayout layout = new GroupLayout(pain);
    pain.setLayout(layout);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);


  
  
    list = new JList<>();
    list.setVisibleRowCount(20);
    list.setBackground(new Color(0x252525));
    list.setForeground(new Color(0xd2d2d2));
  
  
    JScrollPane listScroll = new JScrollPane(list);
    listScroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
    listScroll.getHorizontalScrollBar().setUI(new DarkScrollBarUI());
    
    
    listScroll.setPreferredSize(list.getPreferredScrollableViewportSize());
    
    
    list.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        int index = list.locationToIndex(evt.getPoint());
        if (index != -1) results.get(index).select();
      }
    });
    
    searchField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { call(); }
      public void  removeUpdate(DocumentEvent e) { call(); }
      public void  insertUpdate(DocumentEvent e) { call(); }
      
      private void call() {
        updateList(searchField.getText());
      }
    });
    searchField.addActionListener(e -> {
      int index = list.getSelectedIndex();
      if (index != -1) results.get(index).select();
    });
    searchField.addKeyListener(new KeyListener() {
      @Override public void keyTyped(KeyEvent e) { }
      @Override public void keyReleased(KeyEvent e) { }
      @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == 65535) {
          int sz = results.size();
          int index = list.getSelectedIndex();
          if (sz == 0) return;
          switch (e.getKeyCode()) {
            case 38: // up
              list.setSelectedIndex(Math.floorMod(index-1, sz));
              break;
            case 40: // down
              list.setSelectedIndex(Math.floorMod(index+1, sz));
              break;
          }
        }
      }
    });
    
  
    list.setFont(list.getFont().deriveFont(16f));
  
  
  
    

    layout.setHorizontalGroup(layout
      .createSequentialGroup()
      .addGap(BORDER)
      .addGroup(layout.createParallelGroup()
        .addComponent(searchField)
        .addComponent(listScroll, GroupLayout.Alignment.CENTER))
      .addGap(BORDER)
    );


    layout.setVerticalGroup(layout.createSequentialGroup()
      .addGap(BORDER)
      .addComponent(searchField)
      .addComponent(listScroll)
      .addGap(BORDER)
    );

    setLocationRelativeTo(null); // center
    Dimension size = layout.preferredLayoutSize(pain);
    setSize(size.width, size.height);
    Dimension screen = Toolkit.getScreenSize();
    setLocation((screen.width - size.width) / 2,
                      (screen.height - size.height) / 2);




    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        handleClose();
      }
    });
    
    Toolkit.registerWindowCloseKeys(getRootPane(), actionEvent -> handleClose());
    Toolkit.setIcon(this);

    // hack to to get first field to focus properly on osx
    addWindowListener(new WindowAdapter() {
      public void windowActivated(WindowEvent e) {
        searchField.requestFocusInWindow();
        searchField.selectAll();
      }
    });
    pack();
    setResizable(true);
    setLocationRelativeTo(null);
    
    updateList("");
  }
  
  ArrayList<SearchResult> results;
  private void updateList(String search) {
    search = search.toLowerCase();
    results = new ArrayList<>();
    for (SketchCode c : editor.sketch.getCode()) {
      if (c.getFileName().toLowerCase().contains(search)) results.add(new SearchResult(c));
    }
    list.setModel(new AbstractListModel<SearchResult>() {
      @Override
      public int getSize() {
        return results.size();
      }

      @Override
      public SearchResult getElementAt(int i) {
        return results.get(i);
      }
    });
    if (results.size() > 0) list.setSelectedIndex(0);
  }
  
  class SearchResult {
  
    private final SketchCode tab;
  
    public SearchResult(SketchCode tab) {
      this.tab = tab;
    }
  
    @Override public String toString() {
      return tab.getFileName();
    }
  
    public void select() {
      handleClose();
      editor.sketch.setCurrentCode(tab);
    }
  }
  
  
  public void handleClose() {
    setVisible(false);
  }


  // look for the next instance of the find string to be found
  // once found, select it (and go to that line)
  private boolean find(boolean wrap) {
    final boolean allTabs = true;
    String searchTerm = searchField.getText();

    // this will catch "find next" being called when no search yet
    if (searchTerm.length() != 0) {
      String text = editor.getText();

      // Started work on find/replace across tabs. These two variables store
      // the original tab and selection position so that it knew when to stop
      // rotating through.
      Sketch sketch = editor.getSketch();
      int tabIndex = sketch.getCurrentCodeIndex();
//    int selIndex = backwards ?
//      editor.getSelectionStart() : editor.getSelectionStop();


      int nextIndex;
      //int selectionStart = editor.textarea.getSelectionStart();
      int selectionEnd = editor.getSelectionStop();
  
      nextIndex = text.indexOf(searchTerm, selectionEnd);
      if (nextIndex == -1) {
        // For searching in all tabs, wrapping always happens.

        int tempIndex = tabIndex;
        // Look for searchterm in all tabs.
        while (tabIndex <= sketch.getCodeCount() - 1) {
          if (tabIndex == sketch.getCodeCount() - 1) {
            // System.out.println("wrapping.");
            tabIndex = -1;
          }

          try {
            Document doc = sketch.getCode(tabIndex + 1).getDocument();
            if(doc != null) {
              text = doc.getText(0, doc.getLength()); // this thing has the latest changes
            }
            else {
              text = sketch.getCode(tabIndex + 1).getProgram(); // not this thing.
            }
          } catch (BadLocationException e) {
            e.printStackTrace();
          }
          tabIndex++;
          text = text.toLowerCase();
          nextIndex = text.indexOf(searchTerm, 0);

          if (nextIndex != -1  || tabIndex == tempIndex) {
            break;
          }
        }

        // searchterm wasn't found in any of the tabs.
        // No tab switching should happen, restore tabIndex
        if (nextIndex == -1) {
          tabIndex = tempIndex;
        }
      }
  
      if (nextIndex != -1) {
        sketch.setCurrentCode(tabIndex);
        editor.setSelection(nextIndex, nextIndex + searchTerm.length());
      }
  
      return nextIndex != -1;
    }
    return false;
  }





  /**
   * Returns true if find next/previous will work, for graying-
   * out of menu items.
   */
  public boolean canFindNext() {
    return searchField.getText().length() != 0;
  }
}
