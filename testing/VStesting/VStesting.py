import socket
import sys
import time
import random

def print_usage():
	print "VStesting.py {HOST} {PORT} {MESSAGE_TYPE}\n\tMESSAGE_TYPE:"
	print "\n\n\t\t1: Sending string that simulates random data in the following form: <ID>:<random_value>;<ID>:<random_value>;<ID>:<random_value>;... Can be used by TemperaturesVS, ChartVS, ChartByIDVS"
	print "\n\n\t\t2-4:Sending string that simulates random data in the following form: ID:<sensor ID>;latitude:<lat_value>;longitude:<long_value>;<DataFieldName>:<DataFieldValue>;<DataFieldName>:<DataFieldValue>;... Can be used by SVGDataDisplayVS"
	
	
#Checking number of arguments
if len(sys.argv) != 4:
	print_usage()
	sys.exit(0)

#Checking host
try:
	socket.gethostbyname(sys.argv[1])
	HOST = sys.argv[1]
except socket.error:
	HOST = "localhost"
	print "Could not resolve host. Using HOST=\"localhost\""

#Checking port
try:
	PORT = int(sys.argv[2])
except:
	PORT = 5555
	print "Error parsing port. Using PORT=5555"
	
if PORT < 1:
	PORT = 5555
	print "Port must be between 1 and 65535. Using PORT=5555"

#Checking message type
try:
	MESSAGE_TYPE = int(sys.argv[3])
except:
	MESSAGE_TYPE = -1
	
if MESSAGE_TYPE < 1 or MESSAGE_TYPE > 4:
	print "Message type must be a number between 1 and 4. Exiting..."
	sys.exit(1)

#Generating data string	
elif MESSAGE_TYPE == 1:
	DATA = "1:%d;3:%d;4:%d;5:%d;7:%d\n" % ( random.randint(10,50), random.randint(-50,-20), random.randint(-30,15), random.randint(40,150), random.randint(60,110) )
	
elif MESSAGE_TYPE == 2:
	DATA="ID:2;latitude:46.025883;longitude:16.545794;Temperature:%.1f;Pressure:%.1f;Switch:%d\n" % ( random.uniform(-20, 50), random.uniform(850, 1200), random.randint(0, 1) )
	
elif MESSAGE_TYPE == 3:
	DATA="ID:3;latitude:45.892541;longitude:16.846878;Humidity:%.1f;Temperature:%.1f;Pressure:%.1f;Switch:%d\n" % ( random.uniform(10, 70), random.uniform(10, 45), random.uniform(850, 1200), random.randint(0, 1) )
	
elif MESSAGE_TYPE == 4:
	DATA="ID:4;latitude:45.764806;longitude:16.025544;Humidity:%.1f;Pressure:%.1f;Switch:%d\n" % ( random.uniform(10, 90), random.uniform(1000, 1200), random.randint(0, 1) )
	
else:
	print "Message type error. Not sending anything. Exiting..."
	exit(1)

#Connecting and sending
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((HOST, PORT))

#Receiving query
RECV_DATA = sock.recv(1024)
print "RECEIVED:\t", RECV_DATA

#Sending generated random data
print "SENT:\t\t", DATA
sock.send(DATA)

#Receiving answer
RECV_DATA = sock.recv(1024)
print "RECEIVED:\t", RECV_DATA

#Closing connection
sock.close()
