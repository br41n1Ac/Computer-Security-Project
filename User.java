import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

public class User {
	private boolean loggedIn;
	private String userId, hospitalDivision, username;
	private byte[] salt, hash;
	private UserGroup userGroup;

	/**
	 * Userobjekt, representerar en user
	 */
	User() {
		loggedIn = false;
	}

	public String getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public void populate(String[] fields) {
		this.username = fields[0];
		this.userId = fields[2];
		this.hospitalDivision = fields[3];
		this.userGroup = UserGroup.getUserGroup(fields[4]);
		String[] saltAndHash = fields[1].split(",");
		this.salt = new byte[16];
		this.hash = new byte[saltAndHash.length - 16];
		for (int i = 0; i < saltAndHash.length; i++) {
			byte b = Byte.parseByte(saltAndHash[i]);
			if (i < 16) {
				this.salt[i] = b;
			} else {
				this.hash[i-16] = b;
			}
		}
		loggedIn = true;
	}

	
	public boolean isLoggedIn() {
		return loggedIn;
	}

	public boolean hasReadPermission(Journal journal) {
		if(this.userGroup == UserGroup.GOVERNMENT) {
			return true;
		}
		if (journal.getUserId(this.userGroup).equals(this.userId)) {
			return true;
		} else {
			switch (this.userGroup) {
				case NURSE:
				case DOCTOR:
					return journal.getHospitalDivision().equals(this.hospitalDivision);
				case GOVERNMENT:
					return true;
				default:
					return false;
			}
		} 

	}

	public boolean hasWritePermission() {
		return this.userGroup == UserGroup.DOCTOR;
	}
 
	public boolean hasCreatePermission() {
		return this.userGroup == UserGroup.DOCTOR;
	}

	public boolean hasDeletePermission() {
		return this.userGroup == UserGroup.GOVERNMENT;
	}

	public boolean checkCredentials(String username, String password) {
		password = new String(this.salt) + password;
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (Exception e) {
			e.printStackTrace();
		}
		byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
		return (this.username.equals(username) && Arrays.equals(this.hash, hash));
	}

	@Override
	public String toString() {
		return userId;
	}
}
