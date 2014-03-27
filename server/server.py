#!/usr/bin/env python
# -*- coding: utf-8 -*-

import SocketServer
import json
import sys
import os
import MySQLdb as mdb
from threading import Thread
from time import sleep


class Handler(SocketServer.BaseRequestHandler) :
	def handle(self):
		print "Got connection from : " , self.client_address
		data = "foo"
		while len(data):
			print " === recv"
			data = self.request.recv(81920)

			if data.strip():
				file = open("output.json", "w")
				file.write(data)
				file.close()
				#print data
				t = Thread(target=self.serveur_thread, args=(data,))
				t.start()
				self.request.send("200\n")
			else:
				print "**** rentre pas"
		print "Client left" 

	def serveur_thread(self, client_data):
		print "===== THREAD ====="
		try:
			json.loads(client_data)
		except ValueError as e:
			print "ValueError exception caught! JSON parsing problem"
		


		#obj_json = json.loads(client_data)

		#for wifi in obj_json:
		#	print obj_son["SSID"]
		#con = mdb.connect('localhost', 'wardriver', 'mRC22jrM9d6WJ3N7', 'wardriver')
		#with con:
		#	cur = con.cursor()
		#	cur.execute("INSERT INTO access_points (SSID,BSSID,secured,capabilities,frequency,level,distance,longitude,latitude,altitude,userId) VALUES(\"GRC Surveillance van 42\", \"20:aa:4b:ff:8c:f9\", \"0\", \"[WPA2-PSK-CCMP+TKIP][WPS][ESS]\", \"2.437\", \"-71\", \"17\", \"45.523365\", \"-73.607073\", \"15\", \"1\")")
		#print "insertion succesful"

SocketServer.TCPServer.allow_reuse_address = True
server = SocketServer.TCPServer(("0.0.0.0", 9000), Handler)
server.serve_forever()
