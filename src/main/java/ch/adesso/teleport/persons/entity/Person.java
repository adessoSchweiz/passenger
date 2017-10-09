package ch.adesso.teleport.persons.entity;

import ch.adesso.teleport.AggregateRoot;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Person extends AggregateRoot {

	enum EventType {
		PERSON_CREATED("person_created"), FIRSTNAME_CHANGED("firstname_changed"), LASTNAME_CHANGED(
				"lastname_changed"), BIRTHDAY_CHANGED("birthday_changed"), PERSON_STATUS_CHANGED(
						"person_status_changed"), EMAIL_CHANGED("email_changed"), MOBIL_NO_CHANGED("mobil_no_changed");

		private String type;

		private EventType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type;
		}
	}

	private String firstname;
	private String lastname;
	private String birthday;
	private PersonStatus status;
	private String mobil;
	private String email;

	public Person(String id) {
		super();
		applyChange(EventType.PERSON_CREATED, id, getId());
	}

	public void updateFrom(Person passenger) {
		if (passenger.getId() != null) {
			setId(passenger.getId());
		}
		setVersion(passenger.getVersion());
		firstName(passenger.getFirstname());
		lastName(passenger.getLastname());
		birthday(passenger.getBirthday());
		status(passenger.getStatus());
		emailAddress(passenger.getEmail());
		mobileNumber(passenger.getMobil());
	}

	public void firstName(String firstName) {
		applyChange(EventType.FIRSTNAME_CHANGED, firstName, getFirstname());
	}

	public void lastName(String lastName) {
		applyChange(EventType.LASTNAME_CHANGED, lastName, getLastname());
	}

	public void birthday(String birthday) {
		applyChange(EventType.BIRTHDAY_CHANGED, birthday, getBirthday());
	}

	public void status(PersonStatus status) {
		applyChange(EventType.PERSON_STATUS_CHANGED, status != null ? status.toString() : null,
				getStatus() != null ? getStatus().toString() : null);
	}

	public void mobileNumber(String mobilNumber) {
		applyChange(EventType.MOBIL_NO_CHANGED, mobilNumber, getMobil());
	}

	public void emailAddress(String emailAddress) {
		applyChange(EventType.EMAIL_CHANGED, emailAddress, getEmail());
	}

	@Override
	protected void initHandlers() {
		addHandler(EventType.PERSON_CREATED, e -> this.setId(e.getValue()));
		addHandler(EventType.FIRSTNAME_CHANGED, e -> this.setFirstname(e.getValue()));
		addHandler(EventType.LASTNAME_CHANGED, e -> this.setLastname(e.getValue()));
		addHandler(EventType.BIRTHDAY_CHANGED, e -> this.setBirthday(e.getValue()));
		addHandler(EventType.MOBIL_NO_CHANGED, e -> this.setMobil(e.getValue()));
		addHandler(EventType.EMAIL_CHANGED, e -> this.setEmail(e.getValue()));
		addHandler(EventType.PERSON_STATUS_CHANGED.toString(), e -> this.setStatus(e.toEnum(PersonStatus.class)));
	}

}
