package inspector.jmondb.model;

/*
 * #%L
 * jMonDB Core
 * %%
 * Copyright (C) 2014 InSPECtor
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public enum InstrumentModel {

	/* Thermo instruments */
	THERMO_LTQ("MS:1000447"),
	THERMO_LTQ_ORBITRAP("MS:1000449"),
	THERMO_ORBITRAP_XL("MS:1000556"),
	THERMO_LTQ_VELOS("MS:1000855"),
	THERMO_TSQ_VANTAGE("MS:1001510"),
	THERMO_ORBITRAP_VELOS("MS:1001742"),
	THERMO_Q_EXACTIVE("MS:1001911"),
	THERMO_ORBITRAP_FUSION("MS:1002416"),

	/* general instruments */
	UNKNOWN_MODEL("MS:1000031");

	private final String cvAccession;

	InstrumentModel(String accession) {
		this.cvAccession = accession;
	}

	@Override
	public String toString() {
		return cvAccession;
	}

	public static InstrumentModel fromString(String text) {
		if(text != null)
			for(InstrumentModel model : values())
				if(text.equals(model.toString()))
					return model;

		return UNKNOWN_MODEL;
	}
}
