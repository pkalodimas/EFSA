package data_collection;

import java.sql.Timestamp;

/**
 * Object which models a data collection
 * @author avonva
 * @author shahaal
 */
public class DcfDataCollection implements IDcfDataCollection {

	private int id = -1;
	private String code;
	private String description;
	private String category;
	private Timestamp activeFrom;
	private Timestamp activeTo;
	private String resourceId;

	@Override
	public void setId(int id) {
		this.id = id;
	}
	@Override
	public void setCode(String code) {
		this.code = code;
	}
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public void setCategory(String category) {
		this.category = category;
	}
	@Override
	public void setActiveFrom(Timestamp activeFrom) {
		this.activeFrom = activeFrom;
	}
	@Override
	public void setActiveTo(Timestamp activeTo) {
		this.activeTo = activeTo;
	}
	@Override
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	@Override
	public int getId() {
		return this.id;
	}
	@Override
	public String getCode() {
		return this.code;
	}
	@Override
	public String getDescription() {
		return this.description;
	}
	@Override
	public String getCategory() {
		return this.category;
	}
	@Override
	public Timestamp getActiveFrom() {
		return this.activeFrom;
	}
	@Override
	public Timestamp getActiveTo() {
		return this.activeTo;
	}
	@Override
	public String getResourceId() {
		return this.resourceId;
	}

	@Override
	public boolean isActive() {

		Timestamp today = new Timestamp(System.currentTimeMillis());

		boolean started = this.activeFrom.before(today);
		boolean notOver = this.activeTo.after(today);

		return started && notOver;
	}

	@Override
	public String toString() {
		return "DATA COLLECTION: id=" + (this.id == -1 ? "not defined yet" : this.id )
				+ ";code=" + this.code 
				+ ";description=" + this.description 
				+ ";category=" + this.category 
				+ ";activeFrom=" + this.activeFrom
				+ ";activeTo=" + this.activeTo
				+ ";resourceId=" + this.resourceId;
	}
}
