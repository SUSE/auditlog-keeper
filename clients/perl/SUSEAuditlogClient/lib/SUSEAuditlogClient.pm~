package SUSEAuditlogClient;

use 5.006001;
use strict;
use warnings;
#require Exporter;

#our @ISA = qw(Exporter);
#our %EXPORT_TAGS = ( 'all' => [ qw(
#	) ] );
#
#our @EXPORT_OK = ( @{ $EXPORT_TAGS{'all'} } );
#
#our @EXPORT = qw(	
#);
#
#our $VERSION = '0.01';
#
#
# Preloaded methods go here.

sub new {
    my $self = {};
    $self->{HOST} = undef;
    $self->{PORT} = undef;
    bless($self);

    return $self;
}


sub set_host {
    my $self = shift;
    $self->{HOST} = shift;
}


sub set_port {
    my $self = shift;
    $self->{PORT} = shift;
}


sub get_connection_url {
    my $self = shift;
    return "http://" . $self->{HOST} . ":" . $self->{PORT};
}

1;
__END__

=head1 NAME

SUSEAuditlogClient - Perl extension client for the SUSE AuditLog Keeper.

=head1 SYNOPSIS

  use SUSEAuditlogClient;

=head1 DESCRIPTION

This is an XML-RPC client for the SUSE AuditLog Keeper.

=head2 EXPORT

None by default.



=head1 SEE ALSO

Please visit: http://www.suse.com/products/suse-manager

=head1 AUTHOR

Bo Maryniuk (bo@suse.de)

=head1 COPYRIGHT AND LICENSE

Author: Bo Maryniuk (bo@suse.de)

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself, either Perl version 5.12.1 or,
at your option, any later version of Perl 5 you may have available.

=cut
