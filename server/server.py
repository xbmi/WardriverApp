#!/usr/bin/env python
# -*- coding: utf-8 -*-

import SocketServer
import json
import sys
import os
import MySQLdb as mdb

class EchoHandler(SocketServer.BaseRequestHandler) :
	def handle(self):
		print "Got connection from : " , self.client_address
		data = self.request.recv(2048)
		print "Client data:", data
		
		#print "Got connection from : " , self.client_address
		#data = "foo"
		#while len(data):
		#	data = self.request.recv(1024)
		#	print "Client data:", data
		#	self.request.send(data)
		#print "Client left"
		#sys.exit()
		request = '{"SSID": "GRC Surveillance van 42", "BSSID": "20:aa:4b:ff:8c:f9", "secured": "0", "capabilities": "[WPA2-PSK-CCMP+TKIP][WPS][ESS]", "frequency": "2.437", "level": "-71", "distance": "17", "latitude": "45.523365", "longitude": "-73.607073", "altitude": "15", "userid": "1" }'

		try:
			json.loads(request)
		except ValueError as e:
			print "ValueError exception caught! JSON parsing problem"
			sys.exit()

		if "request" in data:
			obj_json = json.loads(request)

			con = mdb.connect('localhost', 'wardriver', 'mRC22jrM9d6WJ3N7', 'wardriver')
			with con:
				cur = con.cursor()
				cur.execute("INSERT INTO access_points (SSID,BSSID,secured,capabilities,frequency,level,distance,longitude,latitude,altitude,userId) VALUES(\"GRC Surveillance van 42\", \"20:aa:4b:ff:8c:f9\", \"0\", \"[WPA2-PSK-CCMP+TKIP][WPS][ESS]\", \"2.437\", \"-71\", \"17\", \"45.523365\", \"-73.607073\", \"15\", \"1\")")
			print "insertion succesful"

SocketServer.TCPServer.allow_reuse_address = True
server = SocketServer.TCPServer(("0.0.0.0", 9000), EchoHandler)
server.serve_forever()
