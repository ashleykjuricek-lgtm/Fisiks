package simulation;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Main {

        private Main() {
        }

        public static void main(String[] args) {
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                                final Fisiks app = new Fisiks();
                                app.init();

                                JFrame frame = new JFrame("Fisiks");
                                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                frame.setContentPane(app);
                                frame.pack();
                                frame.setLocationRelativeTo(null);
                                frame.setVisible(true);

                                app.start();

                                frame.addWindowListener(new WindowAdapter() {
                                        @Override
                                        public void windowClosing(WindowEvent e) {
                                                app.stop();
                                                app.destroy();
                                        }
                                });
                        }
                });
        }
}