#!/usr/bin/perl

use YAML::Syck;
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
    my %event = ();

    if (!((-e $path) && (-r $path))) {
	return \%event;
    }
    
    my $yaml = YAML::Syck::LoadFile($path);
    my $urlopts = ${$yaml}{$uri};

    if (defined($urlopts)) {
	$event{"EVT.TYPE"} = ${$urlopts}{type};

	for my $el (@{${$urlopts}{required}}) {
	    print $el . "\n";
	}

	print "Length: " . ($#{${$urlopts}{required}} == 0) . "\n";
    }

    return \%event;
}


# Test
my $event = get_log_event($ARGV[0], '/network/systems/details/remote_commands.pxt');

if (keys(%{$event}) > 0) {
    test($event);
}

my %x = (twat => 1);
if (defined($x{twat})) {
    print "do it\n";
} else {
    print "no joy\n";
}
