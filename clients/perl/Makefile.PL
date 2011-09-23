use 5.006001;
use ExtUtils::MakeMaker;
WriteMakefile(
    NAME              => 'SUSEAuditlogClient',
    VERSION_FROM      => 'lib/SUSEAuditlogClient.pm',
    PREREQ_PM         => {'Test'        =>  0, },
    ($] >= 5.005 ?
      (ABSTRACT_FROM  => 'lib/SUSEAuditlogClient.pm',
       AUTHOR         => 'Bo Maryniuk <bo@suse.de>') : ()),
);
