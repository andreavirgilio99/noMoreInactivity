package noMoreInactivity;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import org.jnativehook.GlobalScreen;

public class EntryPoint {

	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	LogManager.getLogManager().reset();
				Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
				logger.setLevel(Level.OFF);
				
                TimerApplication timerApp = new TimerApplication();
                timerApp.pack();
                timerApp.setVisible(true);
            }
        });
	}
}
