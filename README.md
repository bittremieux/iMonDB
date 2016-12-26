# iMonDB

The iMonDB (**I**nstrument **MON**itoring **D**ata**B**ase) tool suite consists of a highly efficient database structure and associated software tools to automatically extract, store, and manage mass spectrometry instrument parameters from raw data files.

Instrument parameters give a detailed account on the operation of a mass spectrometer, which can subsequently give insight into the observations in the mass spectral data. The advantage of instrument information at the lowest level is the high sensitivity to detect emerging defects in a timely fashion. The iMonDB enables the monitoring of instrument parameters over a considerable time period, which fosters an additional approach to mass spectrometry quality control.

The iMonDB tool suite consists of the following components:

* **iMonDB MySQL database**: a highly optimized database structure to store instrument parameters
* **iMonDB Collector**: a schedulable tool to keep the iMonDB up to date by automatically storing new instrument parameters in the database
* **iMonDB Viewer**: a tool to visualize the evolution of individual instrument parameters over time
* **iMonDB Core**: a high-level Java API for use by developers who wish to include instrument monitoring capabilities in their own software

The various iMonDB implementations are released as open source under the permissive Apache 2.0 license. If you use iMonDB as part of your work, please cite the following publication:

* Bittremieux, W., Willems, H., Kelchtermans, P., Martens, L., Laukens, K., and Valkenborg, D. **iMonDB: Mass spectrometry quality control through instrument monitoring.** *Journal of Proteome Research* (2015). doi:[10.1021/acs.jproteome.5b00127](http://pubs.acs.org/doi/abs/10.1021/acs.jproteome.5b00127)
