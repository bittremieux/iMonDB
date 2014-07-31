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
		
		std::cout << "Instrument model CV-term\tMS:" << detail::translateAsInstrumentModel(rawFile->getInstrumentModel()) << std::endl;
		std::cout << "Sample date\t" << rawFile->getCreationDate() << std::endl << std::endl;
        
        // read all segments
        for(long segment = 0; /* end loop in catch */; ++segment)
        {
            try
            {
                std::auto_ptr<thermo::LabelValueArray> tuneData = rawFile->getTuneData(segment);
                
                for(int i = 0; i < tuneData->size(); ++i)
                    std::cout << tuneData->label(i) << '\t' << tuneData->value(i) << std::endl;
            }
            catch(thermo::RawEgg&)
            {
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
