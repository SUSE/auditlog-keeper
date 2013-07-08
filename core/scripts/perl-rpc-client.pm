#!/usr/bin/perl

require RPC::XML;
require RPC::XML::Client;

$client = RPC::XML::Client->new('http://127.0.0.1:6888/');
$request = RPC::XML::request->new('audit.log', 
				  RPC::XML::string->new('bo'),
				  RPC::XML::string->new('Hello from Perdel!'),
				  RPC::XML::string->new('10.20.30.40'),
				  RPC::XML::struct->new('EVT.TYPE' => 'ADD_USER')
    );
$response = $client->send_request($request);
print $response->value . "\n";

