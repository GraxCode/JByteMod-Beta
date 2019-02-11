package me.grax.jbytemod.ui;

import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import com.alee.global.StyleConstants;
import com.alee.laf.button.WebButton;

import me.grax.jbytemod.JByteMod;

public class MyToolBar extends JToolBar {
  private MyMenuBar menubar;

  public MyToolBar(JByteMod jbm) {
    this.menubar = (MyMenuBar) jbm.getJMenuBar();
    this.setFloatable(false);
    if (!menubar.isAgent()) {
      this.add(makeNavigationButton(JByteMod.res.getResource("load"), getIcon("load"), e -> {
        menubar.openLoadDialogue();
      }));
      this.add(makeNavigationButton(JByteMod.res.getResource("save"), getIcon("save"), e -> {
        if (menubar.getLastFile() != null) {
          jbm.saveFile(menubar.getLastFile());
        } else {
          menubar.openSaveDialogue();
        }
      }));
    } else {
      this.add(makeNavigationButton(JByteMod.res.getResource("reload"), getIcon("reload"), e -> {
        jbm.refreshAgentClasses();
      }));
      this.add(makeNavigationButton(JByteMod.res.getResource("apply"), getIcon("save"), e -> {
        jbm.applyChangesAgent();
      }));
    }
    this.addSeparator();
    this.add(makeNavigationButton(JByteMod.res.getResource("search"), getIcon("search"), e -> {
      menubar.searchLDC();
    }));
    this.addSeparator();
    this.add(makeNavigationButton("Access Helper", getIcon("table"), e -> {
      new JAccessHelper().setVisible(true);
    }));
    this.add(makeNavigationButton("Attach to other process", getIcon("plug"), e -> {
      menubar.openProcessSelection();
    }));
  }

  private ImageIcon getIcon(String string) {
    		try {
			return new ImageIcon(Toolkit.getDefaultToolkit()
					.getImage(this.getClass().getResource("/resources/toolbar/" + string + ".png")));
		} catch (Throwable x) {
			x.printStackTrace();
			System.out.println("[Error] Load ImageIcon Failed.Try using empty ImageIcon to instead!");
			String emptyImage = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/4QBERXhpZgAATU0AKgAAAAgAAgENAAIAAAAWAAAAJgESAAMAAAABAAEAAAAAAAA8P1BIUCBQaHBpbmZvKCk7ID8+/xQA/9sAQwACAQECAQECAgICAgICAgMFAwMDAwMGBAQDBQcGBwcHBgcHCAkLCQgICggHBwoNCgoLDAwMDAcJDg8NDA4LDAwM/9sAQwECAgIDAwMGAwMGDAgHCAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwM/8AAEQgADAANAwEiAAIRAQMRAf/EAB8AAAEFAQEBAQEBAAAAAAAAAAABAgMEBQYHCAkKC//EALUQAAIBAwMCBAMFBQQEAAABfQECAwAEEQUSITFBBhNRYQcicRQygZGhCCNCscEVUtHwJDNicoIJChYXGBkaJSYnKCkqNDU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6g4SFhoeIiYqSk5SVlpeYmZqio6Slpqeoqaqys7S1tre4ubrCw8TFxsfIycrS09TV1tfY2drh4uPk5ebn6Onq8fLz9PX29/j5+v/EAB8BAAMBAQEBAQEBAQEAAAAAAAABAgMEBQYHCAkKC//EALURAAIBAgQEAwQHBQQEAAECdwABAgMRBAUhMQYSQVEHYXETIjKBCBRCkaGxwQkjM1LwFWJy0QoWJDThJfEXGBkaJicoKSo1Njc4OTpDREVGR0hJSlNUVVZXWFlaY2RlZmdoaWpzdHV2d3h5eoKDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uLj5OXm5+jp6vLz9PX29/j5+v/aAAwDAQACEQMRAD8A/fyiiigD/9k=";
			byte[] b = null;
			try {
				b = new sun.misc.BASE64Decoder().decodeBuffer(emptyImage);
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {
					b[i] += 256;
				}
			}
			return new ImageIcon(b);
		}
  }

  protected JButton makeNavigationButton(String action, ImageIcon i, ActionListener a) {
    JButton button = WebButton.createIconWebButton(i, StyleConstants.smallRound, true);
    button.setToolTipText(action);
    button.addActionListener(a);
    button.setFocusable(false);
    button.setBorderPainted(false);
    button.setRolloverEnabled(false);
    return button;
  }
}
