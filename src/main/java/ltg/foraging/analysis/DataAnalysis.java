package ltg.foraging.analysis;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;


public class DataAnalysis {


	public void importData() {
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream("/Users/tebemis/Desktop/Dropbox/Foraging_Data_Analysis/foraging_pilot_oct12_log.json");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = null;
			try {
				while ((strLine = br.readLine()) != null)   {
					// Parse into a JSON object
					System.out.println (strLine);
				}
			} catch (IOException e) {
				System.err.println("Impossible to parse file line");
			}
			in.close();

		} catch (FileNotFoundException e) {
			System.err.println("Impossible to open file");
		} catch (IOException e) {
			System.err.println("Impossible to close file");
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataAnalysis da = new DataAnalysis();
		da.importData();
	}

}
