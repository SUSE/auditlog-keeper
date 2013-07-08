#!/usr/bin/perl

use YAML::Tiny;
use warnings;
use strict;


sub test {
    my $h = shift;
    for my $k (keys %{$h}) {
	my $v = ${$h}{$k};
	print "$k => $v\n";
    }
}


sub get_log_event {
    my ($path, $uri) = @_;
    my $conf = YAML::Tiny->read($path);
    my %event = ();
    for (my $i = 0; $i < ($#{$conf} + 1); $i++) {
	my $urlopts = $conf->[$i]{$uri};
	if (defined($urlopts)) {
	    #print ${$urlopts}{required} . "\n";
	    $event{"EVT.TYPE"} = ${$urlopts}{type};
	}
    }
    
    return \%event;
}

# test
my $event = get_log_event($ARGV[0], '/network/systems/details/remote_commands.pxt');
if (keys(%{$event}) > 0) {
    test($event);
}

