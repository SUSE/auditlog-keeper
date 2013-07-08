#!/usr/bin/python

import xmlrpclib

server = xmlrpclib.ServerProxy("http://10.11.138.23:6888/")
#server = xmlrpclib.ServerProxy("http://eucavm055.suse.de:6888/")

extmap = {'EVT.TYPE' : 'ADD_USER', 'EVT.SRC' : 'BACKEND_API',
       'REQ.foo' : 'bar', 'REQ.spam' : 'baz', 'REQ.name' : 'fred',
       'EVT.URL' : 'http://www.google.com/',}

print server.audit.log("bo", "Added user \"pitchfork\"", "10.10.0.78", extmap)

