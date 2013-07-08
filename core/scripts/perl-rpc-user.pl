#!/usr/bin/perl

use SUSEAuditlogClient;
use strict;
use warnings;

my $client = SUSEAuditlogClient->new("localhost", 6888);
my %extmap = ("EVT.TYPE" => "ADD_USER");
#print $client->log(undef, 'hello, world!', 'eucavm038.suse.de', \%extmap) . "\n";
print $client->log(undef, 'hello, world!', undef, \%extmap) . "\n";
#print $client->log(undef, 'no extmap this time', 'eucavm038.suse.de', undef) . "\n";

