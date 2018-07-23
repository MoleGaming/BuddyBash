package buddybash.internals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SettingsIO {
	private HashMap<String, Object> settings = new HashMap<String, Object>();
	private File settingsFile;
	private BufferedReader br;
	private BufferedWriter bw;

	public SettingsIO(String filePath) {
		filePath = this.getClass().getProtectionDomain().getCodeSource().getLocation() + "buddybash/settings/"+filePath;
		filePath = filePath.replaceAll("%20", " ").substring(6);
		settingsFile = new File(filePath);
		settings.clear();
		try {
			BufferedReader br = new BufferedReader(new FileReader(settingsFile));
			String line;
			line = br.readLine();
			while (line != null) {
				String[] args = line.split(",");
				args[0] = args[0].replaceAll(":0", ":");
				args[0] = args[0].replaceAll(":1", ",");
				args[1] = args[1].replaceAll(":0", ":");
				args[1] = args[1].replaceAll(":1", ",");
				settings.put(args[0], args[1]);
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Object getSetting(String settingName) {
		return settings.get(settingName);
	}

	public void setSetting(String settingName, Object value) {
		settings.put(settingName, value);
	}
	
	public boolean settingExists(String settingName){
		return settings.containsKey(settingName);
	}

	public void saveSettings() {
		try {
			bw = new BufferedWriter(new FileWriter(settingsFile));
			for (Entry<String, Object> entry : settings.entrySet()) {
				String setting = entry.getKey();
				String value = entry.getValue().toString();
				setting = setting.replaceAll("\\", "\\0");
				setting = setting.replaceAll(",", "\\1");
				value = value.replaceAll("\\", "\\0");
				value = value.replaceAll(",", "\\1");
				bw.write(setting+","+value);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
