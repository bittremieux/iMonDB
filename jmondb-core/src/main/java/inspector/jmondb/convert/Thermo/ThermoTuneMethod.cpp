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
// Original author: Matt Chambers <matt.chambers .@. vanderbilt.edu>
// Edited by: Pieter Kelchtermans <pieter.kelchtermans .@. ugent.be>
// Edited by: Wout Bittremieux <wout.bittremieux .@. uantwerpen.be>

#include <string>
#include <iostream>
#include <sstream>

#include <boost/filesystem.hpp>
#include <boost/algorithm/string.hpp>

#include "pwiz_aux/msrc/utility/vendor_api/thermo/RawFile.h"
namespace thermo = pwiz::vendor_api::Thermo;
#include "pwiz/data/vendor_readers/Thermo/Reader_Thermo_Detail.hpp"
namespace detail = pwiz::msdata::detail::Thermo;

int main(int argc, const char* argv[])
{
    if(argc != 2)
    {
        std::cerr << "ThermoTuneMethod extracts the tune method information from Thermo raw files." << std::endl;
        std::cerr << "Usage: ThermoTuneMethod <raw file>" << std::endl;
        return -1;
    }
	else if(!boost::filesystem::exists(argv[1]))
	{
		std::cerr << "File <" << argv[1] << "> does not exist" << std::endl;
		return -1;
	}
	else if(!boost::iequals(boost::filesystem::extension(argv[1]), ".raw"))
	{
		std::cerr << "File <" << argv[1] << "> is not a *.raw file" << std::endl;
		return -1;
	}

    try
    {
        // read raw file
        thermo::RawFilePtr rawFile = thermo::RawFile::create(argv[1]);

		// initialize raw file access
		rawFile->getInstrumentModel();

        // read all segments
        for(long segment = 0; /* end loop in catch */; ++segment)
        {
            try
            {
                std::auto_ptr<thermo::LabelValueArray> tuneData = rawFile->getTuneData(segment);
                
                for(int i = 0; i < tuneData->size(); ++i)
                    std::cout << tuneData->label(i) << '\t' << tuneData->value(i) << std::endl;

                std::cout << "--END_OF_SEGMENT_" << segment << std::endl;
            }
            catch(thermo::RawEgg& e)
            {
				//std::cerr << "Error: " << e.what() << std::endl;
                break;
            }
        }        
    }
    catch(std::exception& e)
    {
        std::cerr << "Error: " << e.what() << std::endl;
    }
    catch(...)
    {
        std::cerr << "Error: Unknown exception." << std::endl;
    }
    
    return 0;
}
