package inspector.jmondb.jpa;

import inspector.jmondb.model.EventType;
import inspector.jmondb.model.InstrumentModel;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply=true)
public class EventTypeConverter implements AttributeConverter<EventType, String> {

	@Override
	public String convertToDatabaseColumn(EventType eventType) {
		return eventType.toString();
	}

	@Override
	public EventType convertToEntityAttribute(String s) {
		EventType[] types = EventType.values();
		for(EventType type : types)
			if(type.toString().equals(s))
				return type;
		return null;
	}
}
