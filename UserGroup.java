public enum UserGroup {
	PATIENT("pat"), NURSE("nur"), DOCTOR("doc"), GOVERNMENT("gov");

	private String groupCode;

	UserGroup(String groupCode) {
		this.groupCode = groupCode;
	}

	@Override
	public String toString() {
		return groupCode;
	}

	public static UserGroup getUserGroup(String groupCode) {
		if (PATIENT.matches(groupCode)) {
			return PATIENT;
		} else if (NURSE.matches(groupCode)) {
			return NURSE;
		} else if (DOCTOR.matches(groupCode)) {
			return DOCTOR;
		} else if (GOVERNMENT.matches(groupCode)) {
			return GOVERNMENT;
		}
		return null;
	}
	

	private boolean matches(String groupCode) {
		return this.groupCode.equals(groupCode);
	}
} 
