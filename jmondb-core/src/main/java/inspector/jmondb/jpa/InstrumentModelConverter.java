package inspector.jmondb.jpa;

import inspector.jmondb.model.InstrumentModel;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply=true)
public class InstrumentModelConverter implements AttributeConverter<InstrumentModel, String> {

	@Override
	public String convertToDatabaseColumn(InstrumentModel instrumentModel) {
		return instrumentModel.toString();
	}

	@Override
	public InstrumentModel convertToEntityAttribute(String s) {
		return InstrumentModel.fromString(s);
	}
}
