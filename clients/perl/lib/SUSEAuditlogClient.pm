package SUSEAuditlogClient;

use 5.006001;
use strict;
use warnings;
use Sys::Hostname;
use Socket;

require RPC::XML;
require RPC::XML::Client;

# -----------------------
# Constructor. Accepts host and port params.
# Usage:
# my $client = SUSEAuditlogClient->new("localhost", 6888);
# -----------------------
sub new {
    shift;
    my $self = {};
    $self->{HOST} = shift;
    $self->{PORT} = shift;
    $self->{CONN} = undef;
    $self->{NAMESPACE} = "audit";
    $self->{SYSUSR} = `/usr/bin/whoami`;
    $self->{SYSUID} = `/usr/bin/id -u`;

    chomp($self->{SYSUSR});
    chomp($self->{SYSUID});

    bless($self);
    return $self;
}


# -----------------------
# &set_host
# Sets a hostname to the current client.
# -----------------------
sub set_host {
    my $self = shift;
    $self->{HOST} = shift;
}


# -----------------------
# &set_port
# Sets a port to the current client.
# -----------------------
sub set_port {
    my $self = shift;
    $self->{PORT} = shift;
}


# -----------------------
# &set_namespace
# Sets a namespace for an XML-RPC server.
# -----------------------
sub set_namespace {
    my $self = shift;
    $self->{NAMESPACE} = shift;
}


# -----------------------
# &get_connection_url
# Returns an URL for a connection to the remote XML-RPC server.
# -----------------------
sub get_connection_url {
    my $self = shift;
    return "http://" . $self->{HOST} . ":" . $self->{PORT};
}


# -----------------------
# &connect
# Connect to the XML-RPC server.
# -----------------------
sub connect {
    my $self = shift;
    $self->{CONN} = RPC::XML::Client->new($self->get_connection_url());
}


# -----------------------
# &disconnect
# Disconnect from the XML-RPC server.
# -----------------------
sub disconnect {
    my $self = shift;
    $self->{CONN} = undef;
}


# -----------------------
# &log
# Log to the Auditlog Keeper.
#
# Params:
#   log(uid, message, remote_host_ip, extmap);
#
# $uid
#   Is a string of user ID.
#
# $message
#   Is a string of required arbitrary message.
#
# $remote_host_ip
#   Is an IP address of the remote host.
#
# %extmap
#   Is a hash of map, that is built according
#   to the current logger specs.
# -----------------------
sub log {
    my $self = shift;
    $self->connect() if (!defined($self->{CONN}));
    my ($uid, $message, $remote_host, $extmap_ref) = @_;

    return $self->{CONN}->send_request(
	RPC::XML::request->new($self->{NAMESPACE} . '.log', 
			       RPC::XML::string->new(
				   (defined($uid) ? $uid
				    : ($self->{SYSUSR} . " (" . $self->{SYSUID} . ")"))),
			       RPC::XML::string->new(
				   (defined($message) ? $message
				    : die "Error: Log message has been missed.")),
			       RPC::XML::string->new(
				   (defined($remote_host) ? $remote_host
				    : inet_ntoa(scalar(gethostbyname(hostname())) || 'localhost'))),
			       (defined($extmap_ref) ? RPC::XML::struct->new(%{$extmap_ref}) 
				: RPC::XML::struct->new())))
	->value;
}


1;
__END__

=head1 NAME

SUSEAuditlogClient - Perl extension client for the SUSE AuditLog Keeper.

=head1 SYNOPSIS

  use SUSEAuditlogClient;

=head1 DESCRIPTION

This is an XML-RPC client for the SUSE AuditLog Keeper.

=head1 SEE ALSO

Please visit: http://www.suse.com/products/suse-manager

=head1 AUTHOR

Bo Maryniuk (bo@suse.de)

=head1 COPYRIGHT AND LICENSE

Author: Bo Maryniuk (bo@suse.de)

Copyright 2011 SUSE Linux Products GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

=cut
