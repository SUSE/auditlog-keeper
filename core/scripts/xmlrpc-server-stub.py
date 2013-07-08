#!/usr/bin/python

from datetime import datetime
from SimpleXMLRPCServer import SimpleXMLRPCServer
from SimpleXMLRPCServer import SimpleXMLRPCRequestHandler

# Restrict to a particular path.
class RequestHandler(SimpleXMLRPCRequestHandler):
    rpc_paths = ('/')

# Create server
server = SimpleXMLRPCServer(("localhost", 6888),
                            requestHandler=RequestHandler,
                            logRequests=False,
                            allow_none=True)
server.register_introspection_functions()

# Create the root object and operations
class ServiceRoot:
    pass

class Operations:
    def test(self, name):
        print "Calling test, param:" + str(name)
	return "Hello, " + str(name)

    def testytest(self, a, b, c):
        print "Testytest: " + str(a) + ", " + str(b) + ", " + str(c)
	return str(a) + str(b) + str(c)

    def ping(self):
        now = datetime.now()
        print str(now) + " - PING"
        return now

    def log(self, uid, message, host, extmap=None):
        now = datetime.now()
        print str(now) + " - " + uid + " from [" + host + "]: " + message + " " + str(extmap or "")

# Register operations in namespace 'audit'
root = ServiceRoot();
root.audit = Operations();
server.register_instance(root, allow_dotted_names=True)

# Run the server's main loop
server.serve_forever()
