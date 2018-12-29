import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

public class Journal {
	private String journalId, patientId, doctorId, nurseId, hospitalDivision, info;
	private Path path;

	Journal(String journalId) {
		this.journalId = journalId;
		this.path = Paths.get("./server/journals/" + this.journalId + ".txt");
	}

	public String getJournalId() {
		return journalId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public void setDoctorId(String doctorId) {
		this.doctorId = doctorId;
	}

	public void setNurseId(String nurseId) {
		this.nurseId = nurseId;
	}

	public void populate(Path path) throws IOException {
		Files.lines(path).forEach(s -> {
			String[] fields = s.split(":");
			switch (fields[0]) {
				case "patient":
					this.patientId = fields[1];
					break;
				case "nurse":
					this.nurseId = fields[1];
					break;
				case "doctor":
					this.doctorId = fields[1];
					break;
				case "division":
					this.hospitalDivision = fields[1];
					break;
				case "info":
					this.info = fields[1];
					break;
			}
		});
	}

	public String getUserId(UserGroup userGroup) {
		switch (userGroup) {
			case PATIENT:
				return patientId;
			case NURSE:
				return nurseId;
			case DOCTOR:
				return doctorId;
			default:
				return null;
		}
	}

	public String getHospitalDivision() {
		return hospitalDivision;
	}

	public void setHospitalDivision(String hospitalDivision) {
		this.hospitalDivision = hospitalDivision;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public boolean writeInfo(String newInfo) {
		try {
			newInfo = "¤" + newInfo;
			Files.write(this.path, newInfo.getBytes(), StandardOpenOption.APPEND);
			info = info + newInfo;
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean createFile() {
		LinkedList<String> lines = new LinkedList<String>();
		try {
			lines.add("patient:" + this.patientId);
			lines.add("doctor:" + this.doctorId);
			lines.add("nurse:" + this.nurseId);
			lines.add("division:" + this.hospitalDivision);
			lines.add("info:" + this.info);
			Files.write(this.path, lines, Charset.forName("UTF-8"));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean deleteFile() {
		try {
			Files.deleteIfExists(this.path);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return journalId;
	}
}