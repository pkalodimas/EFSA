package soap;

/**
 * This class contains all the xml messages which need to be
 * attached to the uploadCatalogueFile message to make the
 * related operations, as the reserve, unreserve...
 * @author avonva
 * @author shahaal
 */
public class UploadMessages {

	public static String get(UploadCatalogueFileAction action, String catalogueCode, String description) {
		
		String message = null;
		
		switch(action) {
		case PUBLISH_MAJOR:
		case PUBLISH_MINOR:
			message = getPublishMessage(action, catalogueCode);
			break;
		case RESERVE_MAJOR:
		case RESERVE_MINOR:
		case UNRESERVE:
			message = getReserveMessage(action, catalogueCode, description);
			break;
		default:
			break;
		}
		
		return message;
	}
	
	/**
	 * Get the message to reserve/unreserve a catalogue
	 * @return
	 */
	private static String getReserveMessage(UploadCatalogueFileAction action, String catalogueCode, String description) {

		String op = null;
		
		if (action == UploadCatalogueFileAction.RESERVE_MAJOR)
			op = "reserveMajor";
		else if (action == UploadCatalogueFileAction.RESERVE_MINOR)
			op = "reserveMinor";
		else if (action == UploadCatalogueFileAction.UNRESERVE)
			op = "unreserve";
		else
			return null;
		
		description = description == null ? "Missing reservation note" : description;
		
		String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<message xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:noNamespaceSchemaLocation=\"file:///D:/cat_xsd/UploadCatalogue.xsd\">"
				+ "<updateCatalogue catalogueCode=\"" + catalogueCode + "\">"
				+ "<" + op + ">"
				+ "<reservationNote>" + description + "</reservationNote>"
				+ "</" + op + ">"
				+ "</updateCatalogue>"
				+ "</message>";

		return message;
	}
	
	private static String getPublishMessage(UploadCatalogueFileAction action, String catalogueCode) {

		String op = action == UploadCatalogueFileAction.PUBLISH_MAJOR ? "publishMajor" : "publishMinor";
		
		String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<message xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:noNamespaceSchemaLocation=\"file:///D:/cat_xsd/UploadCatalogue.xsd\">"
				+ "<" + op
				+ " catalogueCode=\"" + catalogueCode + "\">"
				+ "</" + op + ">"
				+ "</message>";

		return message;
	}
}
