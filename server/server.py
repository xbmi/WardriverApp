#!/usr/bin/env python
# -*- coding: utf-8 -*-

import SocketServer
import json
import sys
import os
import MySQLdb as mdb
from threading import Thread
from time import sleep
import collections
import Queue

import pprint


class Handler(SocketServer.BaseRequestHandler) :
	def handle(self):
		#queue = queue.Queue()
		print "Got connection from : " , self.client_address
		print "RECV"
		message_size = self.request.recv(16)
		print "Message size:" , int(message_size) , " (en string = (",message_size,"))"
		data = ""
		#data = self.request.recv(int(message_size))
		for i in range(int(message_size)):
			data += self.request.recv(1)
		
		while 1:
			recp = self.request.recv(1)
			if recp == "\n":
				print "FIN DE RECEPTION!"
				break
			else:
				print "reception supplementaire de: '" + str(recp) + "'"
				data += recp

		#print "i = " + str(i)
		#data += self.request.recv(1)

		#data += self.request.recv(16)
		#data = recept
		#while len(recept.strip()):
		#	print "boucle... recu: " + str(recept)
		#	recept = self.request.recv(1024)
		#	data += recept
		print data
		#file = open("output.json", "w")
		#file.write(data)
		#file.close()
		print "recu data de grosseur " + str(len(data))	
		#data = data[:-1]
		try:
			json.loads(data)
		except ValueError as e:
			print "ValueError exception caught! JSON parsing problem"
			self.request.send("error\n")
		else:
			print "JSON PARSING.... "
			obj_json = json.loads(data)
			
			con = mdb.connect('localhost', 'wardriver', 'mRC22jrM9d6WJ3N7', 'wardriver')

			for wifi in obj_json:
				if obj_json[wifi]['secured']:
					secured = "1"
				else:
					secured = "0"
				with con:
					curs = con.cursor()
					curs.execute("SELECT COUNT(*) FROM access_points WHERE BSSID = '"+obj_json[wifi]['BSSID']+"'")
					(number_rows,) = curs.fetchone()
					if number_rows == 0:		
						cur = con.cursor()
						requete_sql = "INSERT INTO access_points (SSID,BSSID,secured,capabilities,frequency,level,distance,longitude,latitude,altitude) VALUES('%s','%s',%s,'%s','%s','%s','%s','%s','%s','%s')" % (obj_json[wifi]['BSSID'], str(obj_json[wifi]['BSSID']), secured, str(obj_json[wifi]['capabilities']), str(obj_json[wifi]['frequency']), str(obj_json[wifi]['level']), str(obj_json[wifi]['distance']), str(obj_json[wifi]['longitude']), str(obj_json[wifi]['latitude']), str(obj_json[wifi]['altitude']))
						print requete_sql
						cur.execute(requete_sql)
					elif number_rows == 1:
						cur = con.cursor()
						#print "Entree 'BSSID="+obj_json[wifi]['BSSID']+"' deja presente dans la bd"
						requete_sql = "UPDATE access_points SET SSID = '%s', secured = %s, capabilities = '%s', frequency = '%s', level = '%s', distance = '%s', longitude = '%s', latitude = '%s', altitude = '%s' WHERE BSSID = '%s'" % (obj_json[wifi]['SSID'], secured, obj_json[wifi]['capabilities'], str(obj_json[wifi]['frequency']), str(obj_json[wifi]['level']), str(obj_json[wifi]['distance']), str(obj_json[wifi]['longitude']), str(obj_json[wifi]['latitude']), str(obj_json[wifi]['altitude']), obj_json[wifi]['BSSID'])
						print requete_sql
						cur.execute(requete_sql)
						#requete_sql = "UPDATE access_points SET SSID = '"+obj_json[wifi]['SSID']+"'"
						#requete_sql += ", secured = '"+secured+"'"
						#requete_sql += ", capabilities = '"+obj_json[wifi]['capabilities']+"'"
						#requete_sql += ", frequency = '"+str(obj_json[wifi]['frequency'])+"'"
						#requete_sql += ", level = '"+str(obj_json[wifi]['level'])+"'"
						#requete_sql += ", distance = '"+str(obj_json[wifi]['distance'])+"'"
						#requete_sql += ", longitude = '"+str(obj_json[wifi]['longitude'])+"'"
						#requete_sql += ", latitude = '"+str(obj_json[wifi]['latitude'])+"'"
						#requete_sql += ", altitude = '"+str(obj_json[wifi]['altitude'])+"'"
						#requete_sql += " WHERE BSSID = '"+obj_json[wifi]['BSSID']+"'"
						#print requete_sql
						#cur.execute(requete_sql)
					else:
						print "=== Plus de deux entree avec le meme BSSID, WOOT ==="
			with con:
				curseur = con.cursor()
				curseur.execute("SELECT * FROM access_points")
				rows = curseur.fetchall()
				objet = json.dumps(rows, encoding='latin1')
				print "envoi d'une chaine de " + str(len(objet)) + " char"
				#print "ENVOI: " + objet
				self.request.send(objet + "\n")


		#self.request.send("Insertion successful\n")
		print "Client left\n" 


SocketServer.TCPServer.allow_reuse_address = True
server = SocketServer.ThreadingTCPServer(("0.0.0.0", 9000), Handler)
server.serve_forever()
