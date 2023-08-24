package dcf_log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;

import ack.IDcfAckLog;
import ack.MessageValResCode;
import ack.OkCode;
import ack.OpResError;
import dataset.DcfDatasetStatus;

public class DcfAckLogMock implements IDcfAckLog {

	private String dcCode;
	private MessageValResCode messageValResCode;
	private OkCode opResCode;
	private Collection<String> opResLog;
	private OpResError opResError;
	private String datasetId;
	private DcfDatasetStatus datasetStatus;
	private String detailedAckResId;
	
	public DcfAckLogMock(OkCode opResCode, DcfDatasetStatus status) {
		this.opResCode = opResCode;
		this.datasetStatus = status;
	}
	
	/**
	 * 
	 * @param dcCode
	 * @param messageValResCode
	 * @param opResCode
	 * @param datasetId
	 * @param datasetStatus
	 */
	public DcfAckLogMock(String dcCode, MessageValResCode messageValResCode, OkCode opResCode, 
			String datasetId, DcfDatasetStatus datasetStatus) {
		this.dcCode = dcCode;
		this.messageValResCode = messageValResCode;
		this.opResCode = opResCode;
		this.datasetId = datasetId;
		this.datasetStatus = datasetStatus;
	}
	
	/**
	 * shahaal
	 * @param dcCode
	 * @param messageValResCode
	 * @param opResCode
	 * @param datasetId
	 * @param datasetStatus
	 */
	public DcfAckLogMock(String dcCode, String detailedAckResId, OkCode opResCode, 
			String datasetId, DcfDatasetStatus datasetStatus) {
		this.dcCode = dcCode;
		this.detailedAckResId = detailedAckResId;
		this.opResCode = opResCode;
		this.datasetId = datasetId;
		this.datasetStatus = datasetStatus;
	}
	
	public void setDcCode(String dcCode) {
		this.dcCode = dcCode;
	}
	public void setMessageValResCode(MessageValResCode messageValResCode) {
		this.messageValResCode = messageValResCode;
	}
	// shahaal, set the detailedAckResId from the ack file in temp
	// if the log status is in delivered
	public void setDetailedAckResId(String detailedAckResId) {
		this.detailedAckResId = detailedAckResId;
	}
	public void setOpResCode(OkCode opResCode) {
		this.opResCode = opResCode;
	}
	public void setOpResLog(Collection<String> opResLog) {
		this.opResLog = opResLog;
	}
	public void setOpResError(OpResError opResError) {
		this.opResError = opResError;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
	public void setDatasetStatus(DcfDatasetStatus datasetStatus) {
		this.datasetStatus = datasetStatus;
	}
	
	@Override
	public String getDCCode() {
		return this.dcCode;
	}

	@Override
	public MessageValResCode getMessageValResCode() {
		return this.messageValResCode;
	}
	
	// shahaal, set the detailedAckResId from the ack file in temp
	// if the log status is in delivered
	@Override
	public String getDetailedAckResId() {
		return this.detailedAckResId;
	}

	@Override
	public OkCode getOpResCode() {
		return this.opResCode;
	}

	@Override
	public boolean isOk() {
		if(this.opResCode == null)
			return false;
		
		return this.opResCode == OkCode.OK;
	}

	@Override
	public Collection<String> getOpResLog() {
		return this.opResLog;
	}

	@Override
	public boolean hasErrors() {
		return this.opResLog != null && !this.opResLog.isEmpty();
	}

	@Override
	public OpResError getOpResError() {
		return this.opResError;
	}

	@Override
	public String getDatasetId() {
		return this.datasetId;
	}

	@Override
	public DcfDatasetStatus getDatasetStatus() {
		return this.datasetStatus;
	}

	@Override
	public InputStream getRawLog() {
		File file = new File("test-files" + System.getProperty("file.separator") + "ack.xml");
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getMessageValResText() {
		return "This is the message val res text error";
	}
}
