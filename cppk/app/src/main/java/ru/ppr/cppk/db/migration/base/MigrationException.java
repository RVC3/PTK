package ru.ppr.cppk.db.migration.base;

public class MigrationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7653674125616916500L;

	private final int toVersionNumber;
	private final int fromVersionNumber;

	public MigrationException(int fromVersionNumber, int toVersionNumber, Throwable cause) {
		super(cause);
		this.toVersionNumber = toVersionNumber;
		this.fromVersionNumber = fromVersionNumber;
	}

	public int getToVersionNumber() {
		return toVersionNumber;
	}
	
	public int getFromVersionNumber() {
		return fromVersionNumber;
	}

}
